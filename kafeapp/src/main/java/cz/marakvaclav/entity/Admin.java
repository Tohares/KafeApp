package cz.marakvaclav.entity;

import java.math.BigInteger;

/**
 * Entita reprezentující administrátora systému. 
 * Oproti běžnému uživateli má navíc nastavené bankovní údaje pro příjem plateb od ostatních kafařů.
 */
public class Admin extends Uzivatel {
    private String cisloUctuIBAN = null;
    private String cisloUctuCZ = null;
    
    public Admin(String login, char[] heslo) {
        super(login, heslo);
    }

    public Admin(String login, char[] heslo, String cisloUctuIBAN, String cisloUctuCZ) {
        super(login, heslo);
        this.cisloUctuIBAN = cisloUctuIBAN;
        this.cisloUctuCZ = cisloUctuCZ;
    }

    public String getCisloUctuIBAN() {
        return cisloUctuIBAN;
    }

    public void setCisloUctuIBAN(String cisloUctu) {
        this.cisloUctuIBAN = cisloUctu;
    }

    public String getCisloUctuCZ() {
        return cisloUctuCZ;
    }

    public void setCisloUctuCZ(String cisloUctu) {
        this.cisloUctuCZ = cisloUctu;
    }

    public static boolean isValidIBAN(String iban) {
        if (iban == null) return false;
        String clean = iban.replaceAll("\\s+", "").toUpperCase();
        
        // 1. Základní formát (2 písmena, 2 kontrolní číslice, zbytek alfanumerické znaky, celková délka 15 až 34)
        if (!clean.matches("^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$")) return false;
        
        // 2. Kontrola délky specifická pro ČR a SR (musí být přesně 24 znaků)
        if (clean.startsWith("CZ") && clean.length() != 24) return false;
        if (clean.startsWith("SK") && clean.length() != 24) return false;

        // 3. Matematická kontrola Modulo 97 (mezinárodní standard pro IBAN)
        // Přesuneme první 4 znaky na konec
        String moved = clean.substring(4) + clean.substring(0, 4);
        
        // Písmena převedeme na čísla (A=10, B=11 ... Z=35)
        StringBuilder numericIban = new StringBuilder();
        for (char c : moved.toCharArray()) {
            if (Character.isLetter(c)) {
                numericIban.append(Character.getNumericValue(c));
            } else {
                numericIban.append(c);
            }
        }
        
        // Zbytek po dělení číslem 97 musí být podle normy ISO 13616 přesně 1
        BigInteger bigInt = new BigInteger(numericIban.toString());
        return bigInt.mod(new BigInteger("97")).intValue() == 1;
    }

    /**
     * Kontroluje, zda je české číslo účtu konzistentní s českým IBANem.
     * @param czAccount Formát [prefix-]číslo/kód banky, např. "19-1234567890/0800" nebo "1234567890/0800".
     * @param iban Český IBAN (24 znaků začínajících na CZ).
     * @return true, pokud jsou účty konzistentní, nebo pokud nelze kontrolu provést (jeden z údajů chybí).
     */
    public static boolean isCzAccountConsistentWithIban(String czAccount, String iban) {
        // Pokud jeden z údajů chybí, nebo pokud IBAN není český, nelze provést kontrolu.
        if (czAccount == null || czAccount.trim().isEmpty() || iban == null || iban.trim().isEmpty()) {
            return true;
        }
        
        String cleanIban = iban.replaceAll("\\s+", "").toUpperCase();
        if (!cleanIban.startsWith("CZ") || cleanIban.length() != 24) {
            return true;
        }

        try {
            // 1. Parsování českého formátu účtu
            String[] parts = czAccount.trim().split("/");
            if (parts.length != 2) return false;
            String bankCodeCz = parts[1];

            String[] accountParts = parts[0].split("-");
            long prefixCz = (accountParts.length == 2) ? Long.parseLong(accountParts[0]) : 0;
            long numberCz = (accountParts.length == 2) ? Long.parseLong(accountParts[1]) : Long.parseLong(accountParts[0]);

            // 2. Extrakce dat z IBANu
            String bankCodeIban = cleanIban.substring(4, 8);
            long prefixIban = Long.parseLong(cleanIban.substring(8, 14));
            long numberIban = Long.parseLong(cleanIban.substring(14, 24));

            // 3. Porovnání
            return bankCodeCz.equals(bankCodeIban) && prefixCz == prefixIban && numberCz == numberIban;
        } catch (NumberFormatException e) {
            // Pokud se cokoliv nepodařilo převést na číslo, formát je neplatný.
            return false;
        }
    }

    /**
     * Vytvoří české číslo účtu z platného českého IBANu.
     * @param iban Český IBAN (24 znaků začínajících na CZ).
     * @return Lokální formát účtu (např. 1234567890/0800), nebo prázdný řetězec při chybě.
     */
    public static String getCzAccountFromIban(String iban) {
        if (iban == null) return "";
        String clean = iban.replaceAll("\\s+", "").toUpperCase();
        if (!clean.startsWith("CZ") || clean.length() != 24) return "";
        
        try {
            String bankCode = clean.substring(4, 8);
            long prefix = Long.parseLong(clean.substring(8, 14));
            long number = Long.parseLong(clean.substring(14, 24));
            
            if (prefix > 0) {
                return prefix + "-" + number + "/" + bankCode;
            } else {
                return number + "/" + bankCode;
            }
        } catch (NumberFormatException e) {
            return "";
        }
    }

    public String toCsv() {
        return login + ";" + hesloHash + ";" + cisloUctuIBAN + ";" + cisloUctuCZ;
    }
}