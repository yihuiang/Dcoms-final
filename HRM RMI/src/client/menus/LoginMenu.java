package client.menus;
import client.controllers.AuthController;
import java.util.Scanner;

public class LoginMenu
{
    private final AuthController authController;
    private final Scanner scanner;

    public LoginMenu(AuthController authController, Scanner scanner)
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
                case "1":
                    if(doLogin()) return true;
                    break;
                case "0":
                    return false;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private boolean doLogin()
    {
        System.out.println();
        System.out.print(" ------------------------LOGIN------------------------\n");
        System.out.print(" Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print(" Enter Password: ");
        String password = scanner.nextLine().trim();

        //send to controller
        String error = authController.login(email, password);
        if (error == null)
        {
            System.out.println();
            System.out.println("  ✔  Welcome, " + authController.getCurrentUser().getName() + "!");
            System.out.println("     Role   : " + authController.getCurrentUser().getRole());
            System.out.println("     Access : " + (authController.isHR() ? "HR" : "Employee"));
            System.out.println();
            pause();
            return true;
        }
        else
        {
            printError(error);
            return false;
        }
    }

    //UI
    private void printHeader()
    {
        System.out.println();
        System.out.println(" ------------------------LOGIN------------------------");
        System.out.println(" 1. Login");
        System.out.println(" 0. Exit");
        System.out.print(" Enter choice: ");
    }
    //pause
    private void pause()
    {
        System.out.println(" Press Enter to continue...");
        scanner.nextLine();
    }
}