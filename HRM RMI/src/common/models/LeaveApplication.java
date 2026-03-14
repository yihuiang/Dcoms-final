package common.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LeaveApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    // From employees.json (lookup only, not stored in leave_requests.json)
    private String    employeeId;

    // Stored in leave_requests.json
    private String    applicationId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int       amountOfDays;   // auto-calculated
    private LocalDate appliedDate;    // date form was submitted (initialDate)
    private String    status;         // Pending / Approved / Declined

    public LeaveApplication() {}

    public LeaveApplication(String applicationId, String employeeId,
                            LocalDate fromDate, LocalDate toDate) {
        this.applicationId = applicationId;
        this.employeeId    = employeeId;   // looked up from employees.json
        this.fromDate      = fromDate;
        this.toDate        = toDate;
        this.amountOfDays  = (int) ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        this.appliedDate   = LocalDate.now(); // auto set on submission
        this.status        = "Pending";
    }

    // ── Getters ────────────────────────────────────────────────
    public String    getApplicationId() { return applicationId; }
    public String    getEmployeeId()    { return employeeId; }
    public LocalDate getFromDate()      { return fromDate; }
    public LocalDate getToDate()        { return toDate; }
    public int       getAmountOfDays()  { return amountOfDays; }
    public LocalDate getInitialDate()   { return appliedDate; }   // appliedDate = initialDate
    public String    getStatus()        { return status; }

    // ── Setters ────────────────────────────────────────────────
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public void setEmployeeId(String employeeId)       { this.employeeId    = employeeId; }
    public void setFromDate(LocalDate fromDate)         { this.fromDate      = fromDate; }
    public void setToDate(LocalDate toDate)             { this.toDate        = toDate; }
    public void setAmountOfDays(int amountOfDays)       { this.amountOfDays  = amountOfDays; }
    public void setInitialDate(LocalDate initialDate)   { this.appliedDate   = initialDate; }
    public void setStatus(String status)                { this.status        = status; }

    @Override
    public String toString() {
        return "LeaveApplication{" +
                "applicationId='" + applicationId + '\'' +
                ", employeeId='"  + employeeId    + '\'' +
                ", fromDate="     + fromDate       +
                ", toDate="       + toDate         +
                ", amountOfDays=" + amountOfDays   +
                ", appliedDate="  + appliedDate    +
                ", status='"      + status         + '\'' +
                '}';
    }
}