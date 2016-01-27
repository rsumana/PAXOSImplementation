/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_Coordinator;

import Logger.CoordinatorLogger;
import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import ClientOperations.CoordinatorImpl;

/**
 *
 * @author sumanaravikrishnan
 */
public class RMI_Coordinator 
{

        /**
     * @param args the command line arguments
     */
    
    public static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) 
    {
        new CoordinatorLogger();
        createCoordinatorSocket();
    }
    
    //Get port from user
    private static int getPort()
    {
        int port = 0;
        
        boolean isAvailable = false;
        do
        {
            System.out.print("Enter coordinator server port number: ");
            String portClient = sc.next();
            try
            {
                port = Integer.valueOf(portClient);
                if(port>=0 && port<=65535)
                {
                    isAvailable = testServerPort(port);
                }
                if(!isAvailable)
                {
                    System.out.println("PORT NOT AVAILBLE. Please enter new port");
                }
            }
            catch(Exception e)
            {
                System.out.println("PORT NOT VALID. Please enter new port");
            }
        }while(!isAvailable);
        return port;
    }
    
    //Testing port availability
    private static boolean testServerPort(int port) 
    {
        boolean isAvailable = false;
        ServerSocket test = null;
        CoordinatorLogger.logEntry("INFO", "Testing if port "+port+" is occupied");
        try
        {
            //If server socket can be created on a port, then its available
            test = new ServerSocket(port);
            isAvailable = true;
            CoordinatorLogger.logEntry("INFO", port+" is available");
        }
        catch(Exception e)
        {
            //Server socket cannot be created. Some other application is using the port
            CoordinatorLogger.logEntry("INFO", port+" not available");
        }
        finally
        {
            //Closing the test connection
            if(test != null)
            {
                try
                {
                    test.close();
                }
                catch(IOException e)
                {
                    CoordinatorLogger.logEntry("INFO", port+" is not available. Not able to close test socket");
                    isAvailable = false;
                }
            }
        }
        return isAvailable;
    }

    private static void createCoordinatorSocket() 
    {
        try
        {
            System.out.println("\nCOORDINATOR SERVER MACHINE\n-------------------------\n");
            int port = getPort();
            CoordinatorImpl stub = new CoordinatorImpl();
            LocateRegistry.createRegistry(port);
            String bindAddr = "rmi://localhost:"+port+"/coordinator";
            Naming.rebind(bindAddr,stub);
            
            System.out.println("COORDINATOR SERVER LISTENING ON LOCALHOST:"+port);
            System.out.println("\n---------------------------------------\n");
        }
        catch(RemoteException e)
        {
            CoordinatorLogger.logEntry("SEVERE", "Remote Exception in main - "+e);
        }
        catch(Exception e)
        {
            CoordinatorLogger.logEntry("SEVERE", "Exception in main - "+e);
        }
    }
    
}
