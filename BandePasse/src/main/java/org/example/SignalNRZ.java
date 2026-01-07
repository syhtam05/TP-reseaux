package org.example;

import javax.swing.*;
import java.awt.*;

public class SignalNRZ extends JFrame {
    private JTextField inputField;
    private DrawingPanel drawPanel;
    private final int STEP = 60; // Largeur de chaque bit
    private final int AMPLITUDE = 50;

    public SignalNRZ() {
        setTitle("Générateur de Signal NRZ");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Interface ---
        JPanel topPanel = new JPanel();
        inputField = new JTextField("01101", 15);
        JButton drawButton = new JButton("Tracer NRZ");

        topPanel.add(new JLabel("Chaîne Binaire :"));
        topPanel.add(inputField);
        topPanel.add(drawButton);

        drawPanel = new DrawingPanel();
        drawPanel.setBackground(Color.WHITE);

        add(topPanel, BorderLayout.NORTH);
        add(drawPanel, BorderLayout.CENTER);

        drawButton.addActionListener(e -> drawPanel.repaint());
    }

    class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(3));

            String binary = inputField.getText().replaceAll("[^01]", "");
            int x = 50;
            int baseline = getHeight() / 2;
            int yHigh = baseline - AMPLITUDE;
            int yLow = baseline + AMPLITUDE; // On utilise souvent un niveau bipolaire pour le NRZ

            // Dessin de la ligne de base (0V)
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(20, baseline, getWidth() - 20, baseline);

            if (binary.isEmpty()) return;

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3));

            // Déterminer la position verticale initiale
            int currentY = (binary.charAt(0) == '1') ? yHigh : yLow;

            for (int i = 0; i < binary.length(); i++) {
                int nextY = (binary.charAt(i) == '1') ? yHigh : yLow;

                // 1. Tracer la transition verticale si le bit change
                if (i > 0 && nextY != currentY) {
                    g2.drawLine(x, currentY, x, nextY);
                }

                // 2. Tracer le plateau horizontal pour le bit actuel
                g2.drawLine(x, nextY, x + STEP, nextY);

                // 3. Dessiner le bit textuellement au-dessus
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.drawString(String.valueOf(binary.charAt(i)), x + (STEP / 2) - 5, yHigh - 20);

                currentY = nextY;
                x += STEP;

                // Traits de séparation verticaux (pointillés)
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(x, baseline - 70, x, baseline + 70);
                g2.setStroke(new BasicStroke(3));
                g2.setColor(Color.RED);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SignalNRZ().setVisible(true));
    }
}