package cz.marakvaclav.sluzby;

import cz.marakvaclav.entity.PolozkaSkladu;
import cz.marakvaclav.entity.Surovina;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SkladService {
    private List<PolozkaSkladu> sklad = new ArrayList<>();

    public void setSklad(List<PolozkaSkladu> sklad) { this.sklad = sklad; }
    public List<PolozkaSkladu> getSklad() { return sklad; }

    public void overDostatek(Surovina surovina, int pozadovaneMnozstvi) {
        if (pozadovaneMnozstvi > getAgregovanaPolozka(surovina).getAktualniMnozstvi()) {
            throw new IllegalArgumentException("Nedostatek suroviny na skladě: " + surovina.getNazev());
        }
    }

    public PolozkaSkladu getAgregovanaPolozka(Surovina surovina) {
        PolozkaSkladu combined = null;
        for (PolozkaSkladu p : sklad) {
            if (p.getSurovina() == surovina && p.getAktualniMnozstvi() > 0) {
                if (combined == null) {
                    combined = new PolozkaSkladu(p.getId(), p.getSurovina(), p.getKoupeneMnozstvi(), p.getAktualniMnozstvi(), p.getJednotka(), p.getCenaZaKus(), p.getMenaPenezni());
                } else if (combined.getJednotka().equals(p.getJednotka())) {
                    combined.setAktualniMnozstvi(combined.getAktualniMnozstvi() + p.getAktualniMnozstvi());
                }
            }
        }
        if (combined == null) {
            for (PolozkaSkladu p : sklad) {
                if (p.getSurovina() == surovina) {
                    combined = new PolozkaSkladu(p.getId(), p.getSurovina(), p.getKoupeneMnozstvi(), p.getAktualniMnozstvi(), p.getJednotka(), p.getCenaZaKus(), p.getMenaPenezni());
                    break;
                }
            }
        }
        return combined != null ? combined : new PolozkaSkladu(-1, surovina, 0, 0, "ks", BigDecimal.ZERO, "CZK");
    }

    public List<PolozkaSkladu> odeberSurovinu(Surovina surovina, int mnozstvi) {
        List<PolozkaSkladu> spotrebovane = new ArrayList<>();
        if (mnozstvi <= 0) return spotrebovane;
        int zbyva = mnozstvi;
        for (PolozkaSkladu s : sklad) {
            if (s.getSurovina() == surovina && s.getAktualniMnozstvi() > 0) {
                int odebrat = Math.min(s.getAktualniMnozstvi(), zbyva);
                s.setAktualniMnozstvi(s.getAktualniMnozstvi() - odebrat);
                spotrebovane.add(new PolozkaSkladu(s.getId(), s.getSurovina(), odebrat, odebrat, s.getJednotka(), s.getCenaZaKus(), s.getMenaPenezni()));
                zbyva -= odebrat;
                if (zbyva <= 0) break;
            }
        }
        return spotrebovane;
    }

    public void vratSurovinyNaSklad(List<PolozkaSkladu> vracene) {
        for (PolozkaSkladu spotrebovana : vracene) {
            if (spotrebovana.getAktualniMnozstvi() > 0) {
                sklad.stream().filter(s -> s.getId() == spotrebovana.getId()).findFirst()
                     .ifPresent(s -> s.setAktualniMnozstvi(s.getAktualniMnozstvi() + spotrebovana.getAktualniMnozstvi()));
            }
        }
    }

    public void naskladnit(PolozkaSkladu p) {
        sklad.add(p);
    }

    public void smazatPolozku(PolozkaSkladu p) {
        sklad.remove(p);
    }
}