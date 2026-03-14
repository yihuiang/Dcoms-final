package client.menus;

import client.controllers.LeaveController;
import common.models.LeaveApplication;
import common.models.User;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

/**
 * CLI menu for the Leave Management Module.
 * Employees see: Apply, View Balance, View Status.
 * HR staff see:  All Applications, Approve/Reject, View All.
 */
public class LeaveMenu {

    private static final String DIVIDER =
            "============================================================";
    private static final String THIN =
            "------------------------------------------------------------";

    private final LeaveController leaveController;
    private final Scanner         scanner;

    public LeaveMenu(LeaveController leaveController, Scanner scanner) {
        this.leaveController = leaveController;
        this.scanner         = scanner;
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public void show(User currentUser) {
        boolean running = true;
        while (running) {
            printLeaveHeader();
            if (isHR(currentUser)) {
                printHROptions();
            } else {
                printEmployeeOptions();
            }

            System.out.print("  Enter choice: ");
            String choice = scanner.nextLine().trim();

            if (isHR(currentUser)) {
                running = handleHRChoice(choice, currentUser);
            } else {
                running = handleEmployeeChoice(choice, currentUser);
            }
        }
    }

    // ── Headers ───────────────────────────────────────────────────────────────

    private void printLeaveHeader() {
        System.out.println("\n" + DIVIDER);
        System.out.println("         LEAVE MANAGEMENT MODULE");
        System.out.println(DIVIDER);
    }

    private void printEmployeeOptions() {
        System.out.println("  [1] Apply for Leave");
        System.out.println("  [2] View Leave Balance");
        System.out.println("  [3] View My Leave Application Status");
        System.out.println("  [0] Back");
        System.out.println(THIN);
    }

    private void printHROptions() {
        System.out.println("  [1] View All Pending Applications");
        System.out.println("  [2] Approve Leave Application");
        System.out.println("  [3] Reject Leave Application");
        System.out.println("  [4] View All Leave Applications");
        System.out.println("  [0] Back");
        System.out.println(THIN);
    }

    // ── Employee Handlers ─────────────────────────────────────────────────────

    private boolean handleEmployeeChoice(String choice, User currentUser) {
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

        // Show current balance first
        try {
            int balance = leaveController.getLeaveBalance(currentUser.getEmail());
            System.out.println("  Current Leave Balance : " + balance + " day(s)");
        } catch (RemoteException e) {
            System.out.println("  [!] Could not retrieve balance: " + e.getMessage());
        }

        System.out.println(THIN);
        System.out.println("  Applicant Name  : " + currentUser.getName());
        System.out.println("  Job Role        : " + currentUser.getRole());
        System.out.println(THIN);
        System.out.println("  Enter leave dates (format: YYYY-MM-DD)");

        String fromDate = prompt("  From Date       : ");
        String toDate   = prompt("  To Date         : ");

        // Preview & confirm
        System.out.println(THIN);
        System.out.println("  ── Leave Application Preview ──");
        System.out.println("  Name        : " + currentUser.getName());
        System.out.println("  Role        : " + currentUser.getRole());
        System.out.println("  From        : " + fromDate);
        System.out.println("  To          : " + toDate);
        System.out.println(THIN);
        System.out.print("  Confirm and submit? (Y/N): ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("  [i] Application cancelled.");
            return;
        }

        try {
            LeaveApplication la = leaveController.applyAndSubmit(
                    currentUser.getEmail(),
                    currentUser.getName(),
                    currentUser.getRole(),
                    fromDate,
                    toDate
            );
            System.out.println("\n  ✔  Leave application submitted successfully!");
            printApplicationCard(la);
        } catch (Exception e) {
            System.out.println("\n  [✘] Submission failed: " + e.getMessage());
        }
    }

    private void viewLeaveBalance(User currentUser) {
        System.out.println("\n" + DIVIDER);
        System.out.println("  VIEW LEAVE BALANCE");
        System.out.println(DIVIDER);
        try {
            int balance = leaveController.getLeaveBalance(currentUser.getEmail());
            System.out.println("  Employee  : " + currentUser.getName());
            System.out.println("  Role      : " + currentUser.getRole());
            System.out.println(THIN);
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
        try {
            List<LeaveApplication> list = leaveController.getMyApplications(currentUser.getEmail());
            if (list.isEmpty()) {
                System.out.println("  No leave applications found.");
            } else {
                for (LeaveApplication la : list) {
                    printApplicationCard(la);
                }
            }
        } catch (RemoteException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
        System.out.println(DIVIDER);
    }

    // ── HR Handlers ───────────────────────────────────────────────────────────

    private boolean handleHRChoice(String choice, User currentUser) {
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
                    ? "  ✔  Application [" + appId + "] has been APPROVED."
                    : "  [✘] Approval failed.");
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
                    ? "  ✔  Application [" + appId + "] has been DECLINED."
                    : "  [✘] Rejection failed.");
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

    // ── Utility ───────────────────────────────────────────────────────────────

    private void printApplicationCard(LeaveApplication la) {
        String statusBadge = switch (la.getStatus()) {
            case "Approved" -> "[ ✔ APPROVED ]";
            case "Declined" -> "[ ✘ DECLINED ]";
            default         -> "[  PENDING   ]";
        };

        System.out.println(THIN);
        System.out.println("  Application ID   : " + la.getApplicationId());
        System.out.println("  Employee         : " + la.getName());
        System.out.println("  Job Role         : " + la.getRole());
        System.out.println("  Applied On       : " + la.getInitialDate());
        System.out.println("  Leave Period     : " + la.getFromDate() + "  →  " + la.getToDate());
        System.out.println("  Duration         : " + la.getAmountOfDays() + " day(s)");
        System.out.println("  Status           : " + statusBadge);
        System.out.println(THIN);
    }

    private String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }

    private boolean isHR(User user) {
        // Any role containing "HR" (case-insensitive) is treated as HR staff
        return user != null && user.getRole() != null
                && user.getRole().toUpperCase().contains("HR");
    }
}