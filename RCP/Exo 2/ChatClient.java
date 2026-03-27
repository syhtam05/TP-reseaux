import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.Scanner;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {
    
    protected ChatClient() throws RemoteException { super(); }

    @Override
    public void recevoirMessage(String message) throws RemoteException {
        System.out.println("\n" + message);
        System.out.print("> ");
    }

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("Choisissez un pseudo : ");
            String pseudo = sc.nextLine();

            Registry reg = LocateRegistry.getRegistry("localhost", 1099);
            ChatServerInterface serveur = (ChatServerInterface) reg.lookup("ChatService");

            ChatClient monCallback = new ChatClient();
            serveur.enregistrerClient(monCallback, pseudo);

            while (true) {
                System.out.print("> ");
                String msg = sc.nextLine();
                serveur.diffuserMessage(pseudo, msg);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}