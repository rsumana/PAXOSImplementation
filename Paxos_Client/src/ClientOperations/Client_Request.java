/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientOperations;

import Logger.ClientLogger;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 *
 * @author suman
 */
public class Client_Request 
{
    public static Scanner sc = new Scanner(System.in);
    private static boolean isContinue = true;
    
    private static void sendRequestToServer(int opt) throws RemoteException 
    {
        String response = null;
        switch(opt)
        {
            case 1:
            {
                response = HashMap_Operations.put();
                break;
            }
                
            case 2:
            {
                response = HashMap_Operations.get();
                break;
            }
            case 3:
            {
                response = HashMap_Operations.delete();
                break;
            }
        }
        
        ClientLogger.logEntry("INFO", "Response from server - "+response);
        System.out.println(response+"\n");
    }
    
    //create server stub and recursively get user input
    public static void createClientRequest(String sAddr)
    {
        //Creating the connection by creating server stub
        try
        {
            HashMap_Operations.createConnection(sAddr);
        }
        catch(MalformedURLException me)
        {
            ClientLogger.logEntry("SEVERE", "MalformedURLException - "+me);
        }
        catch(NotBoundException ne)
        {
            ClientLogger.logEntry("SEVERE", "NotBoundException - "+ne);
        }
        
        //Getting the user request and processing it
        System.out.println("\n-----CLIENT SERVICE INITIATED-----\n");
        while(isContinue)
        {
            getUserRequest();
        }
        System.out.println("\n-----CLIENT SERVICE TERMINATED-----\n");
    }
    
    //get user option and access the server using server stub
    private static void getUserRequest() 
    {
        System.out.print("\nHASHMAP OPERATIONS\n------------------------\n1. PUT\n2. GET\n3. DELETE\n4. EXIT\n");
        System.out.print("\nYour choice : ");
        try
        {
            String option = sc.next();
            int userChoice = Integer.valueOf(option);
            if(userChoice>=1 && userChoice<=4)
            {
                if(userChoice != 4)
                {
                    sendRequestToServer(userChoice);
                }  
                else
                {
                    //User wishes to exit
                    isContinue = false;
                }
            }
            else
            {
                //Not a given option.
                System.out.println("\nEnter Valid Option! (1/2/3/4)");
            }
        }
        catch(RemoteException re)
        {
            ClientLogger.logEntry("INFO", "Remote Exception - "+re);
            /*System.out.println("RemoteException");
            re.printStackTrace();*/
        }
        catch(Exception e)
        {
            ClientLogger.logEntry("INFO", "User entered non int value - "+e);
            e.printStackTrace();
            System.out.println("\nEnter Valid Option! (1/2/3/4)");
        }    
    }
}
