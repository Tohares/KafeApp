package cz.marakvaclav;

public class Uzivatel {
    protected String login;
    protected String heslo;

    public Uzivatel(String login, String heslo) {
        this.login = login;
        this.heslo = heslo;
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getHeslo() { return heslo; }
    public void setHeslo(String heslo) { this.heslo = heslo; }
}
