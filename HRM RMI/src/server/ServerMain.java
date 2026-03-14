package server;

import common.interfaces.EmployeeService;
import common.interfaces.ReportService;
import server.implementation.EmployeeServiceImpl;
import server.implementation.ReportServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {

    public static void main(String[] args) {
        try {

            Registry registry = LocateRegistry.createRegistry(2099);

            EmployeeService employeeService = new EmployeeServiceImpl();
            ReportService   reportService   = new ReportServiceImpl();

            registry.rebind("EmployeeService", employeeService);
            registry.rebind("ReportService",   reportService);

            System.out.println("Server started...");
            System.out.println("EmployeeService ready.");
            System.out.println("ReportService   ready.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}