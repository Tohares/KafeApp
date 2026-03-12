package cz.marakvaclav;

import javax.swing.SwingUtilities;

import java.util.ArrayList;
import java.util.List;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Vitejte v KafeApp" );
        
        Admin admin = new Admin("admin", "adminheslo", "CZ1655000000003723676956", "3723676956/5500");
        
        final List<Kafar> kafari;
        List<Kafar> nacteniKafaru = SpravceSouboru.nactiKafare();
        if (nacteniKafaru == null) {
            kafari = new ArrayList<>();
        }
        else {
            kafari = nacteniKafaru;
        }

        final List<PolozkaSkladu> sklad;
        List<PolozkaSkladu> nacteniSkladu = SpravceSouboru.nactiSklad();
        if (nacteniSkladu == null) {
            sklad = new ArrayList<>();
        }
        else {
            sklad = nacteniSkladu;
        }

        final List<Vyuctovani> seznamVyuctovani;
        List<Vyuctovani> nacteniVyuctovani = SpravceSouboru.nactiVyuctovani();
        if (nacteniVyuctovani == null) {
            seznamVyuctovani = new ArrayList<>();
        }
        else {
            seznamVyuctovani = nacteniVyuctovani;
        }

                    
        SwingUtilities.invokeLater(() -> new KafeGui(kafari, sklad, seznamVyuctovani, admin));

        if (kafari.isEmpty()) {
            System.out.println("Seznam kafaru je prazdny.");
        } else {
            for (Kafar k : kafari) {
                System.out.println("Nacten uzivatel: " + k.getLogin() + " (kav: " + k.getPocetVypitychKav() + ")");
            }
        }
     
    }
}
