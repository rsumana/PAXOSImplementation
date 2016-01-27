/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientOperations;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 *
 * @author sumanaravikrishnan
 */
public interface CoordinatorInterface extends Remote
{
    public int addAsCohort(String serverAddr) throws RemoteException;
    public HashMap prePopulate() throws RemoteException;
    public String getData(String serverAddr, String key) throws RemoteException;
    public String putData(String serverAddr, String key, String value) throws RemoteException;
    public String deleteData(String serverAddr, String key) throws RemoteException;
    public String propose(String serverAddr, String operation, String key, String value) throws RemoteException;
}
