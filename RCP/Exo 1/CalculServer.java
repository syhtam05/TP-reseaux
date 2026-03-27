import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CalculServer extends UnicastRemoteObject implements CalculService {

    protected CalculServer() throws RemoteException {
        super();
    }

    @Override
    public int addInt(int a, int b) { return a + b; }

    @Override
    public int subInt(int a, int b) { return a - b; }

    @Override
    public float addFloat(float a, float b) { return a + b; }

    @Override
    public float divFloat(float a, float b) { 
        if (b == 0) return 0;
        return a / b; 
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("CalculService", new CalculServer());
            System.out.println("Le serveur RMI est lancé sur le port 1099...");
        } catch (Exception e) {
            System.err.println("Erreur Serveur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}