package Main;

public class CRCManager {

    /**
     * Calcule le CRC et retourne le mot complet (Message + CRC).
     * 
     * @param message   Chaîne de bits (ex: "1101")
     * @param generator Polynôme générateur (ex: "1011")
     */
    public static String calculateCRC(String message, String generator) {
        int n = generator.length() - 1;
        // 1. Adjonction de n zéros au message
        String augmentedMessage = message + "0".repeat(n);

        System.out.println("\n--- Étapes du calcul du CRC ---");
        System.out.println("Message original : " + message);
        System.out.println("Message augmenté : " + augmentedMessage);
        System.out.println("Générateur : " + generator);

        // 2. Division XOR
        String remainder = performXORDivision(augmentedMessage, generator);

        // Le CRC doit avoir une longueur fixe de n bits
        String crc = formatRemainder(remainder, n);

        System.out.println("Reste calculé (CRC) : " + crc);
        System.out.println("Mot complet à envoyer : " + message + crc);

        return message + crc;
    }

    /**
     * Vérifie si un mot complet contient une erreur.
     */
    public static boolean verifyCRC(String fullWord, String generator) {
        System.out.println("\n--- Étapes de vérification ---");
        System.out.println("Mot reçu : " + fullWord);

        String remainder = performXORDivision(fullWord, generator);

        // Si le reste ne contient que des '0', il n'y a pas d'erreur
        boolean isValid = !remainder.contains("1");

        System.out.println("Reste de la division : " + remainder);
        System.out.println(isValid ? "Résultat : Aucune erreur détectée." : "Résultat : Erreur détectée !");

        return isValid;
    }

    /**
     * Simule la division binaire XOR
     */
    private static String performXORDivision(String dividend, String divisor) {
        int divLen = divisor.length();
        char[] data = dividend.toCharArray();

        System.out.println("Début de la division :");
        System.out.println(new String(data));

        for (int i = 0; i <= dividend.length() - divLen; i++) {
            if (data[i] == '1') {
                System.out.print(" ".repeat(i) + divisor + " (XOR)");
                for (int j = 0; j < divLen; j++) {
                    data[i + j] = (data[i + j] == divisor.charAt(j)) ? '0' : '1';
                }
                System.out.println("\n" + " ".repeat(i) + "-".repeat(divLen));
                System.out.println(new String(data));
            }
        }

        String finalRemainder = new String(data).substring(dividend.length() - (divLen - 1));
        return finalRemainder;
    }

    private static String formatRemainder(String remainder, int length) {
        if (remainder.length() < length) {
            return "0".repeat(length - remainder.length()) + remainder;
        }
        return remainder;
    }
}