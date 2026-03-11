package cz.marakvaclav;

public class Admin extends Uzivatel {
    private String cisloUctu = null;
    
    public Admin(String login, String heslo) {
        super(login, heslo);
    }

    public Admin(String login, String heslo, String cisloUctu) {
        super(login, heslo);
        this.cisloUctu = cisloUctu;
    }

    public String getCisloUctu() {
        return cisloUctu;
    }

    public void setCisloUctu(String cisloUctu) {
        this.cisloUctu = cisloUctu;
    }
}