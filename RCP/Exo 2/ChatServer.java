import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class ChatServer extends UnicastRemoteObject implements ChatServerInterface {
    private Map<String, ChatClientInterface> clients = new HashMap<>();

    protected ChatServer() throws RemoteException { super(); }

    @Override
    public synchronized void enregistrerClient(ChatClientInterface client, String pseudo) throws RemoteException {
        clients.put(pseudo, client);
        System.out.println(pseudo + " vient de se connecter.");
        diffuserMessage("Système", pseudo + " a rejoint le chat.");
    }

    @Override
    public synchronized void diffuserMessage(String pseudo, String msg) throws RemoteException {
        String format = "[" + pseudo + "]: " + msg;
        System.out.println("Diffusion: " + format);
        
        for (ChatClientInterface c : clients.values()) {
            try {
                c.recevoirMessage(format);
            } catch (RemoteException e) {
                // Nettoyage si un client est déconnecté brutalement
            }
        }
    }

    public static void main(String[] args) {
        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            reg.rebind("ChatService", new ChatServer());
            System.out.println("Serveur de Chat prêt.");
        } catch (Exception e) { e.printStackTrace(); }
    }
}