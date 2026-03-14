package client.menus;
import client.controllers.AuthController;
import common.models.User;
import java.util.Scanner;

public class MainMenu
{
    private final AuthController authController;
    private final Scanner scanner;

    public MainMenu(AuthController authController, Scanner scanner)
    {
        this.authController = authController;
        this.scanner = scanner;
    }

    public boolean show()
    {
        while (true)
        {
            printHeader();
            String choice = scanner.nextLine().trim();
            switch (choice)
            {
                //profile
                case "1": doViewProfile();   break;
                case "2": doUpdateProfile(); break;
                //HR
                //for others :DDD option
                /*
                put yo cases here for other options like view employees, manage employees, etc. depending on the role of the user (HR, Manager, Employee) and delete this comment/placeholder
                */
                //session logout stuff
                case "9": if (doLogout()) return true; break; //back to main login menu
                case "10": doLogout(); return false; //exit application
                default: printError("Invalid choice! Please try again.");
            }
        }
    }

    //view profile
    private void doViewProfile()
    {
        System.out.println();
        System.out.println("=== Your Profile ===");

        User user = authController.getProfile();
        if (user == null) {
            printError("Could not retrieve profile. Please try again.");
            return;
        }
        String fullName = authController.getFullName(user.getEmployeeId());

        System.out.println("  Employee ID : " + user.getEmployeeId());
        System.out.println("  Full Name   : " + (fullName.isEmpty() ? "(Name not set)" : fullName));
        System.out.println("  Email  : " + user.getEmail());
        System.out.println("  Role   : " + user.getRole());
        System.out.println("  Access : " + (user.isHR() ? "HR" : "Employee"));
        System.out.println();
        pause();
    }

    //update profile
private void doUpdateProfile()
{
        System.out.println();
        System.out.println("=== Update Your Profile ===");
        System.out.println("  (Press ENTER to keep the current value)");
        System.out.println();

        System.out.print("  New Email    : ");
        String newEmail = scanner.nextLine().trim();

        System.out.print("  New Password : ");
        String newPassword = scanner.nextLine().trim();

        // Pass null for blank fields — controller/server will ignore them
        String error = authController.updateProfile(
                newEmail.isEmpty()    ? null : newEmail,
                newPassword.isEmpty() ? null : newPassword
        );

        if (error == null)
        {
            User updated = authController.getCurrentUser();
            String fullName = authController.getFullName(updated.getEmployeeId());
            System.out.println();
            System.out.println("  ✔  Profile updated successfully!");
            System.out.println("     Employee ID : " + updated.getEmployeeId());
            System.out.println("     Full Name   : " + (fullName.isEmpty() ? "(Name not set)" : fullName));
            System.out.println("     Email : " + updated.getEmail());
            System.out.println("     Role  : " + updated.getRole());

            // If email was changed the server issued a new session — notify user
            System.out.println();
        }
        else
        {
            printError(error);
        }
        pause();
    }

    private boolean doLogout()
    {
        String name = authController.getCurrentUser() != null
                ? authController.getCurrentUser().getEmployeeId() : "User";
        String fullName = authController.getFullName(name);
        String displayName = fullName.isEmpty() ? "[" + name + "]" : fullName;
        String error = authController.logout();
        if (error == null) {
            System.out.println();
            System.out.println("  ✔  Logged out. Goodbye, " + displayName + "!");
            System.out.println();
            pause();
            return true;
        }
        else
        {
            printError("Logout error: " + error);
            return false;
        }
    }

    //UI
    private void printHeader()
    {
        System.out.println();
        System.out.println("=== Main Menu ===");
        System.out.println("1. View Profile");
        System.out.println("2. Update Profile");
        if (authController.isHR())
        {
            System.out.println("3. View Employees");
            //add more HR options here
        }
        //add more role-based options here
        System.out.println("9. Logout");
        System.out.println("10. Exit Application");
        System.out.print("Enter your choice: ");
    }

    private void printError(String message)
    {
        System.out.println();
        System.out.println("  ✖  " + message);
        System.out.println();
    }
    private void pause()
    {
        System.out.println("Press ENTER to continue...");
        scanner.nextLine();
    }
}