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

        if (isNullOrEmpty(employee.getEmployeeId()) || !employee.getEmployeeId().matches("E\\d{3}")) {
            return false;
        }

        if (isNullOrEmpty(employee.getFullName()) || !employee.getFullName().matches("[A-Za-z ]+")) {
            return false;
        }

        if (isNullOrEmpty(employee.getEmail()) ||
                !employee.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return false;
        }

        if (isNullOrEmpty(employee.getPhone()) || !employee.getPhone().matches("\\d+")) {
            return false;
        }

        if (isNullOrEmpty(employee.getDepartment()) || !employee.getDepartment().matches("[A-Za-z ]+")) {
            return false;
        }

        if (isNullOrEmpty(employee.getPosition()) || !employee.getPosition().matches("[A-Za-z ]+")) {
            return false;
        }

        if (employee.getSalary() <= 0) {
            return false;
        }

        if (employee.getLeaveDays() < 0 || employee.getLeaveDays() > 30) {
            return false;
        }

        return true;
    }

    @Override
    public String registerEmployee(Employee employee) throws RemoteException {
        if (employee == null) {
            return "Employee data cannot be empty.";
        }

        if (isNullOrEmpty(employee.getEmployeeId()) || !employee.getEmployeeId().matches("E\\d{3}")) {
            return "Invalid Employee ID format. Example: E001.";
        }

        if (isNullOrEmpty(employee.getFullName()) || !employee.getFullName().matches("[A-Za-z .'-]+")) {
            return "Full name cannot contain numbers or invalid special characters.";
        }

        if (isNullOrEmpty(employee.getEmail()) ||
                !employee.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Invalid email format.";
        }

        if (isNullOrEmpty(employee.getPhone()) || !employee.getPhone().matches("\\d+")) {
            return "Phone number must contain numbers only.";
        }

        if (isNullOrEmpty(employee.getDepartment()) || !employee.getDepartment().matches("[A-Za-z ]+")) {
            return "Department cannot contain numbers or special characters.";
        }

        if (isNullOrEmpty(employee.getPosition()) || !employee.getPosition().matches("[A-Za-z ]+")) {
            return "Position cannot contain numbers or special characters.";
        }

        if (employee.getSalary() <= 0) {
            return "Salary must be greater than 0.";
        }

        if (employee.getLeaveDays() < 0 || employee.getLeaveDays() > 30) {
            return "Leave days must be between 0 and 30.";
        }

        if (employeeRepository.findById(employee.getEmployeeId()) != null) {
            return "Employee ID already exists.";
        }

        if (employeeRepository.findByEmail(employee.getEmail()) != null) {
            return "Email already exists.";
        }

        employee.setStatus("PENDING");
        boolean saved = employeeRepository.addEmployee(employee);

        if (saved) {
            return "Employee registered successfully. Current status: PENDING";
        } else {
            return "Failed to save employee record.";
        }
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