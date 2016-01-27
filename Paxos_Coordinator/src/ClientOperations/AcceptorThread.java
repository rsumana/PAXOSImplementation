/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientOperations;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author sumanaravikrishnan
 */
public class AcceptorThread extends Thread
{
    
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future f;

    private static CohortInterface cohort;
    private String serverAddr;
    private String proposerAddr;
    private int proposerId;
    public boolean vote;
    private boolean isPrepare;
    private boolean isPromise;
    
    //For prepare phase
    AcceptorThread(int pId,String serverAddress, String proposerAddress, boolean isPreparePhase)
    {
        this.serverAddr = serverAddress;
        this.proposerAddr = proposerAddress;
        this.proposerId = pId;
//        this.operation = toDo;
        //System.out.println("Thread for "+serverAddr+" created");
        //Initialize server stub
        try 
        {
//            System.out.println("Trying to create stub connection");
            cohort =(CohortInterface)Naming.lookup("rmi:"+this.serverAddr);
//            System.out.println("Created");
        } 
        catch (NotBoundException ex) 
        {
            Logger.getLogger(AcceptorThread.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } 
        catch (MalformedURLException ex) 
        {
            Logger.getLogger(AcceptorThread.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } 
        catch (RemoteException ex) 
        {
            Logger.getLogger(AcceptorThread.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        
        if(isPreparePhase)
        {
            //this.interrupt();
            //this.stop();
            isPrepare = true;
            isPromise = false;
        } 
        else
        {
            isPrepare = false;
            isPromise = true;
        }
//        System.out.println("Initialized thread");
        //Put a timer here. 5 seconds.
//        long startTime = System.currentTimeMillis();
//        new TimerThread(5);
        this.run();
    }
    
    public boolean isProposer()
    {
        if(this.serverAddr.contains(this.proposerAddr) || this.proposerAddr.contains(this.serverAddr))
        {
//            System.out.println("Not requesting "+serverAddr+" since it proposed");
            return true;
        }
        return false;
    }
    
    @Override 
    public void run()
    {
        
//        System.out.println("---------------\nInside Run");
        try 
        {
            f = executor.submit(Executors.callable(new Thread()));
            f.get(5, TimeUnit.SECONDS);
        }
        catch(TimeoutException e) 
        {
            System.out.println("No response from server");
            this.vote = false;
        } 
        catch (InterruptedException ex) 
        {
            Logger.getLogger(AcceptorThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (ExecutionException ex) 
        {
            Logger.getLogger(AcceptorThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        try 
        {
            //prepare-accept phase
            if(!this.isProposer())
            {
                System.out.println("Requesting "+this.serverAddr+" to PREPARE for requestID = "+this.proposerId);
                if(isPrepare  && !isPromise)
                {
                    this.vote = cohort.prepare(this.proposerId);
                }
                else if (!isPrepare && isPromise)
                {
                    this.vote = cohort.promise(this.proposerId);
                }
                if(this.vote)
                {
                    System.out.println(this.serverAddr+" says YES");
                }
                else
                {
                    System.out.println(this.serverAddr+" says NO");
                }
            }
            //reach consensus
        } 
        catch (RemoteException ex) 
        {
            Logger.getLogger(AcceptorThread.class.getName()).log(Level.SEVERE, null, ex);
//            System.out.println("Remote Exception");
//            ex.printStackTrace();
        }
    }
}
