package cz.marakvaclav;

import cz.marakvaclav.sluzby.KafeController;
import cz.marakvaclav.sluzby.KafeGui;
import cz.marakvaclav.sluzby.SpravceSouboru;
import cz.marakvaclav.sluzby.AuthService;
import cz.marakvaclav.sluzby.SkladService;
import cz.marakvaclav.sluzby.VyuctovaniService;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Vítejte v KafeApp" );

        // Nastavení globálního zachytávače výjimek (Global Exception Handler) pro vlákno vykreslování (EDT)
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Throwable rootCause = throwable;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause(); // Zajištění, že se probereme až ke kořenové výjimce (např. z InvocationTarget)
            }
            
            final Throwable cause = rootCause;

            if (cause instanceof SpravceSouboru.DatabaseUnavailableException) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, cause.getMessage(), "Chyba databáze", JOptionPane.ERROR_MESSAGE));
            } else if (cause instanceof IllegalArgumentException) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, cause.getMessage(), "Upozornění", JOptionPane.WARNING_MESSAGE));
            } else if (cause instanceof IllegalStateException) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, cause.getMessage(), "Informace", JOptionPane.INFORMATION_MESSAGE));
            } else {
                cause.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Došlo k neočekávané chybě aplikace:\n" + cause.getMessage(), "Kritická chyba", JOptionPane.ERROR_MESSAGE));
            }
        });

        // Spuštění GUI ve správném vlákně (Event Dispatch Thread) pro bezpečné vykreslování ve Swingu
        SwingUtilities.invokeLater(() -> {
            SpravceSouboru spravceSouboru = new SpravceSouboru();
            spravceSouboru.nactiKonfiguraci();
            
            String nastavenaSlozka = spravceSouboru.getPracovniSlozka();
            
            // Pokud je nastavena cesta, ověříme její dostupnost asynchronně (na pozadí),
            // abychom neblokovali vykreslení UI po dobu dlouhého síťového timeoutu.
            if (nastavenaSlozka != null && !nastavenaSlozka.isEmpty()) {
                JDialog loadingDialog = new JDialog((Frame)null, "Start KafeApp", false);
                loadingDialog.setLayout(new BorderLayout(10, 10));
                JLabel lbl = new JLabel("Ověřuji dostupnost databáze na síti, prosím čekejte...", SwingConstants.CENTER);
                lbl.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 10, 20));
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                loadingDialog.add(lbl, BorderLayout.CENTER);
                loadingDialog.add(progressBar, BorderLayout.SOUTH);
                loadingDialog.pack();
                loadingDialog.setLocationRelativeTo(null);
                loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                loadingDialog.setVisible(true);

                new Thread(() -> {
                    boolean existuje = new File(nastavenaSlozka).exists();
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        dokonciSpusteni(!existuje, spravceSouboru);
                    });
                }).start();
            } else {
                dokonciSpusteni(false, spravceSouboru);
            }
        });
    }

    private static void dokonciSpusteni(boolean slozkaJeNeplatna, SpravceSouboru spravceSouboru) {
        String nastavenaSlozka = spravceSouboru.getPracovniSlozka();
        File configFile = new File(spravceSouboru.getSouborKonfigurace());
        
        // Kontrola prvního spuštění nebo nedostupné složky (např. odpojený síťový disk)
        if (!configFile.exists() || slozkaJeNeplatna) {
            String zprava = slozkaJeNeplatna 
                ? "Složka s databází není dostupná (např. odpojený síťový disk):\n" + nastavenaSlozka + "\n\nChcete vybrat jinou složku pro databázi?"
                : "První spuštění: Chcete vybrat síťovou nebo jinou složku pro databázi?\n(Pokud zvolíte Ne, data se budou ukládat do složky s programem)";
            
            int volba = JOptionPane.showConfirmDialog(null, zprava, "Výběr databáze", JOptionPane.YES_NO_OPTION);
                
            if (volba == JOptionPane.YES_OPTION) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle("Vyberte složku pro ukládání dat (CSV)");
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    spravceSouboru.setPracovniSlozka(chooser.getSelectedFile().getAbsolutePath());
                } else {
                    if (slozkaJeNeplatna) {
                        JOptionPane.showMessageDialog(null, "Bez dostupné databáze nelze pokračovat.", "Konec", JOptionPane.WARNING_MESSAGE);
                        System.exit(0);
                    }
                    spravceSouboru.ulozKonfiguraci(); // Uloží výchozí nastavení, ať už se příště neptá
                }
            } else {
                if (slozkaJeNeplatna) {
                    JOptionPane.showMessageDialog(null, "Bez dostupné databáze nelze pokračovat.", "Konec", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                spravceSouboru.ulozKonfiguraci(); // Uživatel zvolil Ne
            }
        }

        // Inicializace hlavní byznys logiky a načtení základních datových struktur
        AuthService authService = new AuthService();
        SkladService skladService = new SkladService();
        VyuctovaniService vyuctovaniService = new VyuctovaniService();
        KafeController controller = new KafeController(spravceSouboru, authService, skladService, vyuctovaniService);
        if (!controller.inicializujAplikaci()) {
            System.out.println("Založení administrátora bylo zrušeno. Aplikace se ukončí.");
            System.exit(0);
            return;
        }
        
        KafeGui gui = new KafeGui(controller);
        controller.setView(gui);
    }
}