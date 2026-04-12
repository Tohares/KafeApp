package cz.marakvaclav.sluzby;

import cz.marakvaclav.entity.Admin;
import cz.marakvaclav.entity.Kafar;
import cz.marakvaclav.entity.Uzivatel;

import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private List<Kafar> kafari = new ArrayList<>();
    private Admin admin;
    private String prihlasenyUzivatel = null;

    public void setKafari(List<Kafar> kafari) { this.kafari = kafari; }
    public void setAdmin(Admin admin) { this.admin = admin; }
    public List<Kafar> getKafari() { return kafari; }
    public Admin getAdmin() { return admin; }
    public String getPrihlasenyUzivatel() { return prihlasenyUzivatel; }

    public boolean isAdmin() {
        return prihlasenyUzivatel != null && admin != null && prihlasenyUzivatel.equals(admin.getLogin());
    }

    public Kafar getPrihlasenyKafar() {
        if (isAdmin() || prihlasenyUzivatel == null) return null;
        return getKafar(prihlasenyUzivatel);
    }

    public Kafar getKafar(String login) {
        for (Kafar k : kafari) {
            if (k.getLogin().equals(login)) return k;
        }
        return null;
    }

    public void overPrihlaseni(String login, char[] heslo) {
        boolean valid = false;
        if (admin != null && admin.getLogin().equals(login) && Uzivatel.overHeslo(heslo, admin.getHesloHash())) {
            valid = true;
        } else {
            Kafar k = getKafar(login);
            if (k != null && Uzivatel.overHeslo(heslo, k.getHesloHash())) {
                if (!k.isAktivni()) {
                    throw new IllegalArgumentException("Tento účet byl deaktivován administrátorem.");
                }
                valid = true;
            }
        }
        if (!valid) {
            throw new IllegalArgumentException("Špatný login nebo heslo!");
        }
    }

    public void prihlasit(String login) { this.prihlasenyUzivatel = login; }
    public void odhlasit() { this.prihlasenyUzivatel = null; }

    public void zmenitHeslo(char[] stareHeslo, char[] noveHeslo) {
        if (prihlasenyUzivatel == null) throw new IllegalStateException("Nejste přihlášeni.");
        if (isAdmin()) {
            if (Uzivatel.overHeslo(stareHeslo, admin.getHesloHash())) {
                admin.setHeslo(noveHeslo);
                return;
            }
        } else {
            Kafar k = getPrihlasenyKafar();
            if (k != null && Uzivatel.overHeslo(stareHeslo, k.getHesloHash())) {
                k.setHeslo(noveHeslo);
                return;
            }
        }
        throw new IllegalArgumentException("Zadané staré heslo není správné.");
    }

    public Kafar resetovatHesloKafare(String login, char[] noveHeslo) {
        if (!isAdmin()) return null;
        Kafar k = getKafar(login);
        if (k != null) {
            k.setHeslo(noveHeslo);
            k.setVyzadujeZmenuHesla(true);
            return k;
        }
        return null;
    }

    public Kafar zalozitUzivatele(String login, char[] heslo) {
        Kafar k = new Kafar(login, heslo);
        kafari.add(k);
        return k;
    }

    public int getPocetKavCelkem() {
        return kafari.stream().mapToInt(Kafar::getPocetVypitychKav).sum();
    }

    public Kafar vypitKavu() {
        Kafar k = getPrihlasenyKafar();
        if (k != null) {
            k.vypijKavu();
        }
        return k;
    }

    public Kafar odebratKavu() {
        Kafar k = getPrihlasenyKafar();
        if (k != null && k.getPocetVypitychKav() > 0) {
            k.odeberKavu();
            return k;
        }
        return null;
    }

    public Kafar zmenitPocetKav(String login, int novyPocet) {
        Kafar k = getKafar(login);
        if (k != null) {
            k.setPocetVypitychKav(novyPocet);
        }
        return k;
    }
}