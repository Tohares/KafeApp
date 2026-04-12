package cz.marakvaclav.sluzby;

import cz.marakvaclav.entity.Vyuctovani;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VyuctovaniService {
    private List<Vyuctovani> seznamVyuctovani = new ArrayList<>();

    public void setSeznamVyuctovani(List<Vyuctovani> seznamVyuctovani) { this.seznamVyuctovani = seznamVyuctovani; }
    public List<Vyuctovani> getSeznamVyuctovani() { return seznamVyuctovani; }

    public List<Vyuctovani> getHistorieKafare(String login) {
        return seznamVyuctovani.stream().filter(v -> v.getLogin().equals(login)).collect(Collectors.toList());
    }

    public boolean jeVyuctovaniCastecneZaplaceno(Vyuctovani adminVyuctovani, String adminLogin) {
        if (adminVyuctovani == null || !adminVyuctovani.getLogin().equals(adminLogin)) return false;
        return najdiVsechnyPoductenky(adminVyuctovani).stream().anyMatch(v -> !v.getLogin().equals(adminLogin) && v.getStavPlatby());
    }

    public List<Vyuctovani> najdiVsechnyPoductenky(Vyuctovani adminVyuc) {
        return seznamVyuctovani.stream()
            .filter(v -> v.getDatumVystaveni().equals(adminVyuc.getDatumVystaveni()) && 
                         v.getCelkovaCena().compareTo(adminVyuc.getCelkovaCena()) == 0 &&
                         v.getPocetUctovanychKavCelkem() == adminVyuc.getPocetUctovanychKavCelkem())
            .collect(Collectors.toList());
    }

    public Vyuctovani najdiHlavniVyuctovaniKPoductence(Vyuctovani poductenka, String adminLogin) {
        return najdiVsechnyPoductenky(poductenka).stream().filter(v -> v.getLogin().equals(adminLogin)).findFirst().orElse(null);
    }
    
    public boolean jsouVsechnyPoductenkyZaplacene(Vyuctovani poductenka, String adminLogin) {
        return najdiVsechnyPoductenky(poductenka).stream().noneMatch(v -> !v.getLogin().equals(adminLogin) && !v.getStavPlatby());
    }

    public void smazVyuctovani(List<Vyuctovani> kSmazani) { seznamVyuctovani.removeAll(kSmazani); }

    public void pridejVyuctovani(Vyuctovani v) {
        seznamVyuctovani.add(v);
    }
}