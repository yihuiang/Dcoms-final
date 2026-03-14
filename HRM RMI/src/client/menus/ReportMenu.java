package client.menus;

import client.controllers.ReportController;
import common.models.Employee;

import java.util.List;
import java.util.Scanner;

/**
 * Console menu for the Reporting Module.
 * Presents options to HR staff, collects input, and delegates
 * all logic to ReportController.
 */
public class ReportMenu {

    private final ReportController controller;
    private final Scanner          scanner;

    public ReportMenu(ReportController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner    = scanner;
    }

    public void show() {
        boolean running = true;
        while (running) {
            printHeader();
            System.out.println("  [1] Generate Employee Report");
            System.out.println("  [2] Generate Monthly Report");
            System.out.println("  [3] Generate Yearly Report");
            System.out.println("  [0] Back");
            System.out.println("============================================================");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": handleEmployeeReport(); break;
                case "2": handleMonthlyReport();  break;
                case "3": handleYearlyReport();   break;
                case "0": running = false;         break;
                default:
                    System.out.println("[!] Invalid option. Please try again.\n");
            }
        }
    }

    // ── Employee Report ───────────────────────────────────────

    private void handleEmployeeReport() {
        System.out.println("\n--- Generate Employee Report ---");

        List<Employee> employees = controller.getReportableEmployees();

        if (employees == null || employees.isEmpty()) {
            System.out.println("[!] No approved employees found.");
            pressEnterToContinue();
            return;
        }

        // Display employee selection list
        System.out.println();
        System.out.printf("  %-6s %-25s %-20s %-15s%n",
                "No.", "Name", "Department", "Employee ID");
        System.out.printf("  %-6s %-25s %-20s %-15s%n",
                "---", "-".repeat(23), "-".repeat(18), "-".repeat(13));

        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            System.out.printf("  %-6d %-25s %-20s %-15s%n",
                    i + 1,
                    truncate(e.getFullName(), 23),
                    truncate(e.getDepartment(), 18),
                    e.getEmployeeId());
        }

        System.out.println();
        System.out.print("Enter number to select employee (or 0 to cancel): ");
        String input = scanner.nextLine().trim();

        int selection;
        try {
            selection = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            System.out.println("[!] Invalid input.");
            pressEnterToContinue();
            return;
        }

        if (selection == 0) return;

        if (selection < 1 || selection > employees.size()) {
            System.out.println("[!] Selection out of range.");
            pressEnterToContinue();
            return;
        }

        Employee selected  = employees.get(selection - 1);
        String   report    = controller.generateEmployeeReport(selected.getEmployeeId());

        System.out.println("\n" + report);
        promptExport(report, "employee_report_" + selected.getEmployeeId());
    }

    // ── Monthly Report ────────────────────────────────────────

    private void handleMonthlyReport() {
        System.out.println("\n--- Generate Monthly Report ---");

        int month = promptInt("Enter month (1-12): ", 1, 12);
        if (month == -1) return;

        int year  = promptInt("Enter year (e.g. 2025): ", 2000, 2100);
        if (year == -1) return;

        String report = controller.generateMonthlyReport(month, year);
        System.out.println("\n" + report);

        String filename = String.format("monthly_report_%02d_%d", month, year);
        promptExport(report, filename);
    }

    // ── Yearly Report ─────────────────────────────────────────

    private void handleYearlyReport() {
        System.out.println("\n--- Generate Yearly Report ---");

        int year = promptInt("Enter year (e.g. 2025): ", 2000, 2100);
        if (year == -1) return;

        String report = controller.generateYearlyReport(year);
        System.out.println("\n" + report);

        promptExport(report, "yearly_report_" + year);
    }

    // ── Export Prompt ─────────────────────────────────────────

    private void promptExport(String reportContent, String suggestedFilename) {
        System.out.print("Export report to .txt file? (Y/N): ");
        String answer = scanner.nextLine().trim();

        if ("y".equalsIgnoreCase(answer)) {
            System.out.print("Enter filename (leave blank for default '" + suggestedFilename + ".txt'): ");
            String customName = scanner.nextLine().trim();
            String filename   = customName.isEmpty() ? suggestedFilename : customName;

            // Ensure .txt extension
            if (!filename.toLowerCase().endsWith(".txt")) {
                filename += ".txt";
            }

            boolean saved = controller.exportReportToFile(reportContent, filename);
            if (saved) {
                System.out.println("[✓] Report saved to: " + filename);
            } else {
                System.out.println("[!] Failed to save report.");
            }
        }

        pressEnterToContinue();
    }

    // ── Utilities ─────────────────────────────────────────────

    /**
     * Prompts for an integer in [min, max]. Returns -1 on invalid input.
     */
    private int promptInt(String prompt, int min, int max) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            int value = Integer.parseInt(input);
            if (value < min || value > max) {
                System.out.printf("[!] Value must be between %d and %d.%n", min, max);
                pressEnterToContinue();
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid number entered.");
            pressEnterToContinue();
            return -1;
        }
    }

    private void printHeader() {
        System.out.println("\n============================================================");
        System.out.println("                     REPORTS MENU");
        System.out.println("============================================================");
    }

    private void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 1) + "…" : text;
    }
}