package demo.java.demotransactionsystem.util;


import java.util.Random;


public class Utility {
    public static String generateIban() {
        StringBuilder iban = new StringBuilder();

        // Country Code (ISO 3166-1 alpha-2)
        iban.append("NL");

        // Check Digits (two digits)
        iban.append(generateRandomNumber(10, 99));

        iban.append("TEST");

        // Basic Bank Account Number (BBAN)
        iban.append(generateRandomNumber(1000000000, 9999999999L));
        // Example IBAN: NL99 TEST 5678 9012 34
        return iban.toString();
    }

    // Utility method to generate a random number within a range
    private static long generateRandomNumber(long min, long max) {
        Random random = new Random();
        return min + random.nextInt((int) (max - min));
    }
}
