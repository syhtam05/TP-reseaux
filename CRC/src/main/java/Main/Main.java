package Main;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n=== Gestionnaire de Code CRC ===");
            System.out.println("1. Calculer un CRC (Générer une trame)");
            System.out.println("2. Vérifier une trame (Détecter des erreurs)");
            System.out.println("3. Quitter");
            System.out.print("Choix : ");
            
            int choice = scanner.nextInt();
            if (choice == 3) break;

            System.out.print("Saisissez la suite de bits : ");
            String data = scanner.next();
            System.out.print("Saisissez le polynôme générateur (ex: 1011) : ");
            String gen = scanner.next();

            switch (choice) {
                case 1 -> CRCManager.calculateCRC(data, gen);
                case 2 -> CRCManager.verifyCRC(data, gen);
                default -> System.out.println("Choix invalide.");
            }
        }
        scanner.close();
    }
}