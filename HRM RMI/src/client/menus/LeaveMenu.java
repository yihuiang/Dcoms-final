package client.menus;

import client.controllers.LeaveController;
import common.models.Employee;
import common.models.LeaveApplication;
import common.models.User;
import server.repository.EmployeeRepository;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

public class LeaveMenu {

    private static final String DIVIDER     = "============================================================";
    private static final String SINGLE_LINE = "------------------------------------------------------------";

    private final LeaveController    leaveController;
    private final Scanner            scanner;
    // Matches how HRMenu and ClientMain instantiate LeaveMenu:
    // new LeaveMenu(leaveController, scanner, employeeRepo)
    private final EmployeeRepository employeeRepo;

    public LeaveMenu(LeaveController leaveController, Scanner scanner, EmployeeRepository employeeRepo) {
        this.leaveController = leaveController;
        this.scanner         = scanner;
        this.employeeRepo    = employeeRepo;
    }

    public void show(User currentUser) {
        boolean running = true;
        while (running) {
            printLeaveHeader();
            // Uses User.isHR() which handles both "HR" and "HUMAN RESOURCE"
            if (currentUser.isHR()) {
                printHROptions();
            } else {
                printEmployeeOptions();
            }

            System.out.print("  Enter choice: ");
            String choice = scanner.nextLine().trim();

            if (currentUser.isHR()) {
                running = HRChoice(choice, currentUser);
            } else {
                running = EmployeeChoice(choice, currentUser);
            }
        }
    }

    // ── Menu Options ──────────────────────────────────────────────────────────

    private void printLeaveHeader() {
        System.out.println("\n" + DIVIDER);
        System.out.println("         LEAVE MANAGEMENT MODULE         ");
        System.out.println(DIVIDER);
    }

    private void printEmployeeOptions() {
        System.out.println("  [1] Apply for Leave");
        System.out.println("  [2] View Leave Balance");
        System.out.println("  [3] View My Leave Application Status");
        System.out.println("  [0] Back");
        System.out.println(SINGLE_LINE);
    }

    private void printHROptions() {
        System.out.println("  [1] View All Pending Applications");
        System.out.println("  [2] Approve Leave Application");
        System.out.println("  [3] Reject Leave Application");
        System.out.println("  [4] View All Leave Applications");
        System.out.println("  [0] Back");
        System.out.println(SINGLE_LINE);
    }

    // ── Employee ──────────────────────────────────────────────────────────────

    private boolean EmployeeChoice(String choice, User currentUser) {
        switch (choice) {
            case "1" -> applyForLeave(currentUser);
            case "2" -> viewLeaveBalance(currentUser);
            case "3" -> viewApplicationStatus(currentUser);
            case "0" -> { return false; }
            default  -> System.out.println("  [!] Invalid option. Please try again.");
        }
        return true;
    }

    private void applyForLeave(User currentUser) {
        System.out.println("\n" + DIVIDER);
        System.out.println("  APPLY FOR LEAVE");
        System.out.println(DIVIDER);

        // Look up employee by employeeId from User session
        // Uses findById (not viewEmployeeRecord) so PENDING employees can also apply
        Employee employee = employeeRepo.findById(currentUser.getEmployeeId());
        if (employee == null) {
            System.out.println("  [!] Employee record not found.");
            return;
        }

        try {
            int balance = leaveController.getLeaveBalance(employee.getEmployeeId());
            System.out.println("  Current Leave Balance : " + balance + " day(s)");
        } catch (RemoteException e) {
            System.out.println("  [!] Could not retrieve balance: " + e.getMessage());
        }

        System.out.println(SINGLE_LINE);
        // Name and position from employees.json
        System.out.println("  Applicant Name  : " + employee.getFullName());
        System.out.println("  Job Role        : " + employee.getPosition());
        System.out.println(SINGLE_LINE);
        System.out.println("  Enter leave dates (format: YYYY-MM-DD)");

        String fromDate = prompt("  From Date       : ");
        String toDate   = prompt("  To Date         : ");

        System.out.println(SINGLE_LINE);
        System.out.println("  ── Leave Application Preview ──");
        System.out.println("  Name        : " + employee.getFullName());
        System.out.println("  Role        : " + employee.getPosition());
        System.out.println("  From        : " + fromDate);
        System.out.println("  To          : " + toDate);
        System.out.println(SINGLE_LINE);
        System.out.print("  Confirm and submit? (Y/N): ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("  [i] Application cancelled.");
            return;
        }

        try {
            LeaveApplication la = leaveController.applyAndSubmit(
                    employee.getEmployeeId(),
                    fromDate,
                    toDate
            );
            System.out.println("\n  Leave application submitted successfully!");
            printApplicationCard(la);
        } catch (Exception e) {
            System.out.println("\n  Submission failed: " + e.getMessage());
        }
    }

    private void viewLeaveBalance(User currentUser) {
        System.out.println("\n" + DIVIDER);
        System.out.println("  VIEW LEAVE BALANCE");
        System.out.println(DIVIDER);

        // Name and position from employees.json via employeeId
        Employee employee = employeeRepo.findById(currentUser.getEmployeeId());
        if (employee == null) {
            System.out.println("  [!] Employee record not found.");
            return;
        }

        try {
            int balance = leaveController.getLeaveBalance(employee.getEmployeeId());
            System.out.println("  Employee  : " + employee.getFullName());
            System.out.println("  Role      : " + employee.getPosition());
            System.out.println(SINGLE_LINE);
            System.out.printf("  Available Leave Balance  : %d day(s)%n", balance);
            System.out.println(DIVIDER);
        } catch (RemoteException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    private void viewApplicationStatus(User currentUser) {
        System.out.println("\n" + DIVIDER);
        System.out.println("  MY LEAVE APPLICATION STATUS");
        System.out.println(DIVIDER);

        // Get employeeId from session, look up name from employees.json
        Employee employee = employeeRepo.findById(currentUser.getEmployeeId());
        if (employee == null) {
            System.out.println("  [!] Employee record not found.");
            return;
        }

        try {
            // Fetches from leave_requests.json by employeeId
            List<LeaveApplication> list = leaveController.getMyApplications(employee.getEmployeeId());
            if (list.isEmpty()) {
                System.out.println("  No leave applications found.");
            } else {
                System.out.printf("  Total Applications: %d%n%n", list.size());
                for (LeaveApplication la : list) {
                    printApplicationCard(la);
                }
            }
        } catch (RemoteException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
        System.out.println(DIVIDER);
    }

    // ── HR ────────────────────────────────────────────────────────────────────

    private boolean HRChoice(String choice, User currentUser) {
        switch (choice) {
            case "1" -> viewPendingApplications();
            case "2" -> approveApplication();
            case "3" -> rejectApplication();
            case "4" -> viewAllApplications();
            case "0" -> { return false; }
            default  -> System.out.println("  [!] Invalid option. Please try again.");
        }
        return true;
    }

    private void viewPendingApplications() {
        System.out.println("\n" + DIVIDER);
        System.out.println("  PENDING LEAVE APPLICATIONS");
        System.out.println(DIVIDER);
        try {
            List<LeaveApplication> list = leaveController.getPendingApplications();
            if (list.isEmpty()) {
                System.out.println("  No pending applications.");
            } else {
                System.out.printf("  Total Pending: %d%n%n", list.size());
                for (LeaveApplication la : list) {
                    printApplicationCard(la);
                }
            }
        } catch (RemoteException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
        System.out.println(DIVIDER);
    }

    private void approveApplication() {
        System.out.println("\n" + DIVIDER);
        System.out.println("  APPROVE LEAVE APPLICATION");
        System.out.println(DIVIDER);
        String appId = prompt("  Enter Application ID to approve: ");
        System.out.print("  Confirm approval of [" + appId + "]? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("  [i] Action cancelled.");
            return;
        }
        try {
            boolean ok = leaveController.approve(appId);
            System.out.println(ok
                    ? "  Application [" + appId + "] has been APPROVED."
                    : "  Approval failed.");
        } catch (RemoteException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    private void rejectApplication() {
        System.out.println("\n" + DIVIDER);
        System.out.println("  REJECT LEAVE APPLICATION");
        System.out.println(DIVIDER);
        String appId = prompt("  Enter Application ID to reject: ");
        System.out.print("  Confirm rejection of [" + appId + "]? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("  [i] Action cancelled.");
            return;
        }
        try {
            boolean ok = leaveController.reject(appId);
            System.out.println(ok
                    ? "  Application [" + appId + "] has been DECLINED."
                    : "  Rejection failed.");
        } catch (RemoteException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    private void viewAllApplications() {
        System.out.println("\n" + DIVIDER);
        System.out.println("  ALL LEAVE APPLICATIONS");
        System.out.println(DIVIDER);
        try {
            List<LeaveApplication> list = leaveController.getAllApplications();
            if (list.isEmpty()) {
                System.out.println("  No applications found.");
            } else {
                System.out.printf("  Total: %d%n%n", list.size());
                for (LeaveApplication la : list) {
                    printApplicationCard(la);
                }
            }
        } catch (RemoteException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
        System.out.println(DIVIDER);
    }

    // ── Card Display ──────────────────────────────────────────────────────────

    private void printApplicationCard(LeaveApplication la) {
        // Name and position from employees.json using employeeId stored in leave_requests.json
        Employee employee = employeeRepo.findById(la.getEmployeeId());
        String fullName = (employee != null) ? employee.getFullName() : "Unknown";
        String position = (employee != null) ? employee.getPosition() : "Unknown";

        String statusBadge = switch (la.getStatus()) {
            case "Approved" -> "[ APPROVED ]";
            case "Declined" -> "[ DECLINED ]";
            default         -> "[ PENDING  ]";
        };

        System.out.println(SINGLE_LINE);
        System.out.println("  Application ID   : " + la.getApplicationId());
        System.out.println("  Employee ID      : " + la.getEmployeeId());
        System.out.println("  Employee Name    : " + fullName);
        System.out.println("  Job Role         : " + position);
        System.out.println("  Applied On       : " + la.getInitialDate());
        System.out.println("  Leave Period     : " + la.getFromDate() + "  to  " + la.getToDate());
        System.out.println("  Duration         : " + la.getAmountOfDays() + " day(s)");
        System.out.println("  Status           : " + statusBadge);
        System.out.println(SINGLE_LINE);
    }

    private String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }
}