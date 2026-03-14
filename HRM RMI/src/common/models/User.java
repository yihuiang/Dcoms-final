package common.models;
import java.io.Serializable;

//User -- for profile authentication and profile data

public class User implements Serializable
{
    private static final long serialVersionUID = 1L; //uid serialization

    //Identifier ID
    private String employeeId;

    //login credentials
    private String email;
    private String password; //for security, will be stored as SHA-256 hex hash

    //for profile
    private String role; //HR or Employee

    //session ID
    private transient String sessionID;

    //CONSTRUCTOR
    public User(){}
    public User(String employeeId, String email, String password, String role)
    {
        this.employeeId = employeeId;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    //ROLE HR DETECT
    /* only returns true if user has HR role access*/
    public boolean isHR()
    {
        if (role == null) return false;
        String upper = role.toUpperCase();
        return upper.contains("HR") || upper.contains("HUMAN RESOURCE");
    }

    //GETTERS AND SETTERS
    public String getEmployeeId()
    {
        return employeeId;
    }
    public void setEmployeeId(String employeeId)
    {
        this.employeeId = employeeId;
    }

    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getRole()
    {
        return role;
    }
    public void setRole(String role)
    {
        this.role = role;
    }

    public String getSessionID()
    {
        return sessionID;
    }
    public void setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
    }

    //To String, for if user has int or other data types in the future
    @Override
    public String toString()
    {
        return "User{employeeId='" + employeeId + "', email='" + email + "', role='" + role + "', isHR=" + isHR() + "'}";
    }
    
}