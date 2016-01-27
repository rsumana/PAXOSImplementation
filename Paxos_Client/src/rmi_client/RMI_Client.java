/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi_client;

import ClientOperations.Client_Request;
import Logger.ClientLogger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
/**
 *
 * @author sumana
 */
public class RMI_Client 
{
    /**
     * @param args the command line arguments
     */
    
    public static Scanner sc = new Scanner(System.in);
    public static String serverAddr;
    public static void main(String[] args) 
    {
        //Initializing and creating client log
        new ClientLogger();
        try
        {
            System.out.println("\n\nCLIENT MACHINE\n------------------------\n");
            String ip = getIPAddr();
            int port = getPort(ip);
            serverAddr = ip+":"+port;
            Client_Request.createClientRequest(serverAddr);
        }
        catch(Exception e)
        {
            ClientLogger.logEntry("SEVERE", "Exception in main block "+e);
            ClientLogger.closeFileHandler();
        }
    }
    private static String getIPAddr()
    {
        boolean status = false;
        String ipHost = null;
        do
        {
            try
            {
                System.out.print("Enter Server IP Address/HostName : ");
                ipHost = sc.next();
                status = InetAddress.getByName(ipHost).isReachable(4000); //4 seconds
                if(status == false)
                {
                    System.out.println("Host not reachable");
                    System.out.println("Enter Valid IP");
                }
            }
            catch(java.net.UnknownHostException uhe)
            {
                System.out.println(ipHost+" does not map to an IP address");
                //logger.log(Level.INFO, ipHost+" does not map to an IP address");
                System.out.println("Enter Valid IP");
            }
            catch(Exception e)
            {
                System.out.println("Exeption for IP "+ipHost);
                //logger.log(Level.SEVERE, e.getMessage());
                System.out.println("Enter Valid IP");
            }
        } while (status == false);
        return ipHost;
    }
    private static int getPort(String ip)
    {
        int port = 0;
        
        boolean isListened = false;
        do
        {
            System.out.print("Enter Server port number : ");
            String portClient = sc.next();
            try
            {
                port = Integer.valueOf(portClient);
                if(port>=0 && port<=65535)
                {
                    isListened = testPort(ip, port);
                }
                else
                {
                    System.out.println("Enter Valid Port");
                }
                //System.out.println(isListened);
            }
            catch(Exception e)
            {
                System.out.println("Enter Valid Port");
            }
        }while(!isListened);
        return port;
    }
    private static boolean testPort(String ip, int port) 
    {
        boolean isListened = false;
        Socket test = null;
        try
        {
            test = new Socket(ip, port);
            isListened = true;
        }
        catch(Exception e)
        {
            isListened = false;
        }
        finally
        {
            if(test != null)
            {
                try
                {
                    test.close();
                }
                catch(IOException e)
                {
                    isListened = false;
                }
            }
        }
        return isListened;
    }
}
