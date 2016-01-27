/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi_server_cohort;

import ClientOperations.CohortImpl;
import ClientOperations.CoordinatorInterface;
import Logger.ServerLogger;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

/**
 *
 * @author sumanaravikrishnan
 */
public class RMI_Server_Cohort 
{
    /**
     * @param args the command line arguments
     */
    
    public static ServerLogger log = null;
    public static Scanner sc = new Scanner(System.in);
    public static String acceptorServerAddr;
    public static String proposerServerAddr;
    public static int serverId;
    public static void main(String[] args) 
    {
        new ServerLogger();
        if(args.length == 0)
        {
            System.out.println("Not received");
        }
        createCoordinatorConnection(args[0],args[1]);
        createRMIConnection();
    }
    //Get the port on the server the serversocket is created on
    private static int getPort(String type)
    {
        int port = 0;
        
        boolean isAvailable = false;
        do
        {
            System.out.print("Enter "+type+" port number: ");
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
                    System.out.println(type+" PORT NOT AVAILBLE. Please enter new port");
                }
            }
            catch(Exception e)
            {
                System.out.println(type+" PORT NOT VALID. Please enter new port");
            }
        }while(!isAvailable);
        return port;
    }
    private static void createCoordinatorConnection(String host, String ip)
    {
        try
        {
            String coordinatorAddr = "rmi://"+host+":"+ip+"/coordinator";
            CohortImpl.coordinatorStub =(CoordinatorInterface)Naming.lookup(coordinatorAddr);
            ServerLogger.logEntry("INFO", "Connection with Coordinator established at "+host+":"+ip);
        }
        catch(RemoteException e)
        {
            ServerLogger.logEntry("SEVERE", "Remote Exception in main - "+e);
        }
        catch(Exception e)
        {
            ServerLogger.logEntry("SEVERE", "Exception in main - "+e);
        }
    }
    
    //Testing port availability
    private static boolean testServerPort(int port) 
    {
        boolean isAvailable = false;
        ServerSocket test = null;
        ServerLogger.logEntry("INFO", "Testing if port "+port+" is occupied");
        try
        {
            //If server socket can be created on a port, then its available
            test = new ServerSocket(port);
            isAvailable = true;
            ServerLogger.logEntry("INFO", port+" is available");
        }
        catch(Exception e)
        {
            //Server socket cannot be created. Some other application is using the port
            ServerLogger.logEntry("INFO", port+" not available");
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
                    ServerLogger.logEntry("INFO", port+" is not available. Not able to close test socket");
                    isAvailable = false;
                }
            }
        }
        return isAvailable;
    }

    private static void createRMIConnection() 
    {
        try
        {
            System.out.println("\n\nSERVER MACHINE\n-------------------------\n");
            int port = getPort("SERVER");
            
            CohortImpl proposerStub = new CohortImpl();
            CohortImpl acceptorStub = new CohortImpl();
            
            LocateRegistry.createRegistry(port);
            proposerServerAddr = "//localhost:"+port+"/proposer";
            Naming.rebind(proposerServerAddr,proposerStub);
//            System.out.println("CohortImpl stub created at "+proposerServerAddr);
            
            acceptorServerAddr = "//localhost:"+port+"/acceptor";
            Naming.rebind(acceptorServerAddr,acceptorStub);
            //Naming.rebind(acceptorServerAddr,CohortImpl.coordinatorStub);
//            System.out.println("CohortImpl acceptor stub created at "+acceptorServerAddr);
            
            serverId = CohortImpl.coordinatorStub.addAsCohort(acceptorServerAddr);
            CohortImpl.prePopulateHashMap();
            //serverData s = new serverData(port)
            System.out.println("SERVER LISTENING ON PORT "+port+"\n");
            new Reminder(180);
        }
        catch(RemoteException e)
        {
            ServerLogger.logEntry("SEVERE", "Remote Exception in main - "+e);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            ServerLogger.logEntry("SEVERE", "Exception in main - "+e);
        }
    }
    
}
