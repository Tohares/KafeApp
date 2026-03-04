package cz.marakvaclav;

public class Kafar {
    private String login;
    private String heslo;
    private int pocetVypitychKav;

    public Kafar(String login, String heslo) {
        this.login = login;
        this.heslo = heslo;
        this.pocetVypitychKav = 0;
    }

    public int getPocetVypitychKav() {
        return pocetVypitychKav;
    }

    public void vypijKavu() {
        this.pocetVypitychKav++;
        System.out.println(login + " si dal kavu, celkem uz vipil: " + pocetVypitychKav);
    }

    public String getLogin() {
        return login;
    }
}
