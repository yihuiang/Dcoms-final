package server;

import common.interfaces.AuthService;
import common.interfaces.EmployeeService;
import common.interfaces.ReportService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import server.implementation.AuthServiceImpl;
import server.implementation.EmployeeServiceImpl;
import server.implementation.ReportServiceImpl;

public class ServerMain {

    public static void main(String[] args) {
        try {

            Registry registry = LocateRegistry.createRegistry(2099);

            EmployeeService employeeService = new EmployeeServiceImpl();
            ReportService   reportService   = new ReportServiceImpl();
            AuthService     authService     = new AuthServiceImpl();

            registry.rebind("EmployeeService", employeeService);
            registry.rebind("ReportService",   reportService);
            registry.rebind("AuthService", authService );

            System.out.println("Server started...");
            System.out.println("EmployeeService ready.");
            System.out.println("ReportService   ready.");
            System.out.println("AuthService     ready.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}