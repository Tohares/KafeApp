package cz.marakvaclav;

import javax.swing.SwingUtilities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Vitejte v KafeApp
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Vitejte v KafeApp" );
        PolozkaSkladu p1 = new PolozkaSkladu();
        p1.setNazev("Cukr");
        p1.setAktualniMnozstvi(1);
        p1.setJednotka("Kg");
        p1.setCenaZaKus(BigDecimal.valueOf(15.90));
        p1.setMenaPenezni("CZK");

        PolozkaSkladu p2 = new PolozkaSkladu("Kafe", 1, "Kg", BigDecimal.valueOf(450), "CZK");

        System.out.println("Na sklade je: " + p1.getNazev() + " " + p1.getAktualniMnozstvi() + " " + p1.getJednotka());
        System.out.println("Na sklade je: " + p2.getNazev() + " " + p2.getAktualniMnozstvi() + " " + p2.getJednotka());

        Admin admin = new Admin("admin", "adminheslo");
        
        Kafar kafar1 = new Kafar("Vaclav", "heslo123");
        Kafar kafar2 = new Kafar("Petr", "tajneheslo");
        Kafar kafar3 = new Kafar("Honza", "tajneheslo2");
        
        List<Kafar> kafari = new ArrayList<>();
        kafari.add(kafar1);
        kafari.add(kafar2);
        kafari.add(kafar3);
        
        System.out.println("Zahajuji evidenci kavy");

        kafar1.vypijKavu();
        kafar1.vypijKavu();
        kafar2.vypijKavu();
        kafar2.vypijKavu();
        kafar2.vypijKavu();
        kafar3.vypijKavu();
        kafar3.vypijKavu();
        kafar3.vypijKavu();
        kafar3.vypijKavu();
        kafar3.vypijKavu();
        kafar3.vypijKavu();

        System.out.println("Kafar " + kafar1.getLogin() + " vypil uz " + kafar1.getPocetVypitychKav() + " kav");
        System.out.println("Kafar " + kafar2.getLogin() + " vypil uz " + kafar2.getPocetVypitychKav() + " kav");
        System.out.println("Kafar " + kafar3.getLogin() + " vypil uz " + kafar3.getPocetVypitychKav() + " kav");
        
        SwingUtilities.invokeLater(() -> new KafeGui(kafari, admin));

        System.out.println("Kafar " + kafar1.getLogin() + " vypil uz " + kafar1.getPocetVypitychKav() + " kav");
        System.out.println("Kafar1 login: " + kafar1.getLogin() + "/n hashHeslo: " + kafar1.getHesloHash());
     
    }
}
