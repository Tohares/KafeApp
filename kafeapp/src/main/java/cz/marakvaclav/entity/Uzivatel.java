package cz.marakvaclav.entity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Základní (předková) třída reprezentující uživatele systému.
 * Udržuje přihlašovací údaje a poskytuje nástroje pro bezpečné ukládání a ověřování hesel.
 */
public class Uzivatel {
    protected String login;
    protected String hesloHash;

    public Uzivatel(String login, char[] heslo) {
        this.login = login;
        hesloHash = hashHeslo(heslo);
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getHesloHash() { return hesloHash; }
    public void setHeslo(char[] heslo) { hesloHash = hashHeslo(heslo); }
    public void setHesloHash(String hesloHash) { this.hesloHash = hesloHash; }

    // Vygeneruje bezpečný hash hesla pomocí algoritmu BCrypt (včetně náhodné kryptografické soli)
    public static String hashHeslo(char[] heslo) {
        if (heslo == null) return null;
        String hesloStr = new String(heslo); // Zkrácení života Stringu na absolutní minimum
        return BCrypt.hashpw(hesloStr, BCrypt.gensalt());
    }

    public static boolean overHeslo(char[] zadaneHeslo, String ulozenyHash) {
        if (zadaneHeslo != null && ulozenyHash != null && ulozenyHash.startsWith("$2a$")) {
            String hesloStr = new String(zadaneHeslo);
            return BCrypt.checkpw(hesloStr, ulozenyHash);
        }
        return false;
    }

    // Generuje kryptografický kontrolní součet (SHA-256) pro ověřování celistvosti a pravosti uložených datových souborů
    public static String checkSum(String line) {
        try {
            String salt = "tVpT3wR66byYtWuuZu";
            String saltedLine = salt + line;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(saltedLine.getBytes());
            return HexFormat.of().formatHex(hashBytes).substring(52);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Chyba: Algoritmus SHA-256 nebyl nalezen!");
        }
    }  

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uzivatel uzivatel = (Uzivatel) o;
        return Objects.equals(login, uzivatel.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }
}