package cz.marakvaclav.sluzby;

import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.panely.*;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Hlavní okno aplikace, které se stará o vykreslování jednotlivých panelů, dialogů a obsluhu událostí (kliknutí na tlačítka, menu).
public class KafeGui extends JFrame implements KafeView {
    private KafeUIController controller;
    private JPanel emptyPanel;
    private KafariPanel kafariPanel;
    private SkladPanel skladPanel;
    private UctenkyPanel uctenkyPanel;
    private JPanel welcomePanel;
    private UserPanel userPanel;
    private UserToolbarPanel userToolbarPanel;
    private KafarDashboardPanel kafarDashboardPanel;
    private AdminToolbarPanel adminToolbarPanel;
    private JButton vypitButton;
    private JButton odebratButton;
    private KafeMenuBar menuBar;
    private JPanel panelUpozorneniZapis;
    private boolean nacitaSe = false;
    private Timer timerSkrytiZapisovani;
    private long casPoslednihoZobrazeni = 0;
    private KafeDialogManager dialogManager;
    private KafeStateManager stateManager;


    public KafeGui(KafeUIController controller) {
        this.controller = controller;
        this.dialogManager = new KafeDialogManager(this, controller);
        KafeActionHandler actionHandler = new KafeActionHandler(controller, this, dialogManager);
        
        setTitle("KafeApp");
        // Místo okamžitého zabití přesměruje zavření okna na Controller, který zkontroluje probíhající zápisy na disk
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.ukonceniAplikace();
            }
        });
        setSize(1024,768);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        kafariPanel = new KafariPanel();
        kafariPanel.nastavOdsazeniBunek(10, () -> controller.getAdmin() != null ? controller.getAdmin().getLogin() : null);

        userPanel = new UserPanel();
        userPanel.nastavOdsazeniBunek(10, () -> controller.getAdmin() != null ? controller.getAdmin().getLogin() : null);

        skladPanel = new SkladPanel();
        skladPanel.nastavOdsazeniBunek(10);

        uctenkyPanel = new UctenkyPanel(controller, dialogManager::otevriPlatbu);
        uctenkyPanel.nastavOdsazeniBunek(10, () -> controller.getAdmin() != null ? controller.getAdmin().getLogin() : null);
        

        welcomePanel = new JPanel(new GridBagLayout());
        JLabel vzkaz = new JLabel("Pro přístup se prosím přihlaste.");
        vzkaz.setFont(new Font("Arial", Font.BOLD, 16));
        welcomePanel.add(vzkaz);

        emptyPanel = new JPanel(new BorderLayout());
        add(emptyPanel, BorderLayout.CENTER);

        // Inicializace hlavních ovládacích tlačítek pro standardního kafaře
        userToolbarPanel = new UserToolbarPanel();

        vypitButton = new JButton("Vypít kávu");
        odebratButton = new JButton("Zrušit kávu (-1)");
        kafarDashboardPanel = new KafarDashboardPanel(userPanel, uctenkyPanel, vypitButton, odebratButton);

        add(userToolbarPanel, BorderLayout.SOUTH);

        // Inicializace dodatečných ovládacích tlačítek pro administrátora
        adminToolbarPanel = new AdminToolbarPanel();

        JPanel topContainer = new JPanel(new BorderLayout());
        
        panelUpozorneniZapis = new JPanel();
        panelUpozorneniZapis.setBackground(new Color(220, 53, 69)); // tmavě červená
        JLabel upozorneniLabel = new JLabel("Provedené změny se právě ukládají na disk, neukončujte prosím aplikaci...");
        upozorneniLabel.setForeground(Color.WHITE);
        upozorneniLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panelUpozorneniZapis.add(upozorneniLabel);
        panelUpozorneniZapis.setVisible(false);
        
        topContainer.add(panelUpozorneniZapis, BorderLayout.NORTH);
        topContainer.add(adminToolbarPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        menuBar = new KafeMenuBar(actionHandler.getMenuActionHandler());
        setJMenuBar(menuBar);
        
        stateManager = new KafeStateManager(controller, emptyPanel, welcomePanel, kafarDashboardPanel, 
                                            userPanel, uctenkyPanel, userToolbarPanel, adminToolbarPanel, menuBar);
        
        // Koncentrované prodrátování událostí (Event Glue)
        actionHandler.bindEvents(kafariPanel, skladPanel, uctenkyPanel, userToolbarPanel, adminToolbarPanel, vypitButton, odebratButton);

        updateView();
        setVisible(true);
    }

    @Override
    public void nastavStavNacitani(boolean nacitam) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> nastavStavNacitani(nacitam));
            return;
        }
        this.nacitaSe = nacitam;
        updateView();
    }

    @Override
    public void zobrazChybyIntegrity() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::zobrazChybyIntegrity);
            return;
        }
        List<String> chyby = controller.getChybyIntegrity();
        if (!chyby.isEmpty()) {
            String message = String.join("\n", chyby);
            JOptionPane.showMessageDialog(this, 
                "Při načítání dat došlo k chybám integrity:\n" + message, 
                "Kritická chyba dat", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void nastavViditelnostZapisovani(boolean viditelne) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> nastavViditelnostZapisovani(viditelne));
            return;
        }
        if (viditelne) {
            if (timerSkrytiZapisovani != null && timerSkrytiZapisovani.isRunning()) {
                timerSkrytiZapisovani.stop();
            }
            if (!panelUpozorneniZapis.isVisible()) {
                casPoslednihoZobrazeni = System.currentTimeMillis();
                panelUpozorneniZapis.setVisible(true);
            }
        } else {
            long zbyvaCasu = 1000 - (System.currentTimeMillis() - casPoslednihoZobrazeni);
            if (zbyvaCasu > 0) {
                timerSkrytiZapisovani = new Timer((int) zbyvaCasu, e -> panelUpozorneniZapis.setVisible(false));
                timerSkrytiZapisovani.setRepeats(false);
                timerSkrytiZapisovani.start();
            } else {
                panelUpozorneniZapis.setVisible(false);
            }
        }
    }

    @Override
    public void zobrazPanelKafaru() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::zobrazPanelKafaru);
            return;
        }
        if (controller.isAdmin()) {
            // Zruší výběr na pozadí, aby se zablokovala na nich závislá kontextová tlačítka
            kafariPanel.zrusVyber();
            skladPanel.zrusVyber();
            uctenkyPanel.zrusVyber();

            kafariPanel.obnovData(controller.getAktivniKafari(), controller.getSeznamVyuctovani());
            adminToolbarPanel.setObnovitUzivateleEnabled(!controller.getDeaktivovaniKafari().isEmpty());
            
            updateView();
            emptyPanel.add(kafariPanel);
            revalidate(); // Revaliduje celé okno, aby se správně přepočítala šířka tlačítek
            repaint();
        }
    }

    public void zobrazPanelSkladu() {
        if (controller.isAdmin()) {
            kafariPanel.zrusVyber();
            skladPanel.zrusVyber();
            uctenkyPanel.zrusVyber();

            skladPanel.obnovData(controller.getSklad());
            updateView();
            emptyPanel.add(skladPanel);
            revalidate();
            repaint();
        }
    }

    @Override
    public void zobrazPanelUctenek() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::zobrazPanelUctenek);
            return;
        }
        if (controller.isAdmin()) {
            kafariPanel.zrusVyber();
            skladPanel.zrusVyber();
            uctenkyPanel.zrusVyber();

            uctenkyPanel.obnovData(controller.getSeznamVyuctovani(), null);
            updateView();
            emptyPanel.add(uctenkyPanel);
            revalidate();
            repaint();
        }
    }

    public boolean maVybranouPolozku() {
        return skladPanel.maVybranouPolozku();
    }

    public int getVybraneIdPolozky() {
        return skladPanel.getVybraneIdPolozky();
    }

    public Vyuctovani getVybraneVyuctovani() {
        return uctenkyPanel.getVybraneVyuctovani();
    }

    public String getVybranyLogin() {
        return kafariPanel.getVybranyLogin();
    }

    @Override
    public void otevriExportHistorieDialog(List<Vyuctovani> historie) {
        dialogManager.otevriExportHistorieDialog(historie);
    }

    @Override
    public void updateView() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::updateView);
            return;
        }
        stateManager.renderState(nacitaSe);
        revalidate();
        repaint();
    }

    @Override
    public String vyberSlozku(String titulek, String vychoziSlozka) {
        return dialogManager.vyberSlozku(titulek, vychoziSlozka);
    }

    @Override
    public cz.marakvaclav.entity.Admin vyzadejNovehoAdmina(KafeUIController.AdminFactory factory) {
        return dialogManager.vyzadejNovehoAdmina(factory);
    }

    @Override
    public char[] vyzadejVynucenouZmenuHesla() {
        return dialogManager.vyzadejVynucenouZmenuHesla();
    }
}