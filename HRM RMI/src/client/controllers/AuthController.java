package client.controllers;
import common.interfaces.AuthService;
import common.interfaces.EmployeeService;
import common.models.Employee;
import common.models.User;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AuthController
{
    //rmi connection
    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    private static final String SERVICE_NAME = "AuthService";

    //state
    private AuthService authService;
    private User currentUser;
    private EmployeeService employeeService;

    //connection
    public void connect() throws Exception
    {
        Registry registry = LocateRegistry.getRegistry(HOST, PORT);
        authService = (AuthService) registry.lookup(SERVICE_NAME);
        System.out.println("Connected to AuthService");
    }

    //called by main menu to set employee service for profile in main menu
    public void setEmployeeService(EmployeeService employeeService)
    {
        this.employeeService = employeeService;
    }
    public String getFullName (String employeeId)
    {
        if (employeeService == null || employeeId == null) return null;
        try
        {
            Employee emp = employeeService.viewEmployeeRecord(employeeId);
            return emp != null ? emp.getFullName() : null;
        }
        catch (RemoteException e)
        {
            System.err.println("Failed to get full name: " + e.getMessage());
            return null;
        }
    }

    //LOGIN
    public String login(String email, String password)
    {
        try
        {
            User user = authService.login(email, password);
            currentUser = user;
            return null;
        }
        catch (RemoteException e)
        {
            return e.getMessage();
        }
    }

    //LOGOUT
    public String logout()
    {
        if (currentUser == null)
        {
            return "Not logged in.";
        }
        try
        {
            authService.logout(currentUser.getSessionID());
            currentUser = null;
            return null;
        }
        catch (RemoteException e)
        {
            currentUser = null; // Clear local state even if remote logout fails
            return e.getMessage();
        }
    }

    //GET PROFILE
    public User getProfile()
    {
        if(currentUser == null)
        {
            return null; // Not logged in
        }
        try
        {
            return authService.getProfile(currentUser.getSessionID()) ;
        }
        catch (RemoteException e)
        {
            System.err.println("Failed to get profile: " + e.getMessage());
            return null;
        }
        
    }

    //UPDATE PROFILE
    public String updateProfile(String newEmail, String newPassword)
    {
        if (currentUser == null)
        {
            return "Not logged in.";
        }
        try
        {
            User updatedUser = authService.updateProfile(currentUser.getSessionID(), newEmail, newPassword);
            updatedUser.setSessionID(currentUser.getSessionID()); // Preserve session ID
            currentUser = updatedUser; // Update local state with new profile
            return null;
        }
        catch (RemoteException e)
        {
            return e.getMessage();
        }
    }

    public User getUserByEmail(String targetEmail)
    {
        if (currentUser == null) return null;
        try
        {
            return authService.getUserByEmail(currentUser.getSessionID(), targetEmail);
        }
        catch (RemoteException e)
        {
            System.err.println("Failed to get user by email: " + e.getMessage());
            return null;
        }
        
    }

    //SESSION MENU HELPER
    public boolean isLoggedIn()
    {
        return currentUser != null;
    }

    public boolean isHR()
    {
        return currentUser != null && currentUser.isHR();
    }

    public User getCurrentUser()
    {
        return currentUser;
    }
}