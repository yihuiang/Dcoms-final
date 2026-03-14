package client.controllers;

import common.interfaces.LeaveService;
import common.models.LeaveApplication;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Bridges the CLI menus and the RMI LeaveService stub.
 * All RemoteException handling surfaces a clean message to the UI layer.
 */
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // ── Apply & Submit ────────────────────────────────────────────────────────

    /**
     * Builds a LeaveApplication object from raw string input, then submits it.
     * Returns the created application on success, or throws on error.
     */
    public LeaveApplication applyAndSubmit(String employeeEmail, String name, String role,
                                           String fromDateStr, String toDateStr) throws Exception {
        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(fromDateStr);
            toDate   = LocalDate.parse(toDateStr);
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid date format. Please use YYYY-MM-DD.");
        }

        LeaveApplication la = leaveService.applyForLeave(employeeEmail, name, role, fromDate, toDate);
        boolean ok = leaveService.submitLeaveApplication(la);
        if (!ok) throw new Exception("Failed to submit leave application.");
        return la;
    }

    // ── View Balance ──────────────────────────────────────────────────────────

    public int getLeaveBalance(String employeeEmail) throws RemoteException {
        return leaveService.viewLeaveBalance(employeeEmail);
    }

    // ── View Status ───────────────────────────────────────────────────────────

    public List<LeaveApplication> getMyApplications(String employeeEmail) throws RemoteException {
        return leaveService.viewLeaveApplicationStatus(employeeEmail);
    }

    // ── HR: Pending List ──────────────────────────────────────────────────────

    public List<LeaveApplication> getPendingApplications() throws RemoteException {
        return leaveService.getAllPendingApplications();
    }

    // ── HR: Approve ───────────────────────────────────────────────────────────

    public boolean approve(String applicationId) throws RemoteException {
        return leaveService.approveLeaveApplication(applicationId);
    }

    // ── HR: Reject ────────────────────────────────────────────────────────────

    public boolean reject(String applicationId) throws RemoteException {
        return leaveService.rejectLeaveApplication(applicationId);
    }

    // ── All Applications (HR view) ────────────────────────────────────────────

    public List<LeaveApplication> getAllApplications() throws RemoteException {
        return leaveService.getAllLeaveApplications();
    }
}