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

    public static String hashHeslo(String heslo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(heslo.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Chyba: Algoritmus SHA-256 nebyl nalezen!");
        }
    }
}