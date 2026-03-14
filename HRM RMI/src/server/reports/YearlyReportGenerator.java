package server.reports;

import common.models.Employee;
import common.models.LeaveApplication;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates a yearly HR report covering per-employee monthly leave breakdown,
 * department summaries, and full-year payroll totals.
 */
public class YearlyReportGenerator {

    private static final String DIVIDER =
            "============================================================";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Short month labels for the breakdown table header
    private static final String[] MONTH_LABELS = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    /**
     * Builds and returns the formatted yearly report as a String.
     *
     * @param employees all approved employees
     * @param allLeaves all leave applications
     * @param year      e.g. 2025
     * @return          the formatted report
     */
    public String generate(List<Employee> employees,
                           List<LeaveApplication> allLeaves,
                           int year) {

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        StringBuilder sb = new StringBuilder();

        // ── Header ────────────────────────────────────────────
        sb.append(DIVIDER).append("\n");
        sb.append(centre(String.format("YEARLY REPORT — %d", year))).append("\n");
        sb.append(centre("Generated: " + timestamp)).append("\n");
        sb.append(DIVIDER).append("\n\n");

        // ── Filter leaves for target year ──────────────────────
        List<LeaveApplication> yearLeaves = new ArrayList<>();
        for (LeaveApplication la : allLeaves) {
            if (la.getFromDate().getYear() == year) {
                yearLeaves.add(la);
            }
        }

        // ── Build per-employee monthly approved-days matrix ───
        // empId -> int[12] (index 0 = Jan, ... 11 = Dec)
        Map<String, int[]> monthlyMatrix = new LinkedHashMap<>();
        for (Employee e : employees) {
            monthlyMatrix.put(e.getEmployeeId().toUpperCase(), new int[12]);
        }

        // Global status counters
        int totalApproved = 0, totalPending = 0, totalRejected = 0;

        for (LeaveApplication la : yearLeaves) {
            String key    = la.getEmployeeId().toUpperCase();
            String status = la.getStatus().toLowerCase();
            monthlyMatrix.putIfAbsent(key, new int[12]);

            switch (status) {
                case "approved":
                    int monthIdx = la.getFromDate().getMonthValue() - 1;
                    monthlyMatrix.get(key)[monthIdx] += la.getAmountOfDays();
                    totalApproved++;
                    break;
                case "pending":
                    totalPending++;
                    break;
                case "rejected":
                case "declined":
                    totalRejected++;
                    break;
            }
        }

        // ── Per-Employee Annual Leave Breakdown Table ──────────
        sb.append("--- Per-Employee Annual Leave Summary (Approved Days) ---\n");

        // Header row: EmpID | Name | Jan Feb ... Dec | Total
        sb.append(String.format("%-10s %-18s", "Emp ID", "Name"));
        for (String lbl : MONTH_LABELS) {
            sb.append(String.format(" %-4s", lbl));
        }
        sb.append(String.format(" %-6s%n", "Total"));

        // Separator
        sb.append(String.format("%-10s %-18s", "-".repeat(8), "-".repeat(16)));
        sb.append((" ----").repeat(12));
        sb.append(String.format(" %-6s%n", "-----"));

        int grandTotalApprovedDays = 0;

        for (Employee e : employees) {
            String key   = e.getEmployeeId().toUpperCase();
            int[]  grid  = monthlyMatrix.getOrDefault(key, new int[12]);
            int    total = 0;
            for (int d : grid) total += d;
            grandTotalApprovedDays += total;

            sb.append(String.format("%-10s %-18s",
                    e.getEmployeeId(), truncate(e.getFullName(), 16)));
            for (int d : grid) {
                sb.append(String.format(" %-4d", d));
            }
            sb.append(String.format(" %-6d%n", total));
        }
        sb.append("\n");

        // ── Department Annual Summary ──────────────────────────
        sb.append("--- Department Annual Summary ---\n");
        sb.append(String.format("%-22s %-18s %-18s %-20s%n",
                "Department", "Total Employees", "Total Leave Days", "Annual Salary (RM)"));
        sb.append(String.format("%-22s %-18s %-18s %-20s%n",
                "-".repeat(20), "-".repeat(16), "-".repeat(16), "-".repeat(18)));

        // Group by department
        Map<String, List<Employee>> byDept = new LinkedHashMap<>();
        for (Employee e : employees) {
            byDept.computeIfAbsent(e.getDepartment(), dept -> new ArrayList<>()).add(e);
        }

        double grandTotalSalary = 0;

        for (Map.Entry<String, List<Employee>> entry : byDept.entrySet()) {
            String             dept      = entry.getKey();
            List<Employee>     deptEmps  = entry.getValue();
            int    deptLeaveDays = 0;
            double deptSalary    = 0;

            for (Employee e : deptEmps) {
                String key  = e.getEmployeeId().toUpperCase();
                int[]  grid = monthlyMatrix.getOrDefault(key, new int[12]);
                for (int d : grid) deptLeaveDays += d;
                deptSalary += e.getSalary();
            }

            grandTotalSalary += deptSalary;

            sb.append(String.format("%-22s %-18d %-18d RM %,.2f%n",
                    truncate(dept, 20),
                    deptEmps.size(),
                    deptLeaveDays,
                    deptSalary));
        }
        sb.append("\n");

        // ── Year Totals ────────────────────────────────────────
        sb.append("--- Year Totals ---\n");
        sb.append(String.format("%-40s: %d%n",
                "Total Employees",                employees.size()));
        sb.append(String.format("%-40s: %d%n",
                "Total Leave Days (Approved)",     grandTotalApprovedDays));
        sb.append(String.format("%-40s: RM %,.2f%n",
                "Total Annual Payroll",            grandTotalSalary));
        sb.append(String.format("%-40s: %d%n",
                "Approved Leave Applications",     totalApproved));
        sb.append(String.format("%-40s: %d%n",
                "Pending Leave Applications",      totalPending));
        sb.append(String.format("%-40s: %d%n",
                "Rejected Leave Applications",     totalRejected));

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