package common.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LeaveApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    private String applicationId;
    private String employeeEmail;
    private String name;
    private String role;          // job role
    private LocalDate fromDate;   // start of leave
    private LocalDate toDate;     // end of leave
    private int amountOfDays;     // auto-calculated
    private LocalDate initialDate; // date form was submitted
    private String status;        // Approved / Pending / Declined

    public LeaveApplication() {}

    public LeaveApplication(String applicationId, String employeeEmail, String name,
                            String role, LocalDate fromDate, LocalDate toDate) {
        this.applicationId = applicationId;
        this.employeeEmail  = employeeEmail;
        this.name           = name;
        this.role           = role;
        this.fromDate       = fromDate;
        this.toDate         = toDate;
        this.amountOfDays   = (int) ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        this.initialDate    = LocalDate.now();
        this.status         = "Pending";
    }

    // ── Getters ────────────────────────────────────────────────
    public String    getApplicationId()  { return applicationId; }
    public String    getEmployeeEmail()  { return employeeEmail; }
    public String    getName()           { return name; }
    public String    getRole()           { return role; }
    public LocalDate getFromDate()       { return fromDate; }
    public LocalDate getToDate()         { return toDate; }
    public int       getAmountOfDays()   { return amountOfDays; }
    public LocalDate getInitialDate()    { return initialDate; }
    public String    getStatus()         { return status; }

    // ── Setters ────────────────────────────────────────────────
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public void setEmployeeEmail(String employeeEmail)  { this.employeeEmail  = employeeEmail; }
    public void setName(String name)                    { this.name           = name; }
    public void setRole(String role)                    { this.role           = role; }
    public void setFromDate(LocalDate fromDate)         { this.fromDate       = fromDate; }
    public void setToDate(LocalDate toDate)             { this.toDate         = toDate; }
    public void setAmountOfDays(int amountOfDays)       { this.amountOfDays   = amountOfDays; }
    public void setInitialDate(LocalDate initialDate)   { this.initialDate    = initialDate; }
    public void setStatus(String status)                { this.status         = status; }

    @Override
    public String toString() {
        return "LeaveApplication{" +
                "applicationId='" + applicationId + '\'' +
                ", employeeEmail='" + employeeEmail + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", amountOfDays=" + amountOfDays +
                ", initialDate=" + initialDate +
                ", status='" + status + '\'' +
                '}';
    }
}