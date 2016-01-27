/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientOperations;

import Logger.ServerLogger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import rmi_server_cohort.RMI_Server_Cohort;

/**
 *
 * @author sumanaravikrishnan
 */
public class CohortImpl extends UnicastRemoteObject implements CohortInterface
{
    static HashMap<String,String> serverHash;
    public static CoordinatorInterface proposerStub;
    public static CoordinatorInterface coordinatorStub;
    private int promiseId;
    private int prepareId;

    public CohortImpl() throws RemoteException
    {
        promiseId = 0;
        prepareId = 0;
    }
    
    public static void prePopulateHashMap() throws RemoteException
    {
        HashMap coordinator = coordinatorStub.prePopulate();
        ServerLogger.logEntry("INFO", "Going to prepopulate server HashMap");
		serverHash  = new HashMap<>();
        if(coordinator == null)
        {
            serverHash.put("12", "14");
            serverHash.put("1", "4");
            serverHash.put("2", "32");
            serverHash.put("8", "52");
            serverHash.put("5", "10");
        }
        else
        {
            serverHash = coordinator;
        }
        System.out.println("\nHashMap populated with "+serverHash.toString());
        ServerLogger.logEntry("INFO", "HashMap prepopulated. "+serverHash.toString());
    }
    
    @Override
    public synchronized String getData(String key) 
    {
        ServerLogger.logEntry("INFO", "GET OPERATION");
        //boolean continue = coordinatorStub.canGet(key);
        String result = get(key);
        ServerLogger.logEntry("INFO", result);
        displayResultConsole(result);
        return result;
    }

    @Override
    public synchronized String putData(String key, String value) throws RemoteException 
    {
        ServerLogger.logEntry("INFO", "PUT OPERATION");
        System.out.println("Proposing PUT ("+key+","+value+") to the Coordinator");
        String response = coordinatorStub.propose(RMI_Server_Cohort.acceptorServerAddr, "PUT", key,value);
        return response;
    }

    @Override
    public synchronized String deleteData(String key) throws RemoteException 
    {
        ServerLogger.logEntry("INFO", "DELETE OPERATION");
        System.out.println("Proposing DELETE ("+key+") to the Coordinator");
        String response = coordinatorStub.propose(RMI_Server_Cohort.acceptorServerAddr, "DELETE", key, null);
        return response;
    }
    
    private String removeWhiteSpace(String str)
    {
        str = str.trim().replaceAll("\\s+","");
        return str;
    }
    
    private synchronized static void viewData()
    {
        ServerLogger.logEntry("INFO", "HashMap = "+serverHash.toString());
        System.out.println("Updated HashMap = "+serverHash.toString());
    }
    
    private synchronized static void displayResultConsole(String str)
    {
        System.out.println(str+"\n");
        viewData();
    }
    
    private synchronized String get(String key)
    {
        key = removeWhiteSpace(key);
        if(serverHash.containsKey(key))
        {
            String value = serverHash.get(key);
            return "("+key+" : "+value+")";
        }
        return  "KEY ("+key+") NOT PRESENT IN HASHMAP";
    }
    
    @Override
    public synchronized String put(String key, String value)
    {
        key = removeWhiteSpace(key);
        value = removeWhiteSpace(value);
        if(!serverHash.containsKey(key))
        {
            serverHash.put(key, value);
            System.out.println("("+key+":"+value+") ADDED");
            viewData();
            return "("+key+":"+value+") ADDED";
            
        }
        System.out.println("KEY ("+key+") ALREADY PRESENT IN HASHMAP");
        viewData();
        return "KEY ("+key+") ALREADY PRESENT IN HASHMAP";
    }
    
     @Override
    public synchronized String delete(String key)
    {
        key = removeWhiteSpace(key);
        if(serverHash.containsKey(key))
        {
            String value = serverHash.get(key);
            serverHash.remove(key);
            System.out.println("("+key+" : "+value+")"+ " DELETED");
            viewData();
            return "("+key+" : "+value+")"+ " DELETED";
        }
        System.out.println("KEY ("+key+") NOT PRESENT IN HASHMAP");
        viewData();
        return  "KEY ("+key+") NOT PRESENT IN HASHMAP";
    }

    @Override
    public boolean hasKey(String key) throws RemoteException 
    {
        boolean hasKey = serverHash.containsKey(key);
        System.out.println(key+" - "+hasKey);
        return hasKey;
    }

    @Override
    public HashMap getHashMap() throws RemoteException 
    {
        return serverHash;
    }

    @Override
    public boolean prepare(int id) throws RemoteException 
    {
        System.out.println("Request to PREPARE for ID = "+id);
        if(prepareId > id)
        {
            System.out.println("Already PREPARED for ID = "+prepareId);
            return false;
        }
        System.out.println("PREPARED to accept ID = "+id);
        prepareId = id;
        promiseId = id;
        return true;
    }

    @Override
    public boolean promise(int id) throws RemoteException 
    {
        System.out.println("Request to PROMISE for ID = "+id);
        if(id < promiseId)
        {
            System.out.println("Already PROMISED for ID = "+promiseId);
            return false;
        }
        System.out.println("PROMISE to accept ID = "+promiseId);
        promiseId = id;
        return true;
    }
}
