package cz.marakvaclav;

import javax.swing.SwingUtilities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Vitejte v KafeApp" );
        PolozkaSkladu p1 = new PolozkaSkladu();
        p1.setNazev("Cukr");
        p1.setKoupeneMnozstvi(1);
        p1.setAktualniMnozstvi(1);
        p1.setJednotka("Kg");
        p1.setCenaZaKus(BigDecimal.valueOf(15.90));
        p1.setMenaPenezni("CZK");

        PolozkaSkladu p2 = new PolozkaSkladu("Kafe", 1, "Kg", BigDecimal.valueOf(450), "CZK");

        System.out.println("Na sklade je: " + p1.getNazev() + " " + p1.getKoupeneMnozstvi() + " " + p1.getJednotka());
        System.out.println("Na sklade je: " + p2.getNazev() + " " + p2.getKoupeneMnozstvi() + " " + p2.getJednotka());

        Admin admin = new Admin("admin", "adminheslo");
        
        final List<Kafar> kafari;
        List<Kafar> nacteniKafaru = SpravceSouboru.nactiKafare();
        if (nacteniKafaru == null) {
            kafari = new ArrayList<>();
        }
        else {
            kafari = nacteniKafaru;
        }

        final List<PolozkaSkladu> sklad = new ArrayList<>();
        sklad.add(p1);
        sklad.add(p2);
                    
        SwingUtilities.invokeLater(() -> new KafeGui(kafari, sklad, admin));

        if (kafari.isEmpty()) {
            System.out.println("Seznam kafaru je prazdny.");
        } else {
            for (Kafar k : kafari) {
                System.out.println("Nacten uzivatel: " + k.getLogin() + " (kav: " + k.getPocetVypitychKav() + ")");
            }
        }
     
    }
}
