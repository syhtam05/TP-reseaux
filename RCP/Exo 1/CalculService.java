import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CalculService extends Remote {
    int addInt(int a, int b) throws RemoteException;
    int subInt(int a, int b) throws RemoteException;
    
    float addFloat(float a, float b) throws RemoteException;
    float divFloat(float a, float b) throws RemoteException;
}
