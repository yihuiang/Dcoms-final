package common.interfaces;

import common.models.LeaveApplication;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public interface LeaveService extends Remote {

    /**
     * Apply for leave – creates a LeaveApplication with status "Pending".
     */
    LeaveApplication applyForLeave(String employeeEmail, String name, String role,
                                   LocalDate fromDate, LocalDate toDate)
            throws RemoteException;

    /**
     * Submit (persist) a leave application that was already created.
     * Returns true on success.
     */
    boolean submitLeaveApplication(LeaveApplication leaveApplication)
            throws RemoteException;

    /**
     * Get current leave balance (remaining days) for an employee.
     */
    int viewLeaveBalance(String employeeEmail)
            throws RemoteException;

    /**
     * Get all leave applications for an employee (for status checking).
     */
    List<LeaveApplication> viewLeaveApplicationStatus(String employeeEmail)
            throws RemoteException;

    /**
     * HR: approve a leave application by its ID.
     */
    boolean approveLeaveApplication(String applicationId)
            throws RemoteException;

    /**
     * HR: reject/decline a leave application by its ID.
     */
    boolean rejectLeaveApplication(String applicationId)
            throws RemoteException;

    /**
     * Get all pending applications (for HR dashboard).
     */
    List<LeaveApplication> getAllPendingApplications()
            throws RemoteException;

    /**
     * Get all leave applications regardless of status (for reports).
     */
    List<LeaveApplication> getAllLeaveApplications()
            throws RemoteException;

    /**
     * Get leave applications for a specific employee (for reports).
     */
    List<LeaveApplication> getLeaveApplicationsByEmployee(String employeeEmail)
            throws RemoteException;
}