package cz.marakvaclav.entity;

public class Admin extends Uzivatel {
    private String cisloUctuIBAN = null;
    private String cisloUctuCZ = null;
    
    public Admin(String login, String heslo) {
        super(login, heslo);
    }

    public Admin(String login, String heslo, String cisloUctuIBAN, String cisloUctuCZ) {
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

    public String toCsv() {
        return login + ";" + hesloHash + ";" + cisloUctuIBAN + ";" + cisloUctuCZ;
    }
}