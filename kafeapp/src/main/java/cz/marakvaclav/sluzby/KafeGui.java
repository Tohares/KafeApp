package cz.marakvaclav.sluzby;

import cz.marakvaclav.dialogy.*;
import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.entity.PolozkaSkladu;
import cz.marakvaclav.panely.*;

import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;


public class KafeGui extends JFrame {
    private KafeController controller;
    private JPanel emptyPanel;
    private KafariPanel kafariPanel;
    private SkladPanel skladPanel;
    private UctenkyPanel uctenkyPanel;
    private JPanel welcomePanel;
    private UserPanel userPanel;
    private JButton uctenkyButton;
    private JButton vypitButton;
    private JButton prihlasitButton;
    private JButton odhlasitButton;
    private JButton zmenitHesloButton;
    private JButton exportHistorieKafareButton;
    private JButton zalozitUzivateleButton;
    private JButton kafariButton;
    private JButton skladButton;
    private JButton naskladnitButton;
    private JButton upravitPolozkuButton;
    private JButton vyuctovatButton;
    private JButton stornovatVyuctovaniButton;
    private JMenuItem menuItemPrihlasit;
    private JMenuItem menuItemOdhlasit;
    private JMenuItem menuItemZmenitHeslo;
    private JMenuItem menuItemExportZalohy;
    private JMenuItem menuItemImportZalohy;
    private JPanel panelUpozorneniZapis;
    private boolean nacitaSe = false;

    public KafeGui(KafeController controller) {
        this.controller = controller;
        
        setTitle("KafeApp");
        // Místo okamžitého zabití přesměruje zavření okna na Controller, který zkontroluje probíhající zápisy na disk
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controller.ukonceniAplikace();
            }
        });
        setSize(1024,768);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        kafariPanel = new KafariPanel();
        kafariPanel.getTableModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column == 1 && row >= 0) {
                try {
                    Object newValue = kafariPanel.getTableModel().getValueAt(row, column);
                    int novyPocet = Integer.parseInt(newValue.toString());
                    
                    if (novyPocet < 0) {
                        throw new NumberFormatException("Záporné hodnoty nejsou povoleny.");
                    }

                    String login = (String) kafariPanel.getTableModel().getValueAt(row, 0);
                    controller.zmenitPocetKav(login, novyPocet);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Zadejte prosím platné číslo!", "Chyba formátu", JOptionPane.ERROR_MESSAGE);
                    akcePrepnoutNaKafare(); 
                }
            }
        });
        nastavOdsazeni(kafariPanel.getTable(), 10);

        userPanel = new UserPanel();
        nastavOdsazeni(userPanel.getTable(), 10);

        skladPanel = new SkladPanel();
        nastavOdsazeni(skladPanel.getTable(), 10);

        uctenkyPanel = new UctenkyPanel(this);
        nastavOdsazeni(uctenkyPanel.getTable(), 10);

        uctenkyPanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = uctenkyPanel.getTable().getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = uctenkyPanel.getTable().convertRowIndexToModel(selectedRow);
                    String loginTab = uctenkyPanel.getTableModel().getValueAt(modelRow, 0).toString();
                    stornovatVyuctovaniButton.setEnabled(controller.getAdmin() != null && loginTab.equals(controller.getAdmin().getLogin()));
                } else {
                    stornovatVyuctovaniButton.setEnabled(false);
                }
            }
        });
        

        welcomePanel = new JPanel(new GridBagLayout());
        JLabel vzkaz = new JLabel("Pro přístup se prosím přihlaste.");
        vzkaz.setFont(new Font("Arial", Font.BOLD, 16));
        welcomePanel.add(vzkaz);

        emptyPanel = new JPanel(new BorderLayout());
        add(emptyPanel, BorderLayout.CENTER);

        JPanel panelTlacitek = new JPanel();
        
        vypitButton = new JButton("Vypít kávu");
        prihlasitButton = new JButton("Přihlásit");
        odhlasitButton = new JButton("Odhlásit");
        zmenitHesloButton = new JButton("Změnit heslo");
        zalozitUzivateleButton = new JButton("Vytvořit nového uživatele");
        exportHistorieKafareButton = new JButton("Export historie");

        vypitButton.addActionListener(e -> akceVypitKavu());
        prihlasitButton.addActionListener(e -> akcePrihlasit());
        odhlasitButton.addActionListener(e -> akceOdhlasit());
        zmenitHesloButton.addActionListener(e -> akceZmenitHeslo());
        zalozitUzivateleButton.addActionListener(e -> akceZalozitUzivatele());
        exportHistorieKafareButton.addActionListener(e -> akceExportHistorieKafare());

        panelTlacitek.add(vypitButton);
        panelTlacitek.add(prihlasitButton);
        panelTlacitek.add(odhlasitButton);
        panelTlacitek.add(zmenitHesloButton);
        panelTlacitek.add(zalozitUzivateleButton);
        panelTlacitek.add(exportHistorieKafareButton);

        add(panelTlacitek, BorderLayout.SOUTH);

        JPanel panelTlacitekAdmina = new JPanel();
        kafariButton = new JButton("Kafaři");
        skladButton = new JButton("Sklad");
        naskladnitButton = new JButton("Naskladnit");
        upravitPolozkuButton = new JButton("Změnit položku");
        upravitPolozkuButton.setEnabled(false);
        vyuctovatButton = new JButton("Vyúčtovat");
        stornovatVyuctovaniButton = new JButton("Stornovat účtenku");
        stornovatVyuctovaniButton.setEnabled(false);
        uctenkyButton = new JButton("Účtenky");
        
        skladPanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                upravitPolozkuButton.setEnabled(skladPanel.getTable().getSelectedRow() != -1);
            }
        });

        kafariButton.addActionListener(e -> akcePrepnoutNaKafare());
        skladButton.addActionListener(e -> akcePrepnoutNaSklad());
        naskladnitButton.addActionListener(e -> akceNaskladnit());
        upravitPolozkuButton.addActionListener(e -> akceUpravitPolozku());
        vyuctovatButton.addActionListener(e -> akceVyuctovat());
        stornovatVyuctovaniButton.addActionListener(e -> akceStornovatVyuctovani());
        uctenkyButton.addActionListener(e -> akceUctenky());

        panelTlacitekAdmina.add(kafariButton);
        panelTlacitekAdmina.add(skladButton);
        panelTlacitekAdmina.add(naskladnitButton);
        panelTlacitekAdmina.add(upravitPolozkuButton);
        panelTlacitekAdmina.add(vyuctovatButton);
        panelTlacitekAdmina.add(stornovatVyuctovaniButton);
        panelTlacitekAdmina.add(uctenkyButton);

        JPanel topContainer = new JPanel(new BorderLayout());
        
        panelUpozorneniZapis = new JPanel();
        panelUpozorneniZapis.setBackground(new Color(220, 53, 69)); // tmavě červená
        JLabel upozorneniLabel = new JLabel("Provedené změny se právě ukládají na disk, neukončujte prosím aplikaci...");
        upozorneniLabel.setForeground(Color.WHITE);
        upozorneniLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panelUpozorneniZapis.add(upozorneniLabel);
        panelUpozorneniZapis.setVisible(false);
        
        topContainer.add(panelUpozorneniZapis, BorderLayout.NORTH);
        topContainer.add(panelTlacitekAdmina, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        vytvorMenu();

        updateView();
        setVisible(true);
    }

    private void vytvorMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuSoubor = new JMenu("Aplikace");
        menuItemPrihlasit = new JMenuItem("Přihlásit");
        menuItemOdhlasit = new JMenuItem("Odhlásit");
        menuItemZmenitHeslo = new JMenuItem("Změnit heslo");
        JMenuItem menuItemPrepnoutDatabazi = new JMenuItem("Přepnout databázi...");
        JMenuItem menuItemKonec = new JMenuItem("Ukončit");

        menuItemPrihlasit.addActionListener(e -> akcePrihlasit());
        menuItemOdhlasit.addActionListener(e -> akceOdhlasit());
        menuItemZmenitHeslo.addActionListener(e -> akceZmenitHeslo());
        menuItemPrepnoutDatabazi.addActionListener(e -> controller.prepnoutDatabazi());
        menuItemKonec.addActionListener(e -> controller.ukonceniAplikace());

        menuSoubor.add(menuItemPrihlasit);
        menuSoubor.add(menuItemOdhlasit);
        menuSoubor.add(menuItemZmenitHeslo);
        menuSoubor.addSeparator();
        menuSoubor.add(menuItemPrepnoutDatabazi);
        menuSoubor.addSeparator();
        menuSoubor.add(menuItemKonec);

        JMenu menuZaloha = new JMenu("Zálohování");
        menuItemExportZalohy = new JMenuItem("Export kompletní zálohy");
        menuItemImportZalohy = new JMenuItem("Import dat ze zálohy");

        menuItemExportZalohy.addActionListener(e -> akceExportZalohy());
        menuItemImportZalohy.addActionListener(e -> akceImportZalohy());

        menuZaloha.add(menuItemExportZalohy);
        menuZaloha.add(menuItemImportZalohy);

        JMenu menuNapoveda = new JMenu("Nápověda");
        JMenuItem menuItemOAplikaci = new JMenuItem("O aplikaci");
        menuItemOAplikaci.addActionListener(e -> akceOAplikaci());
        menuNapoveda.add(menuItemOAplikaci);

        menuBar.add(menuSoubor);
        menuBar.add(menuZaloha);
        menuBar.add(menuNapoveda);

        setJMenuBar(menuBar);
    }

    public void nastavStavNacitani(boolean nacitam) {
        this.nacitaSe = nacitam;
        updateView();
    }

    public void zobrazChybyIntegrity() {
        if (!SpravceSouboru.chybyIntegrity.isEmpty()) {
            String message = String.join("\n", SpravceSouboru.chybyIntegrity);
            JOptionPane.showMessageDialog(this, 
                "Při načítání dat došlo k chybám integrity:\n" + message, 
                "Kritická chyba dat", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    public void nastavViditelnostZapisovani(boolean viditelne) {
        panelUpozorneniZapis.setVisible(viditelne);
    }

    public void zobrazChybu(String zprava) {
        JOptionPane.showMessageDialog(this, zprava, "Chyba", JOptionPane.ERROR_MESSAGE);
    }

    public void zobrazInformaci(String zprava) {
        JOptionPane.showMessageDialog(this, zprava, "Informace", JOptionPane.INFORMATION_MESSAGE);
    }

    public void zobrazPanelKafaru() {
        if (controller.isAdmin()) {
            kafariPanel.obnovData(controller.getKafari(), controller.getSeznamVyuctovani());
            updateView();
            emptyPanel.add(kafariPanel);
            emptyPanel.revalidate();
            emptyPanel.repaint();
        }
    }

    public void zobrazPanelSkladu() {
        if (controller.isAdmin()) {
            skladPanel.obnovData(controller.getSklad());
            updateView();
            emptyPanel.add(skladPanel);
            emptyPanel.revalidate();
            emptyPanel.repaint();
        }
    }

    public void zobrazPanelUctenek() {
        if (controller.isAdmin()) {
            uctenkyPanel.obnovData(controller.getSeznamVyuctovani(), null);
            updateView();
            emptyPanel.add(uctenkyPanel);
            emptyPanel.revalidate();
            emptyPanel.repaint();
        }
    }

    private void akceExportHistorieKafare() {
        controller.zpracujExportHistorie();
    }

    public void otevriExportHistorieDialog(List<Vyuctovani> historie) {
        ExportHistorieKafareDialog dialog = new ExportHistorieKafareDialog(this, historie);
        dialog.setVisible(true);
    }

    private void akceExportZalohy() {
        ExportHistorieVsehoDialog dialog = new ExportHistorieVsehoDialog(this, controller);
        dialog.setVisible(true);
    }

    private void akceImportZalohy() {
        ImportHistorieVsehoDialog dialog = new ImportHistorieVsehoDialog(this, controller);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            updateView();
        }
    }

    private void akceOAplikaci() {
        JOptionPane.showMessageDialog(this, "KafeApp v1.0\nAplikace pro evidenci spotřeby kávy, mléka, cukru, kyseliny citronové a vyúčtování.\n\nAutor: Václav Mařák", "O aplikaci", JOptionPane.INFORMATION_MESSAGE);
    }

    private void akceNaskladnit() {
        if (controller.isAdmin()) {
            PolozkaDialog dialog = new PolozkaDialog(this, null);
            dialog.setVisible(true);

            if (dialog.isSucceeded()) {
                controller.naskladnit(dialog.getPolozka());
            }
        }
        zobrazPanelSkladu();
    }

    private void akceUpravitPolozku() {
        int selectedRow = skladPanel.getTable().getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = skladPanel.getTable().convertRowIndexToModel(selectedRow);
            PolozkaSkladu vybrana = controller.getSklad().get(modelRow);
            PolozkaDialog dialog = new PolozkaDialog(this, vybrana);
            dialog.setVisible(true);
            if (dialog.isDeleted()) {
                controller.smazatPolozkuSkladu(vybrana);
                zobrazPanelSkladu();
            } else if (dialog.isSucceeded()) {
                controller.upravitPolozkuSkladu(vybrana);
                zobrazPanelSkladu();
            }
        }
    }

    private void akceVyuctovat() {
        if (!controller.maNecoKVyuctovani()) {
            zobrazInformaci("Není co vyúčtovat. Celkový počet vypitých káv je 0.");
            return;
        }
        if (controller.isAdmin()) {
            VyuctovaniDialog vyuctovaniDialog = new VyuctovaniDialog(this, controller);
            vyuctovaniDialog.setVisible(true);
        }
        controller.reloadVyuctovani();
        zobrazPanelUctenek();        
    }

    private void akceStornovatVyuctovani() {
        int selectedRow = uctenkyPanel.getTable().getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = uctenkyPanel.getTable().convertRowIndexToModel(selectedRow);
            Vyuctovani v = najdiVyuctovaniZRadku(modelRow);
            if (v != null && v.getLogin().equals(controller.getAdmin().getLogin())) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Opravdu chcete stornovat toto hromadné vyúčtování?\nKafařům se vrátí káva k úhradě a spotřebované suroviny se vloží zpět na sklad.", 
                    "Potvrdit storno", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (controller.stornovatVyuctovani(v)) {
                        zobrazInformaci("Vyúčtování bylo úspěšně stornováno.");
                        zobrazPanelUctenek(); // refresh tabulky
                    } else {
                        zobrazChybu("Nelze stornovat! Některé účtenky z tohoto vyúčtování již byly zaplaceny.");
                    }
                }
            }
        }
    }

    private void akceVypitKavu() {
        controller.vypitKavu();
    }        
    
    private void akceUctenky() {
        zobrazPanelUctenek();
    }

    private void akcePrihlasit() {
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);

        if (loginDialog.isSucceeded()) {
            controller.zpracujPrihlaseni(loginDialog.getLogin(), loginDialog.getHeslo());
        }
    }

    private void akceOdhlasit() {
        controller.odhlasit();
    }

    private void akceZmenitHeslo() {
        ZmenaHeslaDialog dialog = new ZmenaHeslaDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isSucceeded()) {
            if (controller.zmenitHeslo(dialog.getStareHeslo(), dialog.getNoveHeslo())) {
                zobrazInformaci("Heslo bylo úspěšně změněno.");
            } else {
                zobrazChybu("Zadané staré heslo není správné.");
            }
        }
    }

    private void akceZalozitUzivatele() {
        NewUserDialog newUserDialog = new NewUserDialog(this, controller.getKafari());
        newUserDialog.setVisible(true);

        if (newUserDialog.isSucceeded()) {
            controller.zalozitUzivatele(newUserDialog.getLogin(), newUserDialog.getHeslo());
        }
    }

    private void akcePrepnoutNaKafare() {
        zobrazPanelKafaru();
    }

    private void akcePrepnoutNaSklad() {
        zobrazPanelSkladu();
    }

    private void setButtonsVisible(boolean uctenky, boolean vypit, boolean odhlasit, boolean zmenitHeslo, boolean prihlasit, boolean zalozit, boolean kafari,
            boolean sklad, boolean naskladnit, boolean upravitSklad, boolean vyuctovat, boolean stornoVyuctovani, boolean expHistKafare) {
        uctenkyButton.setVisible(uctenky);
        vypitButton.setVisible(vypit);
        odhlasitButton.setVisible(odhlasit);
        zmenitHesloButton.setVisible(zmenitHeslo);
        prihlasitButton.setVisible(prihlasit);
        zalozitUzivateleButton.setVisible(zalozit);
        kafariButton.setVisible(kafari);
        skladButton.setVisible(sklad);
        naskladnitButton.setVisible(naskladnit);
        upravitPolozkuButton.setVisible(upravitSklad);
        vyuctovatButton.setVisible(vyuctovat);
        stornovatVyuctovaniButton.setVisible(stornoVyuctovani);
        exportHistorieKafareButton.setVisible(expHistKafare);
    }

    public void updateView() {
        emptyPanel.removeAll();
        if (nacitaSe) {
            JPanel loadingPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(10, 10, 10, 10);
            
            JLabel loadingLabel = new JLabel("Načítám data ze sítě, čekejte prosím...");
            loadingLabel.setFont(new Font("Arial", Font.BOLD, 18));
            loadingPanel.add(loadingLabel, gbc);
            
            gbc.gridy = 1;
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setPreferredSize(new Dimension(300, 20));
            loadingPanel.add(progressBar, gbc);
            
            emptyPanel.add(loadingPanel);
            
            setButtonsVisible(false, false, false, false, false, false, false, false, false, false, false, false, false);
            menuItemPrihlasit.setVisible(false);
            menuItemOdhlasit.setVisible(false);
            menuItemZmenitHeslo.setVisible(false);
            menuItemExportZalohy.setVisible(false);
            menuItemImportZalohy.setVisible(false);
        } else {
            String prihlasenyUzivatel = controller.getPrihlasenyUzivatel();

            if (prihlasenyUzivatel == null) {
                emptyPanel.add(welcomePanel);
                setButtonsVisible(false, false, false, false, true, true, false, false, false, false, false, false, false);
            } else if (controller.isAdmin()) {
                setButtonsVisible(true, false, true, true, false, true, true, true, true, true, true, true, false);
            }
            else {
                int[] stat = controller.getStatistikyPrihlasenehoKafare();
                uctenkyPanel.obnovData(controller.getSeznamVyuctovani(), prihlasenyUzivatel);

                JPanel uctenkySekce = new JPanel(new BorderLayout(0, 5));
                JLabel nadpisUctenky = new JLabel("Historie vyúčtování a platby");
                nadpisUctenky.setFont(new Font("Arial", Font.BOLD, 18));
                nadpisUctenky.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 0));
                uctenkySekce.add(nadpisUctenky, BorderLayout.NORTH);
                uctenkySekce.add(uctenkyPanel, BorderLayout.CENTER);

                userPanel.obnovData(prihlasenyUzivatel, stat[0], stat[1], stat[2]);
                
                JPanel souhrnyPanel = new JPanel(new GridLayout(2, 1, 0, 10));
                souhrnyPanel.add(userPanel);
                souhrnyPanel.add(uctenkySekce);
                emptyPanel.add(souhrnyPanel);

                setButtonsVisible(false, true, true, true, false, false, false, false, false, false, false, false, true);
            }
            
            // Aktualizace viditelnosti položek v horním menu
            menuItemPrihlasit.setVisible(prihlasenyUzivatel == null);
            menuItemOdhlasit.setVisible(prihlasenyUzivatel != null);
            menuItemZmenitHeslo.setVisible(prihlasenyUzivatel != null);
            menuItemExportZalohy.setVisible(controller.isAdmin());
            menuItemImportZalohy.setVisible(controller.isAdmin());
        }
        
        emptyPanel.revalidate();
        emptyPanel.repaint();
    }

    private void nastavOdsazeni(JTable table, int padding) {
        var standardMargin = javax.swing.BorderFactory.createEmptyBorder(0, padding, 0, padding);
        var indentedMargin = javax.swing.BorderFactory.createEmptyBorder(0, padding + 10, 0, padding);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);

                // Zjistí se login uživatele na aktuálním řádku (vždy ze sloupce 0)
                // Je nutné převést index řádku, protože tabulka může být seřazená
                int modelRow = t.convertRowIndexToModel(r);
                String login = t.getModel().getValueAt(modelRow, 0).toString();

                // Pokud se jedná o první sloupec (Jméno) a login NENÍ admin, provede se odsazení
                if (c == 0 && controller.getAdmin() != null && !login.equals(controller.getAdmin().getLogin())) {
                    setBorder(indentedMargin);
                } else {
                    setBorder(standardMargin);
                }

                return this;
            }
        };

        // Renderer se aplikuje na sloupce (vynechají se Boolean/Tlačítka)
        for (int i = 0; i < table.getColumnCount(); i++) {
            Class<?> columnClass = table.getColumnClass(i);
            // Tady pozor: pokud již byl přidán ButtonRenderer, také se přeskočí
            if (columnClass != Boolean.class && columnClass != JButton.class) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
        }
    }

    public Vyuctovani najdiVyuctovaniZRadku(int modelRow) {
        String loginTab = uctenkyPanel.getTableModel().getValueAt(modelRow, 0).toString();
        LocalDate datumTab = (LocalDate) uctenkyPanel.getTableModel().getValueAt(modelRow, 2);
        BigDecimal cenaTab = (BigDecimal) uctenkyPanel.getTableModel().getValueAt(modelRow, 4);

        return controller.najdiVyuctovani(loginTab, datumTab, cenaTab);
    }

    public String vyberSlozku(String titulek, String vychoziSlozka) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(titulek);
        if (vychoziSlozka != null && !vychoziSlozka.isEmpty()) {
            chooser.setCurrentDirectory(new java.io.File(vychoziSlozka));
        } else {
            chooser.setCurrentDirectory(new java.io.File("."));
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public void otevriPlatbu(Vyuctovani v) {
        PlatbaDialog platbaDialog = new PlatbaDialog(this, v, controller.getAdmin(), controller.getPrihlasenyUzivatel());
        platbaDialog.setVisible(true);
        if (platbaDialog.isSucceeded()) {
            controller.zpracujPlatbu(v);
        }
    }

}