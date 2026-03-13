package common.models;

import java.io.Serializable;

public class Employee implements Serializable {
    private String employeeId;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private String position;
    private double salary;
    private int leaveDays;
    private String status;


    public Employee() {
    }

    public Employee(String employeeId, String fullName, String email, String phone,
                    String department, String position, double salary, int leaveDays) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.position = position;
        this.salary = salary;
        this.leaveDays = leaveDays;
        this.status = "PENDING";
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public int getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(int leaveDays) {
        this.leaveDays = leaveDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Employee ID: " + employeeId +
                "\nFull Name: " + fullName +
                "\nEmail: " + email +
                "\nPhone: " + phone +
                "\nDepartment: " + department +
                "\nPosition: " + position +
                "\nSalary (RM): " + salary +
                "\nLeave Balance (Days): " + leaveDays;
    }
}