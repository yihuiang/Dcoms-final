package server.reports;

import common.models.Employee;
import common.models.LeaveApplication;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates a detailed report for a single employee,
 * including their profile and full leave history.
 */
public class EmployeeReportGenerator {

    private static final String DIVIDER =
            "============================================================";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Builds and returns the formatted employee report as a String.
     *
     * @param employee   the employee whose report is being generated
     * @param leaveList  all leave applications belonging to this employee
     * @return           the formatted report
     */
    public String generate(Employee employee, List<LeaveApplication> leaveList) {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);

        // ── Header ────────────────────────────────────────────
        sb.append(DIVIDER).append("\n");
        sb.append(centre("EMPLOYEE REPORT")).append("\n");
        sb.append(centre("Generated: " + timestamp)).append("\n");
        sb.append(DIVIDER).append("\n\n");

        // ── Profile ───────────────────────────────────────────
        sb.append(String.format("%-15s: %s%n", "Employee ID",  employee.getEmployeeId()));
        sb.append(String.format("%-15s: %s%n", "Full Name",    employee.getFullName()));
        sb.append(String.format("%-15s: %s%n", "Email",        employee.getEmail()));
        sb.append(String.format("%-15s: %s%n", "Phone",        employee.getPhone()));
        sb.append(String.format("%-15s: %s%n", "Department",   employee.getDepartment()));
        sb.append(String.format("%-15s: %s%n", "Position",     employee.getPosition()));
        sb.append(String.format("%-15s: RM %,.2f%n", "Salary", employee.getSalary()));
        sb.append("\n");

        // ── Leave History ─────────────────────────────────────
        sb.append("--- Leave History ---\n");

        if (leaveList.isEmpty()) {
            sb.append("No leave applications found for this employee.\n");
        } else {
            // Table header
            sb.append(String.format("%-14s %-13s %-13s %-7s %-12s%n",
                    "App ID", "From Date", "To Date", "Days", "Status"));
            sb.append(String.format("%-14s %-13s %-13s %-7s %-12s%n",
                    "-".repeat(12), "-".repeat(11), "-".repeat(11),
                    "-".repeat(5), "-".repeat(10)));

            int totalApprovedDays = 0;

            for (LeaveApplication la : leaveList) {
                sb.append(String.format("%-14s %-13s %-13s %-7d %-12s%n",
                        la.getApplicationId(),
                        la.getFromDate().toString(),
                        la.getToDate().toString(),
                        la.getAmountOfDays(),
                        la.getStatus()));

                if ("approved".equalsIgnoreCase(la.getStatus())) {
                    totalApprovedDays += la.getAmountOfDays();
                }
            }

            sb.append("\n");
            sb.append(String.format("%-40s: %d%n",
                    "Total Leave Days Taken (Approved)", totalApprovedDays));
            sb.append(String.format("%-40s: %d%n",
                    "Total Applications", leaveList.size()));
        }

        sb.append(DIVIDER).append("\n");
        return sb.toString();
    }

    // ── Utility ───────────────────────────────────────────────

    private String centre(String text) {
        int width = 60;
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
}