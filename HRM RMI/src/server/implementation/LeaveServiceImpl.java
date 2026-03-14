package server.implementation;

import common.interfaces.LeaveService;
import common.models.LeaveApplication;
import server.repository.EmployeeRepository;
import server.repository.LeaveRepository;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeaveServiceImpl extends UnicastRemoteObject implements LeaveService {

    private static final long serialVersionUID = 1L;

    private final LeaveRepository    leaveRepo;
    private final EmployeeRepository employeeRepo;

    public LeaveServiceImpl(LeaveRepository leaveRepo, EmployeeRepository employeeRepo)
            throws RemoteException {
        super();
        this.leaveRepo    = leaveRepo;
        this.employeeRepo = employeeRepo;
    }

    // ── Apply for Leave ───────────────────────────────────────────────────────

    @Override
    public LeaveApplication applyForLeave(String employeeEmail, String name, String role,
                                          LocalDate fromDate, LocalDate toDate)
            throws RemoteException {

        if (fromDate.isAfter(toDate)) {
            throw new RemoteException("Invalid dates: 'from' date must be before or equal to 'to' date.");
        }

        String id = "LA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LeaveApplication la = new LeaveApplication(id, employeeEmail, name, role, fromDate, toDate);
        return la; // not yet persisted – caller must call submitLeaveApplication
    }

    // ── Submit Leave Application ──────────────────────────────────────────────

    @Override
    public boolean submitLeaveApplication(LeaveApplication leaveApplication) throws RemoteException {
        // Check if employee has enough balance
        int balance = viewLeaveBalance(leaveApplication.getEmployeeEmail());
        if (leaveApplication.getAmountOfDays() > balance) {
            throw new RemoteException("Insufficient leave balance. Requested: "
                    + leaveApplication.getAmountOfDays() + " days, Available: " + balance + " days.");
        }
        leaveRepo.add(leaveApplication);
        return true;
    }

    @Override
    public int viewLeaveBalance(String employeeEmail) throws RemoteException {
        return 0;
    }

    // ── View Leave Balance ────────────────────────────────────────────────────

    //@Override
    /*public int viewLeaveBalance(String employeeEmail) throws RemoteException {
        // Get total leave balance from employee record
        int totalBalance = employeeRepo.getTotalLeaveBalance(employeeEmail);

        // Subtract approved leave days
        List<LeaveApplication> approved = leaveRepo.findByEmail(employeeEmail)
                .stream()
                .filter(la -> la.getStatus().equalsIgnoreCase("Approved"))
                .collect(Collectors.toList());

        int usedDays = approved.stream().mapToInt(LeaveApplication::getAmountOfDays).sum();
        return totalBalance - usedDays;
    }*/

    // ── View Leave Application Status ─────────────────────────────────────────

    @Override
    public List<LeaveApplication> viewLeaveApplicationStatus(String employeeEmail)
            throws RemoteException {
        return leaveRepo.findByEmail(employeeEmail);
    }

    // ── Approve Leave Application ─────────────────────────────────────────────

    @Override
    public boolean approveLeaveApplication(String applicationId) throws RemoteException {
        LeaveApplication la = leaveRepo.findById(applicationId);
        if (la == null) throw new RemoteException("Application not found: " + applicationId);
        if (!la.getStatus().equalsIgnoreCase("Pending")) {
            throw new RemoteException("Application is not in Pending status.");
        }
        la.setStatus("Approved");
        return leaveRepo.update(la);
    }

    // ── Reject Leave Application ──────────────────────────────────────────────

    @Override
    public boolean rejectLeaveApplication(String applicationId) throws RemoteException {
        LeaveApplication la = leaveRepo.findById(applicationId);
        if (la == null) throw new RemoteException("Application not found: " + applicationId);
        if (!la.getStatus().equalsIgnoreCase("Pending")) {
            throw new RemoteException("Application is not in Pending status.");
        }
        la.setStatus("Declined");
        return leaveRepo.update(la);
    }

    // ── Get All Pending ───────────────────────────────────────────────────────

    @Override
    public List<LeaveApplication> getAllPendingApplications() throws RemoteException {
        return leaveRepo.findByStatus("Pending");
    }

    // ── Get All ───────────────────────────────────────────────────────────────

    @Override
    public List<LeaveApplication> getAllLeaveApplications() throws RemoteException {
        return leaveRepo.loadAll();
    }

    // ── Get by Employee ───────────────────────────────────────────────────────

    @Override
    public List<LeaveApplication> getLeaveApplicationsByEmployee(String employeeEmail)
            throws RemoteException {
        return leaveRepo.findByEmail(employeeEmail);
    }
}