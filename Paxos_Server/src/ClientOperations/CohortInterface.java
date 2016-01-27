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
public interface CohortInterface extends Remote
{
    public boolean hasKey(String key) throws RemoteException;
    public String getData(String key) throws RemoteException;
    public String putData(String key, String value) throws RemoteException;
    public String deleteData(String key) throws RemoteException;
//    public String get(String key) throws RemoteException;
    public String put(String key, String value) throws RemoteException;
    public String delete(String key) throws RemoteException;
    public HashMap<String,String> getHashMap() throws RemoteException;
    public boolean prepare(int id) throws RemoteException;
    public boolean promise(int id) throws RemoteException;
}
