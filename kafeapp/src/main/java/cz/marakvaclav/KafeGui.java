package cz.marakvaclav;

import java.util.List;

import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class KafeGui extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Kafar> kafari;
    private String prihlasenyUzivatel = null;

    public KafeGui(List<Kafar> kafari) {
        this.kafari = kafari;
        
        setTitle("KafeApp - Evidence");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600,400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] sloupce = {"Uzivatel (login)", "Nezaplacene kavy"};
        tableModel = new DefaultTableModel(sloupce, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Kafar k : kafari) {
            tableModel.addRow(new Object[]{k.getLogin(), k.getPocetVypitychKav()});
        }

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelTlacitek = new JPanel();
        JButton zaplatitButton = new JButton("Zaplatit");
        JButton vypitButton = new JButton("Vypit kavu");
        JButton prihlasitButton = new JButton("Prihlasit");
        JButton odhlasitButton = new JButton("Odhlasit");


        zaplatitButton.addActionListener(e -> akceZaplatit());
        vypitButton.addActionListener(e -> akceVypitKavu());
        prihlasitButton.addActionListener(e -> akcePrihlasit());
        odhlasitButton.addActionListener(e -> akceOdhlasit());



        panelTlacitek.add(zaplatitButton);
        panelTlacitek.add(vypitButton);
        panelTlacitek.add(prihlasitButton);
        panelTlacitek.add(odhlasitButton);


        add(panelTlacitek, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void akceVypitKavu() {
        int vybranyRadek = table.getSelectedRow();
        if (vybranyRadek >= 0) {
            Kafar k = kafari.get(vybranyRadek);
            k.vypijKavu();
            tableModel.setValueAt(k.getPocetVypitychKav(), vybranyRadek, 1);
        }
    }

    private void akceZaplatit() {
        int vybranyRadek = table.getSelectedRow();
        if (vybranyRadek >= 0) {
            Kafar k = kafari.get(vybranyRadek);
            k.zaplatit();
            tableModel.setValueAt(k.getPocetVypitychKav(), vybranyRadek, 1);
        }
    }

    private void akcePrihlasit() {
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);

        if (loginDialog.isSucceeded()) {
            String login = loginDialog.getLogin();
            String heslo = loginDialog.getHeslo();
            System.out.println("Pokus o prihlaseni: " + login + " s heslem: " + heslo);
            prihlasenyUzivatel = login;
        }
    }

    private void akceOdhlasit() {
        prihlasenyUzivatel = null;
    }
}
