package dictionaryLookUpRMI_ParalWrite;

import java.rmi.Remote;
import java.rmi.RemoteException;
 
public interface RMI_LookUpInterface extends Remote {

    public String getDef(String word) throws InterruptedException, RemoteException;

}