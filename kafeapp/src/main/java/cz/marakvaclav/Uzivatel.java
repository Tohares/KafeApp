package cz.marakvaclav;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Uzivatel {
    protected String login;
    protected String hesloHash;

    public Uzivatel(String login, String heslo) {
        this.login = login;
        hesloHash = hashHeslo(heslo);
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getHesloHash() { return hesloHash; }
    public void setHeslo(String heslo) { hesloHash = hashHeslo(heslo); }
    public void setHesloHash(String hesloHash) { this.hesloHash = hesloHash; }



    public static String hashHeslo(String heslo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(heslo.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Chyba: Algoritmus SHA-256 nebyl nalezen!");
        }
    }

    public static String checkSum(String line) {
        try {
            String salt = getSecretSalt();
            String saltedLine = salt + line;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(saltedLine.getBytes());
            return HexFormat.of().formatHex(hashBytes).substring(52);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Chyba: Algoritmus SHA-256 nebyl nalezen!");
        }
    }

    private static String getSecretSalt() {
        String part1 = Uzivatel.class.getSimpleName();
        String part2 = String.valueOf(Uzivatel.class.getDeclaredMethods().length);
        String part3 = "tVpT3wR66byYtWuuZu";
        
        return part1 + part2 + part3;
    }    
}