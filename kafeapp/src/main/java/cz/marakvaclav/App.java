package cz.marakvaclav;

import cz.marakvaclav.sluzby.KafeController;
import cz.marakvaclav.sluzby.KafeGui;
import cz.marakvaclav.sluzby.SpravceSouboru;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Vítejte v KafeApp" );

        // Spuštění GUI ve správném vlákně (Event Dispatch Thread) pro bezpečné vykreslování ve Swingu
        SwingUtilities.invokeLater(() -> {
            SpravceSouboru.nactiKonfiguraci();
            
            // Kontrola prvního spuštění a nastavení cesty k datové složce s CSV soubory
            java.io.File configFile = new java.io.File("kafeapp.properties");
            if (!configFile.exists()) {
                int volba = JOptionPane.showConfirmDialog(null, 
                    "První spuštění: Chcete vybrat síťovou nebo jinou složku pro databázi?\n" +
                    "(Pokud zvolíte Ne, data se budou ukládat do složky s programem)", 
                    "Výběr databáze", JOptionPane.YES_NO_OPTION);
                    
                if (volba == JOptionPane.YES_OPTION) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setDialogTitle("Vyberte složku pro ukládání dat (CSV)");
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        SpravceSouboru.setPracovniSlozka(chooser.getSelectedFile().getAbsolutePath());
                    } else {
                        SpravceSouboru.ulozKonfiguraci(); // Uloží výchozí nastavení, ať už se příště neptá
                    }
                } else {
                    SpravceSouboru.ulozKonfiguraci(); // Uživatel zvolil Ne
                }
            }

            // Inicializace hlavní byznys logiky a načtení základních datových struktur
            KafeController controller = new KafeController();
            if (!controller.inicializujAplikaci()) {
                System.out.println("Založení administrátora bylo zrušeno. Aplikace se ukončí.");
                System.exit(0);
                return;
            }
            
            KafeGui gui = new KafeGui(controller);
            controller.setGui(gui);

        });
    }
}