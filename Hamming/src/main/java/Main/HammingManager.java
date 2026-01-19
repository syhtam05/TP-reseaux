package Main;

import java.util.Scanner;

public class HammingManager {
    
 // Méthode pour inverser une chaîne (utile pour la lecture droite-gauche)
    private static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    public static String calculerHamming(String donnees) {
        // On inverse pour que l'index 0 de la String soit la position 1 (à droite)
        String d = reverse(donnees);
        int m = d.length();
        int r = 0;
        while (Math.pow(2, r) < (m + r + 1)) r++;

        int n = m + r;
        int[] hamming = new int[n + 1]; 

        // 1. Placement des bits de données (en sautant les puissances de 2)
        int j = 0;
        for (int i = 1; i <= n; i++) {
            if ((i & (i - 1)) != 0) {
                hamming[i] = Character.getNumericValue(d.charAt(j++));
            }
        }

        // 2. Calcul des bits de parité
        for (int i = 0; i < r; i++) {
            int posP = (int) Math.pow(2, i);
            for (int k = 1; k <= n; k++) {
                if (((k >> i) & 1) == 1 && k != posP) {
                    hamming[posP] ^= hamming[k];
                }
            }
        }

        // Construction du résultat (on ré-inverse pour l'affichage de gauche à droite)
        StringBuilder res = new StringBuilder();
        for (int i = 1; i <= n; i++) res.append(hamming[i]);
        return res.reverse().toString(); 
    }

    public static void verifierHamming(String mot) {
        // On inverse pour que l'index 0 soit la position 1
        String m = reverse(mot);
        int n = m.length();
        int r = (int) (Math.log(n) / Math.log(2)) + 1;
        int syndrome = 0;

        System.out.println("--- Étapes de vérification (Pos 1 à droite) ---");
        for (int i = 0; i < r; i++) {
            int posP = (int) Math.pow(2, i);
            int controle = 0;
            for (int k = 1; k <= n; k++) {
                if (((k >> i) & 1) == 1) {
                    controle ^= Character.getNumericValue(m.charAt(k - 1));
                }
            }
            System.out.println("Bit de contrôle P" + posP + " (2^" + i + ") : " + controle);
            if (controle != 0) syndrome += posP;
        }

        if (syndrome == 0) {
            System.out.println("=> Résultat : Aucune erreur détectée.");
        } else {
            System.out.println("=> Erreur détectée à la position : " + syndrome + " (en partant de la droite)");
            // Correction
            char[] bits = m.toCharArray();
            bits[syndrome - 1] = (bits[syndrome - 1] == '0') ? '1' : '0';
            System.out.println("=> Mot corrigé : " + reverse(new String(bits)));
        }
    }
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("--- Gestionnaire de Code de Hamming ---");
        
        while (true) {
            System.out.println("\n1. Calculer un mot de Hamming");
            System.out.println("2. Vérifier un mot de Hamming");
            System.out.println("3. Quitter");
            System.out.print("Choix : ");
            int choix = sc.nextInt();
            sc.nextLine(); // Nettoyer le buffer

            if (choix == 3) break;

            System.out.print("Saisir la suite de bits : ");
            String input = sc.nextLine();

            if (choix == 1) {
                String resultat = calculerHamming(input);
                System.out.println("Mot de Hamming généré : " + resultat);
            } else {
                verifierHamming(input);
            }
        }
        sc.close();
    }
}
