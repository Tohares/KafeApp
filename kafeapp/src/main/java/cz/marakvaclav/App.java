package cz.marakvaclav;

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

        PolozkaSkladu p2 = new PolozkaSkladu("Kafe", 1, "Kg");

        System.out.println("Na sklade je: " + p1.getNazev() + " " + p1.getAktualniMnozstvi() + " " + p1.getJednotka());
        System.out.println("Na sklade je: " + p2.getNazev() + " " + p2.getAktualniMnozstvi() + " " + p2.getJednotka());
        
        Kafar kafar1 = new Kafar("Vaclav", "heslo123");
        System.out.println("Zahajuji evidenci kavy");

        kafar1.vypijKavu();
        kafar1.vypijKavu();

        System.out.println("Kafar " + kafar1.getLogin() + " vypil uz " + kafar1.getPocetVypitychKav() + " kav");
    }
}
