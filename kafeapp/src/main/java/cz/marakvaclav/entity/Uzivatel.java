package cz.marakvaclav.entity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Základní (předková) třída reprezentující uživatele systému.
 * Udržuje přihlašovací údaje a poskytuje nástroje pro bezpečné ukládání a ověřování hesel.
 */
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

    // Vygeneruje bezpečný hash hesla pomocí algoritmu BCrypt (včetně náhodné kryptografické soli)
    public static String hashHeslo(String heslo) {
        return BCrypt.hashpw(heslo, BCrypt.gensalt());
    }

    public static boolean overHeslo(String zadaneHeslo, String ulozenyHash) {
        if (ulozenyHash != null && ulozenyHash.startsWith("$2a$")) {
            return BCrypt.checkpw(zadaneHeslo, ulozenyHash);
        }
        return false;
    }

    // Generuje kryptografický kontrolní součet (SHA-256) pro ověřování celistvosti a pravosti uložených datových souborů
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