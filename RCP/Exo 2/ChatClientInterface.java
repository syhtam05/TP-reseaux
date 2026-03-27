import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatClientInterface extends Remote {
    void recevoirMessage(String message) throws RemoteException;
}