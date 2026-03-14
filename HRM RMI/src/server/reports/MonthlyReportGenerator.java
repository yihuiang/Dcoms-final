package server.reports;

import common.models.Employee;
import common.models.LeaveApplication;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates a monthly HR report covering leave activity and payroll
 * for a specified month and year.
 */
public class MonthlyReportGenerator {

    private static final String DIVIDER =
            "============================================================";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Builds and returns the formatted monthly report as a String.
     *
     * @param employees  full list of approved employees
     * @param allLeaves  full list of all leave applications
     * @param month      1–12
     * @param year       e.g. 2025
     * @return           the formatted report
     */
    public String generate(List<Employee> employees,
                           List<LeaveApplication> allLeaves,
                           int month, int year) {

        String timestamp  = LocalDateTime.now().format(TIMESTAMP_FMT);
        String monthLabel = Month.of(month).name().charAt(0)
                + Month.of(month).name().substring(1).toLowerCase();

        StringBuilder sb = new StringBuilder();

        // ── Header ────────────────────────────────────────────
        sb.append(DIVIDER).append("\n");
        sb.append(centre(String.format("MONTHLY REPORT — %s %d", monthLabel.toUpperCase(), year)))
                .append("\n");
        sb.append(centre("Generated: " + timestamp)).append("\n");
        sb.append(DIVIDER).append("\n\n");

        // ── Filter leaves that fall within the target month/year ──
        List<LeaveApplication> monthLeaves = new ArrayList<>();
        for (LeaveApplication la : allLeaves) {
            if (la.getFromDate().getMonthValue() == month
                    && la.getFromDate().getYear() == year) {
                monthLeaves.add(la);
            }
        }

        // ── Per-employee leave stats ───────────────────────────
        // employeeId -> [approvedDays, approvedCount, pendingCount, rejectedCount]
        Map<String, int[]> leaveStats = new LinkedHashMap<>();
        for (Employee e : employees) {
            leaveStats.put(e.getEmployeeId().toUpperCase(), new int[]{0, 0, 0, 0});
        }

        for (LeaveApplication la : monthLeaves) {
            String key = la.getEmployeeId().toUpperCase();
            leaveStats.putIfAbsent(key, new int[]{0, 0, 0, 0});
            int[] stats = leaveStats.get(key);
            String status = la.getStatus().toLowerCase();
            switch (status) {
                case "approved":
                    stats[0] += la.getAmountOfDays();
                    stats[1]++;
                    break;
                case "pending":
                    stats[2]++;
                    break;
                case "rejected":
                case "declined":
                    stats[3]++;
                    break;
            }
        }

        // ── Leave Summary Table ────────────────────────────────
        sb.append("--- Leave Summary ---\n");
        sb.append(String.format("%-12s %-20s %-12s %-10s %-10s %-10s%n",
                "Emp ID", "Name", "Days Taken", "Approved", "Pending", "Rejected"));
        sb.append(String.format("%-12s %-20s %-12s %-10s %-10s %-10s%n",
                "-".repeat(10), "-".repeat(18), "-".repeat(10),
                "-".repeat(8), "-".repeat(8), "-".repeat(8)));

        int grandTotalApprovedDays   = 0;
        int grandTotalApprovedCount  = 0;
        int grandTotalPendingCount   = 0;
        int grandTotalRejectedCount  = 0;

        for (Employee e : employees) {
            String key    = e.getEmployeeId().toUpperCase();
            int[]  stats  = leaveStats.getOrDefault(key, new int[]{0, 0, 0, 0});
            String name   = truncate(e.getFullName(), 18);

            sb.append(String.format("%-12s %-20s %-12d %-10d %-10d %-10d%n",
                    e.getEmployeeId(), name,
                    stats[0], stats[1], stats[2], stats[3]));

            grandTotalApprovedDays  += stats[0];
            grandTotalApprovedCount += stats[1];
            grandTotalPendingCount  += stats[2];
            grandTotalRejectedCount += stats[3];
        }
        sb.append("\n");

        // ── Department Summary Table ───────────────────────────
        sb.append("--- Department Summary ---\n");
        sb.append(String.format("%-20s %-18s %-18s %-18s%n",
                "Department", "Total Employees", "Total Leave Days", "Total Salary (RM)"));
        sb.append(String.format("%-20s %-18s %-18s %-18s%n",
                "-".repeat(18), "-".repeat(16), "-".repeat(16), "-".repeat(16)));

        // Group employees by department
        Map<String, List<Employee>> byDept = new LinkedHashMap<>();
        for (Employee e : employees) {
            byDept.computeIfAbsent(e.getDepartment(), dept -> new ArrayList<>()).add(e);
        }

        double grandTotalSalary = 0;

        for (Map.Entry<String, List<Employee>> entry : byDept.entrySet()) {
            String dept         = entry.getKey();
            List<Employee> dept_emps = entry.getValue();
            int deptLeaveDays   = 0;
            double deptSalary   = 0;

            for (Employee e : dept_emps) {
                String key = e.getEmployeeId().toUpperCase();
                int[]  stats = leaveStats.getOrDefault(key, new int[]{0, 0, 0, 0});
                deptLeaveDays += stats[0];
                deptSalary    += e.getSalary();
            }

            grandTotalSalary += deptSalary;

            sb.append(String.format("%-20s %-18d %-18d RM %,.2f%n",
                    truncate(dept, 18),
                    dept_emps.size(),
                    deptLeaveDays,
                    deptSalary));
        }
        sb.append("\n");

        // ── Month Totals ───────────────────────────────────────
        sb.append("--- Month Totals ---\n");
        sb.append(String.format("%-35s: %d%n",   "Total Employees",          employees.size()));
        sb.append(String.format("%-35s: %d%n",   "Total Leave Days (Approved)", grandTotalApprovedDays));
        sb.append(String.format("%-35s: RM %,.2f%n", "Total Payroll",         grandTotalSalary));
        sb.append(String.format("%-35s: %d%n",   "Approved Leave Applications",  grandTotalApprovedCount));
        sb.append(String.format("%-35s: %d%n",   "Pending Leave Applications",   grandTotalPendingCount));
        sb.append(String.format("%-35s: %d%n",   "Rejected Leave Applications",  grandTotalRejectedCount));

        sb.append(DIVIDER).append("\n");
        return sb.toString();
    }

    // ── Utilities ─────────────────────────────────────────────

    private String centre(String text) {
        int width = 60;
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 1) + "…" : text;
    }
}