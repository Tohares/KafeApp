package cz.marakvaclav.dialogy;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.function.Supplier;

public class CekaniNaZamekDialog extends JDialog {
    private boolean uspech = false;

    public CekaniNaZamekDialog(Supplier<Boolean> pokusZiskatZamek) {
        super((java.awt.Frame)null, "Čekání na uvolnění dat", true);
        setLayout(new BorderLayout(10, 10));
        
        final int MAX_CEKANI_SEKUND = 10;
        JLabel label = new JLabel("Data právě zpracovává jiný proces. Čekám... " + MAX_CEKANI_SEKUND + " s", SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(label, BorderLayout.CENTER);
        
        JButton stornoBtn = new JButton("Zrušit operaci");
        stornoBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(stornoBtn);
        add(btnPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final int[] zbyvaPokusu = {MAX_CEKANI_SEKUND * 2};
        
        Timer timer = new Timer(500, null);
        timer.addActionListener(e -> {
            if (pokusZiskatZamek.get()) {
                uspech = true;
                dispose();
                return;
            }
            
            zbyvaPokusu[0]--;
            int zbyvaSekund = (int) Math.ceil(zbyvaPokusu[0] / 2.0);
            label.setText("Data právě zpracovává jiný proces. Čekám... " + zbyvaSekund + " s");
            
            if (zbyvaPokusu[0] <= 0) {
                dispose(); // Čas vypršel
            }
        });
        
        // Jakmile se okno zavře (odpočtem, stornem nebo křížkem), je nutné Timer zastavit
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                timer.stop();
            }
        });

        timer.start();
    }

    public boolean isUspech() {
        return uspech;
    }
}