/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientOperations;

import Logger.ClientLogger;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.rmi.*;
import rmi_client.RMI_Client;

/**
 *
 * @author sumana
 */
public class HashMap_Operations 
{
    public static Scanner sc = new Scanner(System.in);
    private static CohortInterface serverStub;
    
    static String put() throws RemoteException 
    {
        String operation = "PUT";
        System.out.print("Enter key to "+operation+" : ");
        String key = sc.next();
        System.out.print("Enter value to "+operation+" : ");
        String value = sc.next();
        //String reqMessage = operation + ";" + key + ";" + value;
//        System.out.println("R");
        return serverStub.putData(key,value);
    }

    static String get() throws RemoteException 
    {
        String operation = "GET";
        System.out.print("Enter key to "+operation+" value for : ");
        String key = sc.next();
        //String reqMessage = operation + ";" + key;
        String response = serverStub.getData(key);
        return response;
    }

    static String delete() throws RemoteException 
    {
        String operation = "DELETE";
        System.out.print("Enter key to "+operation+" : ");
        String key = sc.next();
        //String reqMessage = operation + ";" + key;
        return serverStub.deleteData(key);
    }

    public static void createConnection(String cAddr) throws NotBoundException, MalformedURLException 
    {
        try
        {
            String bindAddr = "rmi://"+cAddr+"/proposer";
//            System.out.println("Contacting "+bindAddr);
            serverStub =(CohortInterface)Naming.lookup(bindAddr);
            //serverStub.addAsCohort(RMI_Client.serverAddr);
        }
        catch(RemoteException re)
        {
            ClientLogger.logEntry("SEVERE", "Remote Exception in createConnection - "+re);
        } 
    }
}
