import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CalculClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            CalculService stub = (CalculService) registry.lookup("CalculService");

            System.out.println("10 + 5 (int) = " + stub.addInt(10, 5));
            System.out.println("10.5 + 2.5 (float) = " + stub.addFloat(10.5f, 2.5f));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}