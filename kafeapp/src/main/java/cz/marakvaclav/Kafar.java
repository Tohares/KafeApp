package cz.marakvaclav;

public class Kafar extends Uzivatel {
    private int pocetVypitychKav;

    public Kafar(String login, String heslo) {
        super(login, heslo);
        this.pocetVypitychKav = 0;
    }

    public int getPocetVypitychKav() {
        return pocetVypitychKav;
    }

    public void vypijKavu() {
        this.pocetVypitychKav++;
        System.out.println(login + " si dal kavu, celkem uz vypil: " + pocetVypitychKav);
    }

    public String toCsv() {
        return login + ";" + hesloHash + ";" + pocetVypitychKav;
    }

    public void zaplatit() {
        this.pocetVypitychKav = 0;
        System.out.println(login + " zaplatil vsechny kavy");
    }
}
