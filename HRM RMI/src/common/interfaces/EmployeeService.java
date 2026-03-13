package common.interfaces;

import common.models.Employee;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface EmployeeService extends Remote {
    boolean validateEmployeeDetail(Employee employee) throws RemoteException;
    String registerEmployee(Employee employee) throws RemoteException;
    String getNextEmployeeId() throws RemoteException;
    List<Employee> getPendingEmployees() throws RemoteException;
    boolean approveEmployee(String employeeId) throws RemoteException;
    boolean rejectEmployee(String employeeId) throws RemoteException;
    Employee viewEmployeeRecord(String employeeId) throws RemoteException;
    List<Employee> viewAllEmployeeRecords() throws RemoteException;
    boolean updateEmployeeRecord(Employee employee) throws RemoteException;
    boolean deleteEmployeeRecord(String employeeId) throws RemoteException;
}