import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServerInterface extends Remote {
    void enregistrerClient(ChatClientInterface client, String pseudo) throws RemoteException;
    void diffuserMessage(String pseudo, String msg) throws RemoteException;
}