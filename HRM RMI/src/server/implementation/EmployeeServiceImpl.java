package server.implementation;

import common.interfaces.EmployeeService;
import common.models.Employee;
import server.repository.EmployeeRepository;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class EmployeeServiceImpl extends UnicastRemoteObject implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl() throws RemoteException {
        super();
        employeeRepository = new EmployeeRepository();
    }

    @Override
    public boolean validateEmployeeDetail(Employee employee) throws RemoteException {
        if (employee == null) {
            return false;
        }

        if (isNullOrEmpty(employee.getEmployeeId())) {
            return false;
        }

        if (isNullOrEmpty(employee.getFullName())) {
            return false;
        }

        if (isNullOrEmpty(employee.getEmail()) || !employee.getEmail().contains("@")) {
            return false;
        }

        if (isNullOrEmpty(employee.getPhone())) {
            return false;
        }

        if (isNullOrEmpty(employee.getDepartment())) {
            return false;
        }

        if (isNullOrEmpty(employee.getPosition())) {
            return false;
        }

        if (employee.getSalary() <= 0) {
            return false;
        }

        if (employee.getLeaveDays() < 0) {
            return false;
        }

        return true;
    }

    @Override
    public boolean registerEmployee(Employee employee) throws RemoteException {
        if (employee == null) {
            return false;
        }

        if (!validateEmployeeDetail(employee)) {
            return false;
        }

        if (employeeRepository.findById(employee.getEmployeeId()) != null) {
            return false;
        }

        if (employeeRepository.findByEmail(employee.getEmail()) != null) {
            return false;
        }

        employee.setStatus("PENDING");
        return employeeRepository.addEmployee(employee);
    }

    @Override
    public List<Employee> getPendingEmployees() throws RemoteException {
        List<Employee> employees = employeeRepository.getAllEmployees();
        List<Employee> pendingEmployees = new ArrayList<>();

        for (Employee employee : employees) {
            if ("PENDING".equalsIgnoreCase(employee.getStatus())) {
                pendingEmployees.add(employee);
            }
        }

        return pendingEmployees;
    }

    @Override
    public String getNextEmployeeId() throws RemoteException {
        List<Employee> employees = employeeRepository.getAllEmployees();
        int max = 0;

        for (Employee employee : employees) {
            String id = employee.getEmployeeId();

            if (id != null && id.matches("E\\d{3}")) {
                int number = Integer.parseInt(id.substring(1));
                if (number > max) {
                    max = number;
                }
            }
        }

        return String.format("E%03d", max + 1);
    }

    @Override
    public boolean approveEmployee(String employeeId) throws RemoteException {
        if (isNullOrEmpty(employeeId)) {
            return false;
        }

        Employee employee = employeeRepository.findById(employeeId);

        if (employee == null) {
            return false;
        }

        if (!"PENDING".equalsIgnoreCase(employee.getStatus())) {
            return false;
        }

        employee.setStatus("APPROVED");
        return employeeRepository.updateEmployee(employee);
    }

    @Override
    public boolean rejectEmployee(String employeeId) throws RemoteException {
        if (isNullOrEmpty(employeeId)) {
            return false;
        }

        Employee employee = employeeRepository.findById(employeeId);

        if (employee == null) {
            return false;
        }

        if (!"PENDING".equalsIgnoreCase(employee.getStatus())) {
            return false;
        }

        employee.setStatus("REJECTED");
        return employeeRepository.updateEmployee(employee);
    }

    @Override
    public Employee viewEmployeeRecord(String employeeId) throws RemoteException {
        if (isNullOrEmpty(employeeId)) {
            return null;
        }

        Employee employee = employeeRepository.findById(employeeId);

        if (employee != null && "APPROVED".equalsIgnoreCase(employee.getStatus())) {
            return employee;
        }

        return null;
    }

    @Override
    public List<Employee> viewAllEmployeeRecords() throws RemoteException {
        List<Employee> employees = employeeRepository.getAllEmployees();
        List<Employee> approvedEmployees = new ArrayList<>();

        for (Employee employee : employees) {
            if ("APPROVED".equalsIgnoreCase(employee.getStatus())) {
                approvedEmployees.add(employee);
            }
        }

        return approvedEmployees;
    }

    @Override
    public boolean updateEmployeeRecord(Employee employee) throws RemoteException {
        if (employee == null) {
            return false;
        }

        if (!validateEmployeeDetail(employee)) {
            return false;
        }

        Employee existingEmployee = employeeRepository.findById(employee.getEmployeeId());

        if (existingEmployee == null) {
            return false;
        }

        if (!"APPROVED".equalsIgnoreCase(existingEmployee.getStatus())) {
            return false;
        }

        Employee employeeWithSameEmail = employeeRepository.findByEmail(employee.getEmail());
        if (employeeWithSameEmail != null &&
                !employeeWithSameEmail.getEmployeeId().equalsIgnoreCase(employee.getEmployeeId())) {
            return false;
        }

        employee.setStatus(existingEmployee.getStatus());
        return employeeRepository.updateEmployee(employee);
    }

    @Override
    public boolean deleteEmployeeRecord(String employeeId) throws RemoteException {
        if (isNullOrEmpty(employeeId)) {
            return false;
        }

        Employee existingEmployee = employeeRepository.findById(employeeId);

        if (existingEmployee == null) {
            return false;
        }

        if (!"APPROVED".equalsIgnoreCase(existingEmployee.getStatus())) {
            return false;
        }

        return employeeRepository.deleteEmployee(employeeId);
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}