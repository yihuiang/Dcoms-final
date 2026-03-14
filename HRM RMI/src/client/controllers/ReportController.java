package client.controllers;

import common.interfaces.ReportService;
import common.models.Employee;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side controller for the Reporting Module.
 * Handles RMI lookup and all calls to ReportService,
 * plus local file export.
 */
public class ReportController {

    private ReportService reportService;

    public ReportController() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 2099);
            reportService = (ReportService) registry.lookup("ReportService");
        } catch (Exception e) {
            System.err.println("[ReportController] Failed to connect to ReportService: "
                    + e.getMessage());
            reportService = null;
        }
    }

    // ── Service calls ─────────────────────────────────────────

    /**
     * Retrieves the list of approved employees available for report selection.
     */
    public List<Employee> getReportableEmployees() {
        if (reportService == null) {
            System.err.println("[ReportController] Not connected to server.");
            return new ArrayList<>();
        }
        try {
            return reportService.getReportableEmployees();
        } catch (Exception e) {
            System.err.println("[ReportController] Error fetching employees: "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Fetches a formatted employee report string from the server.
     *
     * @param employeeId the selected employee's ID
     * @return formatted report, or an error message string
     */
    public String generateEmployeeReport(String employeeId) {
        if (reportService == null) {
            return "Error: Not connected to server.";
        }
        try {
            return reportService.generateEmployeeReport(employeeId);
        } catch (Exception e) {
            return "Error generating employee report: " + e.getMessage();
        }
    }

    /**
     * Fetches a formatted monthly report string from the server.
     *
     * @param month 1–12
     * @param year  e.g. 2025
     * @return formatted report, or an error message string
     */
    public String generateMonthlyReport(int month, int year) {
        if (reportService == null) {
            return "Error: Not connected to server.";
        }
        try {
            return reportService.generateMonthlyReport(month, year);
        } catch (Exception e) {
            return "Error generating monthly report: " + e.getMessage();
        }
    }

    /**
     * Fetches a formatted yearly report string from the server.
     *
     * @param year e.g. 2025
     * @return formatted report, or an error message string
     */
    public String generateYearlyReport(int year) {
        if (reportService == null) {
            return "Error: Not connected to server.";
        }
        try {
            return reportService.generateYearlyReport(year);
        } catch (Exception e) {
            return "Error generating yearly report: " + e.getMessage();
        }
    }

    // ── File export ───────────────────────────────────────────

    /**
     * Saves the given report string to a local .txt file.
     *
     * @param content  the report text to write
     * @param filename the target filename (should end with .txt)
     * @return true if saved successfully, false otherwise
     */
    public boolean exportReportToFile(String content, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            System.err.println("[ReportController] Failed to export report: "
                    + e.getMessage());
            return false;
        }
    }
}