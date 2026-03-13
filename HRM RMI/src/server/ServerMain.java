package server;

import common.interfaces.EmployeeService;
import server.implementation.EmployeeServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {

    public static void main(String[] args) {
        try {

            Registry registry = LocateRegistry.createRegistry(1099);

            EmployeeService employeeService = new EmployeeServiceImpl();

            registry.rebind("EmployeeService", employeeService);

            System.out.println("Server started...");
            System.out.println("EmployeeService ready.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}