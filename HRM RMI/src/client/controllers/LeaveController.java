package client.controllers;

import common.interfaces.LeaveService;
import common.models.LeaveApplication;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class LeaveController {

    private final LeaveService leaveService;

    // FIXED: Takes LeaveService as parameter — matches how HRMenu and ClientMain call it:
    // new LeaveController(leaveService)
    // The LeaveService stub is already looked up in ClientMain via registry.lookup("LeaveService")
    // so LeaveController does NOT need to connect itself
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    public LeaveApplication applyAndSubmit(String employeeId,
                                           String fromDateStr,
                                           String toDateStr) throws Exception {
        if (leaveService == null) throw new Exception("Not connected to server.");

        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(fromDateStr);
            toDate   = LocalDate.parse(toDateStr);
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid date format. Please use YYYY-MM-DD.");
        }

        LeaveApplication la = leaveService.applyForLeave(employeeId, fromDate, toDate);
        boolean ok = leaveService.submitLeaveApplication(la);
        if (!ok) throw new Exception("Failed to submit leave application.");
        return la;
    }

    public int getLeaveBalance(String employeeId) throws RemoteException {
        return leaveService.viewLeaveBalance(employeeId);
    }

    public List<LeaveApplication> getMyApplications(String employeeId) throws RemoteException {
        return leaveService.viewLeaveApplicationStatus(employeeId);
    }

    public List<LeaveApplication> getPendingApplications() throws RemoteException {
        return leaveService.getAllPendingApplications();
    }

    public boolean approve(String applicationId) throws RemoteException {
        return leaveService.approveLeaveApplication(applicationId);
    }

    public boolean reject(String applicationId) throws RemoteException {
        return leaveService.rejectLeaveApplication(applicationId);
    }

    public List<LeaveApplication> getAllApplications() throws RemoteException {
        return leaveService.getAllLeaveApplications();
    }
}