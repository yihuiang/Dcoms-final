package common.interfaces;

import common.models.Employee;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ReportService extends Remote {

    /**
     * Generates a full profile + leave history report for a single employee.
     * @param employeeId the ID of the employee to report on
     * @return formatted report string, or an error message if not found
     */
    String generateEmployeeReport(String employeeId) throws RemoteException;

    /**
     * Returns list of all approved employees for selection UI.
     */
    List<Employee> getReportableEmployees() throws RemoteException;

    /**
     * Generates a monthly report covering leave and payroll data.
     * @param month 1–12
     * @param year  e.g. 2025
     * @return formatted report string
     */
    String generateMonthlyReport(int month, int year) throws RemoteException;

    /**
     * Generates a yearly report covering leave and payroll data.
     * @param year e.g. 2025
     * @return formatted report string
     */
    String generateYearlyReport(int year) throws RemoteException;
}