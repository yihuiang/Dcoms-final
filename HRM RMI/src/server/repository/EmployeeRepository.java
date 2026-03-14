package server.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import common.models.Employee;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository {
    private static final String FILE_PATH = "HRM RMI/data/employees.json";
    private final Gson gson;

    public EmployeeRepository() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        initializeFile();
    }

    private void initializeFile() {
        try {
            File file = new File(FILE_PATH);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                FileWriter writer = new FileWriter(file);
                writer.write("[]");
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Employee> getAllEmployees() {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<List<Employee>>() {}.getType();
            List<Employee> employees = gson.fromJson(reader, listType);

            if (employees == null) {
                return new ArrayList<>();
            }
            return employees;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean saveAllEmployees(List<Employee> employees) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(employees, writer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Employee findById(String employeeId) {
        List<Employee> employees = getAllEmployees();
        for (Employee employee : employees) {
            if (employee.getEmployeeId().equalsIgnoreCase(employeeId)) {
                return employee;
            }
        }
        return null;
    }

    public Employee findByEmail(String email) {
        List<Employee> employees = getAllEmployees();
        for (Employee employee : employees) {
            if (employee.getEmail().equalsIgnoreCase(email)) {
                return employee;
            }
        }
        return null;
    }

    public boolean addEmployee(Employee newEmployee) {
        List<Employee> employees = getAllEmployees();
        employees.add(newEmployee);
        return saveAllEmployees(employees);
    }

    public boolean updateEmployee(Employee updatedEmployee) {
        List<Employee> employees = getAllEmployees();

        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmployeeId().equalsIgnoreCase(updatedEmployee.getEmployeeId())) {
                employees.set(i, updatedEmployee);
                return saveAllEmployees(employees);
            }
        }
        return false;
    }

    public boolean deleteEmployee(String employeeId) {
        List<Employee> employees = getAllEmployees();
        boolean removed = employees.removeIf(employee ->
                employee.getEmployeeId().equalsIgnoreCase(employeeId)
        );

        if (removed) {
            return saveAllEmployees(employees);
        }
        return false;
    }

    public int getTotalLeaveBalance(String employeeId) {
        Employee employee = findById(employeeId);
        return (employee != null) ? employee.getLeaveDays() : 14; // default 14 days
    }

}