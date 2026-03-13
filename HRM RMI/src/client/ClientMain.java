package client;

import common.interfaces.EmployeeService;
import common.models.Employee;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class ClientMain {

    private static EmployeeService employeeService;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            employeeService = (EmployeeService) registry.lookup("EmployeeService");

            showHRMainMenu();

        } catch (Exception e) {
            System.out.println("Failed to connect to server.");
            e.printStackTrace();
        }
    }

    private static void showHRMainMenu() {
        while (true) {
            try {
                System.out.println("\n====== HR MANAGEMENT MENU ======");
                System.out.println("1. Register Employee");
                System.out.println("2. Validate Employee Detail");
                System.out.println("3. Employee Record");
                System.out.println("0. Exit");
                System.out.print("Select option: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        registerEmployee();
                        break;
                    case "2":
                        validateEmployee();
                        break;
                    case "3":
                        employeeRecordMenu();
                        break;
                    case "0":
                        System.out.println("Exiting system...");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }

            } catch (Exception e) {
                System.out.println("An error occurred in the HR main menu.");
                e.printStackTrace();
            }
        }
    }

    private static void registerEmployee() {
        try {
            System.out.println("\n=== Register Employee ===");

            String suggestedId = employeeService.getNextEmployeeId();
            String id;

            while (true) {
                System.out.print("Employee ID (press Enter to use " + suggestedId + "): ");
                id = scanner.nextLine().trim();

                if (id.isEmpty()) {
                    id = suggestedId;
                    break;
                }

                if (!id.matches("E\\d{3}")) {
                    System.out.println("Invalid Employee ID format.");
                    System.out.println("Next available ID: " + suggestedId);
                    continue;
                }

                break;
            }

            String name;
            while (true) {
                System.out.print("Full Name: ");
                name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("Full name cannot be empty.");
                } else {
                    break;
                }
            }

            String email;
            while (true) {
                System.out.print("Email: ");
                email = scanner.nextLine().trim();
                if (email.isEmpty() || !email.contains("@")) {
                    System.out.println("Invalid email format.");
                } else {
                    break;
                }
            }

            String phone;
            while (true) {
                System.out.print("Phone: ");
                phone = scanner.nextLine().trim();
                if (phone.isEmpty()) {
                    System.out.println("Phone cannot be empty.");
                } else {
                    break;
                }
            }

            String department;
            while (true) {
                System.out.print("Department: ");
                department = scanner.nextLine().trim();
                if (department.isEmpty()) {
                    System.out.println("Department cannot be empty.");
                } else {
                    break;
                }
            }

            String position;
            while (true) {
                System.out.print("Position: ");
                position = scanner.nextLine().trim();
                if (position.isEmpty()) {
                    System.out.println("Position cannot be empty.");
                } else {
                    break;
                }
            }

            double salary;
            while (true) {
                try {
                    System.out.print("Salary (RM): ");
                    salary = Double.parseDouble(scanner.nextLine().trim());
                    if (salary <= 0) {
                        System.out.println("Salary must be greater than 0.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid salary. Please enter a numeric value.");
                }
            }

            int leaveDays;
            while (true) {
                try {
                    System.out.print("Leave Days: ");
                    leaveDays = Integer.parseInt(scanner.nextLine().trim());
                    if (leaveDays < 0) {
                        System.out.println("Leave days cannot be negative.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid leave days. Please enter a whole number.");
                }
            }

            Employee employee = new Employee(id, name, email, phone, department, position, salary, leaveDays);

            boolean result = employeeService.registerEmployee(employee);

            if (result) {
                System.out.println("Employee registered successfully.");
                System.out.println("Current status: PENDING");
            } else {
                System.out.println("Registration failed.");
                System.out.println("Possible reasons:");
                System.out.println("- Duplicate employee ID");
                System.out.println("- Duplicate email");
                System.out.println("- Invalid employee details");
            }

        } catch (Exception e) {
            System.out.println("An error occurred while registering employee.");
            e.printStackTrace();
        }
    }

    private static void validateEmployee() {
        try {
            while (true) {
                System.out.println("\n=== Pending Employees ===");

                List<Employee> list = employeeService.getPendingEmployees();

                if (list == null || list.isEmpty()) {
                    System.out.println("No pending employees.");
                    return;
                }

                for (Employee e : list) {
                    System.out.println(e.getEmployeeId() + " - " + e.getFullName());
                }

                System.out.println("\nEnter Employee ID to validate");
                System.out.println("Or type 0 to return to main menu");
                System.out.print("Employee ID: ");
                String id = scanner.nextLine().trim();

                if (id.equals("0")) {
                    return;
                }

                Employee selectedEmployee = null;
                for (Employee e : list) {
                    if (e.getEmployeeId().equalsIgnoreCase(id)) {
                        selectedEmployee = e;
                        break;
                    }
                }

                if (selectedEmployee == null) {
                    System.out.println("Invalid ID, please try again.");
                    continue;
                }

                System.out.println("\nSelected Employee:");
                System.out.println(selectedEmployee);

                System.out.println("\n1. Approve");
                System.out.println("2. Reject");
                System.out.println("3. Exit");
                System.out.print("Choose option: ");

                String option = scanner.nextLine().trim();

                if (option.equals("1")) {
                    boolean approved = employeeService.approveEmployee(id);
                    if (approved) {
                        System.out.println("Employee approved.");
                    } else {
                        System.out.println("Failed to approve employee.");
                    }
                } else if (option.equals("2")) {
                    boolean rejected = employeeService.rejectEmployee(id);
                    if (rejected) {
                        System.out.println("Employee rejected.");
                    } else {
                        System.out.println("Failed to reject employee.");
                    }
                } else if (option.equals("3")) {
                    return;
                } else {
                    System.out.println("Invalid option.");
                }
            }

        } catch (Exception e) {
            System.out.println("An error occurred while validating employee.");
            e.printStackTrace();
        }
    }

    private static void employeeRecordMenu() {
        while (true) {
            try {
                System.out.println("\n========== EMPLOYEE RECORD ==========");

                List<Employee> employees = employeeService.viewAllEmployeeRecords();

                if (employees == null || employees.isEmpty()) {
                    System.out.println("No approved employee records found.");
                    return;
                }

                displayEmployeeRecords(employees);

                System.out.println("\nOptions:");
                System.out.println("1. Update Employee");
                System.out.println("2. Delete Employee");
                System.out.println("3. Back to HR Main Menu");
                System.out.print("Select option: ");

                String input = scanner.nextLine().trim();

                switch (input) {
                    case "1":
                        updateEmployeeRecord();
                        break;
                    case "2":
                        deleteEmployeeRecord();
                        break;
                    case "3":
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }

            } catch (Exception e) {
                System.out.println("An error occurred while accessing employee records.");
                e.printStackTrace();
            }
        }
    }

    private static void displayEmployeeRecords(List<Employee> employees) {
        System.out.println("===============================================================================================================================");
        System.out.printf("%-10s %-20s %-25s %-15s %-12s %-20s %-12s %-10s%n",
                "ID", "Name", "Email", "Phone", "Dept", "Position", "Salary", "Leave");
        System.out.println("===============================================================================================================================");

        for (Employee e : employees) {
            System.out.printf("%-10s %-20s %-25s %-15s %-12s %-20s RM%-10.2f %-10d%n",
                    e.getEmployeeId(),
                    e.getFullName(),
                    e.getEmail(),
                    e.getPhone(),
                    e.getDepartment(),
                    e.getPosition(),
                    e.getSalary(),
                    e.getLeaveDays());
        }

        System.out.println("===============================================================================================================================");
    }

    private static void updateEmployeeRecord() {
        try {
            while (true) {
                System.out.print("\nEnter Employee ID to update (or 0 to cancel): ");
                String employeeId = scanner.nextLine().trim();

                if (employeeId.equals("0")) {
                    return;
                }

                Employee employee = employeeService.viewEmployeeRecord(employeeId);

                if (employee == null) {
                    System.out.println("Invalid employee ID. Please try again.");
                    continue;
                }

                System.out.println("\nSelected Employee:");
                System.out.println(employee);

                while (true) {
                    System.out.println("\nWhich field would you like to update?");
                    System.out.println("1. Full Name");
                    System.out.println("2. Email");
                    System.out.println("3. Phone");
                    System.out.println("4. Department");
                    System.out.println("5. Position");
                    System.out.println("6. Salary");
                    System.out.println("7. Leave Days");
                    System.out.println("8. Save and Exit");
                    System.out.print("Select option: ");

                    String choice = scanner.nextLine().trim();

                    switch (choice) {
                        case "1":
                            System.out.print("Enter new full name: ");
                            String fullName = scanner.nextLine().trim();
                            if (fullName.isEmpty()) {
                                System.out.println("Full name cannot be empty.");
                            } else {
                                employee.setFullName(fullName);
                                System.out.println("Full name updated.");
                            }
                            break;

                        case "2":
                            System.out.print("Enter new email: ");
                            String email = scanner.nextLine().trim();
                            if (email.isEmpty() || !email.contains("@")) {
                                System.out.println("Invalid email format.");
                            } else {
                                employee.setEmail(email);
                                System.out.println("Email updated.");
                            }
                            break;

                        case "3":
                            System.out.print("Enter new phone: ");
                            String phone = scanner.nextLine().trim();
                            if (phone.isEmpty()) {
                                System.out.println("Phone cannot be empty.");
                            } else {
                                employee.setPhone(phone);
                                System.out.println("Phone updated.");
                            }
                            break;

                        case "4":
                            System.out.print("Enter new department: ");
                            String department = scanner.nextLine().trim();
                            if (department.isEmpty()) {
                                System.out.println("Department cannot be empty.");
                            } else {
                                employee.setDepartment(department);
                                System.out.println("Department updated.");
                            }
                            break;

                        case "5":
                            System.out.print("Enter new position: ");
                            String position = scanner.nextLine().trim();
                            if (position.isEmpty()) {
                                System.out.println("Position cannot be empty.");
                            } else {
                                employee.setPosition(position);
                                System.out.println("Position updated.");
                            }
                            break;

                        case "6":
                            try {
                                System.out.print("Enter new salary (RM): ");
                                double salary = Double.parseDouble(scanner.nextLine().trim());
                                if (salary <= 0) {
                                    System.out.println("Salary must be greater than 0.");
                                } else {
                                    employee.setSalary(salary);
                                    System.out.println("Salary updated.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid salary. Please enter a numeric value.");
                            }
                            break;

                        case "7":
                            try {
                                System.out.print("Enter new leave days: ");
                                int leaveDays = Integer.parseInt(scanner.nextLine().trim());
                                if (leaveDays < 0) {
                                    System.out.println("Leave days cannot be negative.");
                                } else {
                                    employee.setLeaveDays(leaveDays);
                                    System.out.println("Leave days updated.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid leave days. Please enter a whole number.");
                            }
                            break;

                        case "8":
                            boolean updated = employeeService.updateEmployeeRecord(employee);
                            if (updated) {
                                System.out.println("Employee record updated successfully.");
                            } else {
                                System.out.println("Failed to update employee record.");
                            }
                            return;

                        default:
                            System.out.println("Invalid option. Please try again.");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("An error occurred while updating employee record.");
            e.printStackTrace();
        }
    }

    private static void deleteEmployeeRecord() {
        try {
            while (true) {
                System.out.print("\nEnter Employee ID to delete (or 0 to cancel): ");
                String employeeId = scanner.nextLine().trim();

                if (employeeId.equals("0")) {
                    return;
                }

                Employee employee = employeeService.viewEmployeeRecord(employeeId);

                if (employee == null) {
                    System.out.println("Invalid employee ID. Please try again.");
                    continue;
                }

                System.out.println("\nEmployee to delete:");
                System.out.println(employee);

                System.out.print("\nAre you sure you want to delete this employee? (yes/no): ");
                String confirm = scanner.nextLine().trim();

                if (!confirm.equalsIgnoreCase("yes")) {
                    System.out.println("Delete cancelled.");
                    return;
                }

                System.out.print("Please type DELETE to confirm: ");
                String secondConfirm = scanner.nextLine().trim();

                if (!secondConfirm.equals("DELETE")) {
                    System.out.println("Delete cancelled. Confirmation text did not match.");
                    return;
                }

                boolean deleted = employeeService.deleteEmployeeRecord(employeeId);

                if (deleted) {
                    System.out.println("Employee record deleted successfully.");
                } else {
                    System.out.println("Failed to delete employee record.");
                }
                return;
            }

        } catch (Exception e) {
            System.out.println("An error occurred while deleting employee record.");
            e.printStackTrace();
        }
    }
}