package server.implementation;

import common.interfaces.ReportService;
import common.models.Employee;
import common.models.LeaveApplication;
import server.repository.EmployeeRepository;
import server.repository.LeaveRepository;
import server.reports.EmployeeReportGenerator;
import server.reports.MonthlyReportGenerator;
import server.reports.YearlyReportGenerator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ReportServiceImpl extends UnicastRemoteObject implements ReportService {

    private final EmployeeRepository      employeeRepository;
    private final LeaveRepository         leaveRepository;
    private final EmployeeReportGenerator employeeReportGenerator;
    private final MonthlyReportGenerator  monthlyReportGenerator;
    private final YearlyReportGenerator   yearlyReportGenerator;

    public ReportServiceImpl() throws RemoteException {
        super();
        employeeRepository      = new EmployeeRepository();
        leaveRepository         = new LeaveRepository();
        employeeReportGenerator = new EmployeeReportGenerator();
        monthlyReportGenerator  = new MonthlyReportGenerator();
        yearlyReportGenerator   = new YearlyReportGenerator();
    }

    /**
     * Returns all employees available for report selection.
     */
    @Override
    public List<Employee> getReportableEmployees() throws RemoteException {
        return employeeRepository.getAllEmployees();
    }

    /**
     * Generates a profile + leave history report for a single employee.
     */
    @Override
    public String generateEmployeeReport(String employeeId) throws RemoteException {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return "Error: Employee ID cannot be empty.";
        }

        Employee employee = employeeRepository.findById(employeeId.trim());

        if (employee == null) {
            return "Error: Employee with ID '" + employeeId + "' not found.";
        }

        List<LeaveApplication> leaveHistory =
                leaveRepository.findByEmployeeId(employeeId.trim());

        return employeeReportGenerator.generate(employee, leaveHistory);
    }

    /**
     * Generates a monthly report for the given month and year.
     */
    @Override
    public String generateMonthlyReport(int month, int year) throws RemoteException {
        if (month < 1 || month > 12) {
            return "Error: Month must be between 1 and 12.";
        }
        if (year < 2000 || year > 2100) {
            return "Error: Please provide a valid year (2000–2100).";
        }

        List<Employee>        employees = employeeRepository.getAllEmployees();
        List<LeaveApplication> allLeaves = leaveRepository.loadAll();

        if (employees.isEmpty()) {
            return "No employees found. Cannot generate monthly report.";
        }

        return monthlyReportGenerator.generate(employees, allLeaves, month, year);
    }

    /**
     * Generates a yearly report for the given year.
     */
    @Override
    public String generateYearlyReport(int year) throws RemoteException {
        if (year < 2000 || year > 2100) {
            return "Error: Please provide a valid year (2000–2100).";
        }

        List<Employee>        employees = employeeRepository.getAllEmployees();
        List<LeaveApplication> allLeaves = leaveRepository.loadAll();

        if (employees.isEmpty()) {
            return "No employees found. Cannot generate yearly report.";
        }

        return yearlyReportGenerator.generate(employees, allLeaves, year);
    }
}