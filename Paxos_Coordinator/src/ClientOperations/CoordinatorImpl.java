/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientOperations;

import Logger.CoordinatorLogger;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sumanaravikrishnan
 */
public class CoordinatorImpl extends UnicastRemoteObject implements CoordinatorInterface
{
    //A hashmap of K-Host:Port and V-TimeContacted
    HashMap<Integer,String> cohorts;
    private static CohortInterface cohortStub;
    private static CohortInterface cohort;
    boolean isCohortStubInitialized = false;
    boolean hasClientContacted = false;
    int noOfCohorts;
    LinkedList<String[]> proposals;
    AcceptorThread server1, server2, server3, server4, server5;
    
    
    public CoordinatorImpl() throws RemoteException
    {   
        cohorts = new HashMap<Integer,String>();
        proposals = new LinkedList<String[]>();
        noOfCohorts = 0;
    }

    private void initializeCohortStub(String serverAddr)
    {
        try
        {
            String bindAddr = "rmi://"+serverAddr+"/cohort";
            //System.out.println("Initializing cohort stub");
            cohortStub =(CohortInterface)Naming.lookup(bindAddr); 
            isCohortStubInitialized = true;
            CoordinatorLogger.logEntry("INFO", "Cohort Stub Created");
        }
        catch (NotBoundException ex) 
        {
            CoordinatorLogger.logEntry("SEVERE", "NotBoundException - "+ex);
        } 
        catch (MalformedURLException ex) 
        {
            CoordinatorLogger.logEntry("SEVERE", "MalformedURLException - "+ex);
        }
        catch (RemoteException ex) 
        {
            CoordinatorLogger.logEntry("SEVERE", "RemoteException - "+ex);
        }
    }
    
    @Override
    public String getData(String serverAddr, String key) throws RemoteException 
    {
        displayConsole(serverAddr, "GET", key, null);
        if(!isCohortStubInitialized) initializeCohortStub(serverAddr);
        String response = cohortStub.getData(key);
        return response;
    }
    
    private void displayConsole(String serverAddr, String operation, String key, String value)
    {
        String print = "Request to "+serverAddr+" for "+operation+" ("+key;
        if(operation.equals("PUT")) print+= ","+value;
        print+=")";
        System.out.println(print);
    }
    @Override
    public String putData(String serverAddr, String key, String value) throws RemoteException 
    {
        displayConsole(serverAddr, "PUT", key, value);
        if(!isCohortStubInitialized) initializeCohortStub(serverAddr);
        String response = "";
        int doOperation = 0;
        try
        {
            doOperation = cohortVote(key);
            if(doOperation == 1)
            {
                System.out.println("Decision = PUT("+key+")");
                response = broadcastCohort("PUT", key, value);
                hasClientContacted = true;
            }
            else
            {
                System.out.println("DECISION = NOT PUT("+key+","+value+")");
                response = "KEY ("+key+") ALREADY PRESENT IN HASHMAP";
            }
        }
        catch (NotBoundException ex) 
        {
            CoordinatorLogger.logEntry("SEVERE", "NotBoundException - "+ex);
        } 
        catch (MalformedURLException ex) 
        {
            CoordinatorLogger.logEntry("SEVERE", "MalformedURLException - "+ex);
        }
        return response;
    }

    @Override
    public synchronized String deleteData(String serverAddr, String key) throws RemoteException 
    {
        displayConsole(serverAddr, "DELETE", key, null);
        if(!isCohortStubInitialized) initializeCohortStub(serverAddr);
        String response = "";
        int doOperation = 0;
        try
        {
            doOperation = cohortVote(key);
            if(doOperation == 1)
            {
                System.out.println("Decision = DELETE("+key+")");
                response = broadcastCohort("DELETE", key, null);
                hasClientContacted = true;
            }
            else
            {
                System.out.println("DECISION = NOT DELETE("+key+")");
                response = "KEY ("+key+") NOT PRESENT IN HASHMAP";
            }
        }
        catch (NotBoundException ex) 
        {
            CoordinatorLogger.logEntry("SEVERE", "NotBoundException - "+ex);
        } 
        catch (MalformedURLException ex) 
        {
            CoordinatorLogger.logEntry("SEVERE", "MalformedURLException - "+ex);
        }
        return response;
    }
    
    private int cohortVote(String key)// throws NotBoundException, MalformedURLException, RemoteException
    {
        int doOperation = 1; //1- do operation, 0 - dont do operation
        Iterator<Integer> itr = getCohorts();
        System.out.println("VOTING");
        
        while(itr.hasNext())
        {
            String cohortAddr = cohorts.get(Integer.valueOf(itr.next()));
            System.out.println("");
            try 
            {
                cohort =(CohortInterface)Naming.lookup("rmi:"+cohortAddr);
                System.out.println("VOTING");
                boolean notContinue = cohort.hasKey(key);
                //System.out.println(cohortAddr+" - "+notContinue);
                doOperation = (!notContinue)? doOperation * 1 : doOperation *0;
            } 
            catch (NotBoundException ex) 
            {
                CoordinatorLogger.logEntry("SEVERE", "NotBoundException - "+ex);
            } 
            catch (MalformedURLException ex) 
            {
                CoordinatorLogger.logEntry("SEVERE", "MalformedURLException - "+ex);
            } 
            catch (RemoteException ex) 
            {
                CoordinatorLogger.logEntry("SEVERE", "RemoteException - "+ex);
            }
            
        }
        return doOperation;
    }
    
    private boolean preparePhase(String pServerAddr)
    {
        String[] votes = new String[cohorts.size()];
        boolean consensus = false;
        Iterator<Integer> itr = getCohorts();
        System.out.println("\n---------------------------------------\n");
        System.out.println("PREPARE PHASE");
        int serverId = getValueFromHashMap(pServerAddr);
        int i = 0;
        while(itr.hasNext())
        {
            String cohortAddr = cohorts.get(Integer.valueOf(itr.next()));
            if(!cohortAddr.contains(pServerAddr))
            {
                System.out.println("Requesting "+cohortAddr+" to PREPARE");
                try 
                {
                    cohort =(CohortInterface)Naming.lookup("rmi:"+cohortAddr);
                    votes[i] = String.valueOf(cohort.promise(serverId));
                    if(votes[i] == "true")
                    {
                        System.out.println(cohortAddr+" has PROMISED to ACCEPT");
                    }
                    else
                    {
                        System.out.println(cohortAddr+" DID NOT PROMISE");
                    }
                } 
                catch (NotBoundException ex) 
                {
                    Logger.getLogger(CoordinatorImpl.class.getName()).log(Level.SEVERE, null, ex);
                } 
                catch (MalformedURLException ex) 
                {
                    Logger.getLogger(CoordinatorImpl.class.getName()).log(Level.SEVERE, null, ex);
                } 
                catch (RemoteException ex) 
                {
                    Logger.getLogger(CoordinatorImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            i++;
        }
        consensus = reachConsensus(votes);
        return consensus;
    }
    
    private String broadcastCohort(String operation,String key,String value) throws NotBoundException, MalformedURLException, RemoteException
    {
        String result = "";
        String keyValue = (value == null) ? key : key + "," + value; 

        Iterator<Integer> itr = getCohorts();
        System.out.println("\nACCEPT PHASE");
        while(itr.hasNext())
        {
            String cohortAddr = cohorts.get(Integer.valueOf(itr.next()));
            cohort =(CohortInterface)Naming.lookup("rmi:"+cohortAddr);
            System.out.println("Requesting "+cohortAddr+" to "+operation+" "+keyValue);
            if(operation.equalsIgnoreCase("put"))
            {
                result = cohort.put(key,value);
            }
            else if (operation.equalsIgnoreCase("delete"))
            {
                result = cohort.delete(key);
            }
        }
        return result;
    }
    private Iterator<Integer> getCohorts()
    {
        return cohorts.keySet().iterator();
    }

    @Override
    public int addAsCohort(String serverAddr) throws RemoteException 
    {
        try
        {
            //noOfCohorts++;
            int noCohorts = cohorts.size() + 1;
            cohorts.put(noCohorts, serverAddr);
            CoordinatorLogger.logEntry("INFO", serverAddr+" ADDED AS COHORT, ID = "+noCohorts);
            System.out.println(serverAddr+" added as a cohort with id = "+noCohorts);
        }
        catch(Exception e)
        {
            CoordinatorLogger.logEntry("INFO", serverAddr+" already present as COHORT!");
        }  
        return cohorts.size();
    }

    @Override
    public HashMap prePopulate() throws RemoteException 
    {
        if(!hasClientContacted)
        {
           System.out.println("No updates in Hashmap. Populating standard values only.");
           return null; 
        }
        //return null;
        Iterator<Integer> itr = getCohorts();
        String cohortAddr = cohorts.get(Integer.valueOf(itr.next()));
        try
        {
            cohort =(CohortInterface)Naming.lookup("rmi:"+cohortAddr);
        } 
        catch (NotBoundException ex) 
        {
            CoordinatorLogger.logEntry("SEVERE", "NotBoundException - "+ex);
        } 
        catch (MalformedURLException ex) 
        {
            CoordinatorLogger.logEntry("SEVERE", "MalformedURLException - "+ex);
        } 
        System.out.println("Getting HashMap from "+cohortAddr);
        return cohort.getHashMap();
    }

    @Override
    public String propose(String serverAddr, String operation, String key, String value) throws RemoteException 
    {
        String keyValue = (value == null) ? key : key + "," + value; 
        System.out.println("---------------------------------------");
        System.out.println("PROPOSAL from "+serverAddr+" to "+operation+"("+keyValue+")");
        System.out.println("---------------------------------------");
        if(cohorts.size() > 1)
        {
            System.out.println("PROPOSAL PHASE");
            String[] proposal = getProposal(serverAddr, operation, key, value);
            //proposal.put(serverAddr, value);
            proposals.addLast(proposal);
        //        boolean consensus = preparePhase();
            boolean quorum = consensusPrepare();
            //Go to promise phase phase
            if(quorum)
            {
                try 
                {
                    boolean promise = consensusPromise();
                    System.out.println("Remove request from request queue");
                    proposals.removeFirst();
                    if(promise)
                    {
                        return broadcastCohort(operation,key,value);
                    }
                    else
                    {
                        return "Aborting in PROMISE Phase";
                    }
                } 
                catch (NotBoundException ex) 
                {
                    Logger.getLogger(CoordinatorImpl.class.getName()).log(Level.SEVERE, null, ex);
                } 
                catch (MalformedURLException ex) 
                {
                    Logger.getLogger(CoordinatorImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("Aborting in PREPARE phase");
            return "Aborting in PREPARE phase";
        }
        else
        {
            try 
            {
                return broadcastCohort(operation,key,value);
            } 
            catch (NotBoundException ex) 
            {
                Logger.getLogger(CoordinatorImpl.class.getName()).log(Level.SEVERE, null, ex);
            } 
            catch (MalformedURLException ex) 
            {
                Logger.getLogger(CoordinatorImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private String[] getProposal(String serverAddr, String operation, String key, String value)
    {
        String[] proposal = new String[4];
        proposal[0] = serverAddr;
        proposal[1] = operation;
        proposal[2] = key;
        proposal[3] = value;
        return proposal;
    }
    private int getValueFromHashMap(String pServerAddr) 
    {
        Iterator<Integer> itr = getCohorts();
        int serverId = 0;
        while(itr.hasNext())
        {
            int id = Integer.valueOf(itr.next());
            String serverAddr = cohorts.get(id);
            if(serverAddr.contains(pServerAddr))
            {
                serverId = id;
            }
        }
        return serverId;
    }

    private boolean reachConsensus(String[] votes) 
    {
        int totalVotes = votes.length;
        int totalTrue = 0;
        int totalFalse = 0;
        for(int i=0; i<votes.length; i++)
        {
            if(votes[i].equalsIgnoreCase("true"))
            {
                totalTrue++;
            }
            else
            {
                totalFalse++;
            }
        }
        System.out.println("Total votes in favour = "+totalTrue);
        System.out.println("Total votes against = "+totalFalse);
        if(totalTrue > totalVotes/2)
        {
//            System.out.println("Bro");
            return true;
        }
        return false;
    }
    
    private boolean reachConsensus(LinkedList<Boolean> votes) 
    {
        System.out.println("\nCounting the Votes:");
        int totalVotes = votes.size();
        int totalTrue = 0;
        int totalFalse = 0;
        for(int i=0; i<totalVotes; i++)
        {
            boolean vote = votes.get(i);
            if(vote)
            {
                totalTrue++;
            }
            else
            {
                totalFalse++;
            }
        }
        System.out.println("Total collected votes = "+totalVotes);
        System.out.println("Total votes in favour = "+totalTrue);
        System.out.println("Total votes against = "+totalFalse);
        if(totalTrue > totalFalse)
        {
//            System.out.println("Bro");
            System.out.println("Quorum reached. Next phase.");
            return true;
        }
        System.out.println("Quorum reached. ABORT.");
        return false;
    }
    
    private boolean consensusPrepare()
    {
        LinkedList<Boolean> votes = proposalQuorum(true);
        boolean quorum = reachConsensus(votes);
//        System.out.println("Consensus Reached. Removing proposal from request queue");
        return quorum;
    }
    
    private boolean consensusPromise()
    {
        LinkedList<Boolean> votes = proposalQuorum(false);
        boolean quorum = reachConsensus(votes);
//        System.out.println("Consensus Reached. Removing proposal from request queue");
        return quorum;
    }

    private LinkedList<Boolean> proposalQuorum(boolean isPreparePhase) 
    {
//        boolean quorum = false;
        LinkedList<Boolean> votes = new LinkedList<Boolean>();
        String[] proposal = proposals.getFirst();
        String proposerAddr = proposal[0];
        int proposerId = getValueFromHashMap(proposerAddr);
        System.out.println("Proposer Addr = "+proposerAddr+", ID = "+proposerId);
        
        
        
        //Initialize all the accpetor threads. Operations carried out through their constructors.
        for(int i = 1; i <= cohorts.size(); i++)
        {
//            long startTime = System.currentTimeMillis();
            String serverAddr = cohorts.get(i);
            switch(i)
            {
                case 1  :   server1 = new AcceptorThread(proposerId, serverAddr, proposerAddr, isPreparePhase);
                            break;
                case 2  :   server2 = new AcceptorThread(proposerId, serverAddr, proposerAddr, isPreparePhase);
                            break;
                case 3  :   server3 = new AcceptorThread(proposerId, serverAddr, proposerAddr, isPreparePhase);
                            break;
                case 4  :   server4 = new AcceptorThread(proposerId, serverAddr, proposerAddr, isPreparePhase);
                            break;
                case 5  :   server5 = new AcceptorThread(proposerId, serverAddr, proposerAddr, isPreparePhase);
                            break;
            }
        }
        for(int i = 1; i <= cohorts.size(); i++)
        {
            switch(i)
            {
                case 1  :   if(!server1.isProposer())
                            {
                                votes.add(server1.vote);
                            }
                            break;
                case 2  :   if(!server2.isProposer())
                            {
                                votes.add(server2.vote);
                            }
                            break;
                case 3  :   if(!server3.isProposer())
                            {
                                votes.add(server3.vote);
                            }
                            break;
                case 4  :   if(!server4.isProposer())
                            {
                                votes.add(server4.vote);
                            }
                            break;
                case 5  :   if(!server5.isProposer())
                            {
                                votes.add(server5.vote);
                            }
                            break;
            }
        }
        //
//        proposals.removeFirst();
        return votes;
    }

    

}
