package org.example;

import javax.swing.*;
import java.awt.*;

public class SimulateurSignaux extends JFrame {
    private JTextField inputField;
    private JComboBox<String> codeSelector;
    private DrawingPanel drawPanel;
    private final int STEP = 60;
    private final int AMPLITUDE = 40;

    public SimulateurSignaux() {
        setTitle("Simulateur de Codage en Ligne");
        setSize(900, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Barre de contrôle
        JPanel controlPanel = new JPanel();
        inputField = new JTextField("01101", 10);
        String[] codes = {"NRZ", "Manchester", "Manchester Différentiel", "Miller"};
        codeSelector = new JComboBox<>(codes);
        JButton btnTracer = new JButton("Tracer");

        controlPanel.add(new JLabel("Binaire:"));
        controlPanel.add(inputField);
        controlPanel.add(new JLabel("Codage:"));
        controlPanel.add(codeSelector);
        controlPanel.add(btnTracer);

        drawPanel = new DrawingPanel();
        drawPanel.setBackground(Color.WHITE);

        add(controlPanel, BorderLayout.NORTH);
        add(drawPanel, BorderLayout.CENTER);

        btnTracer.addActionListener(e -> drawPanel.repaint());
    }

    class DrawingPanel extends JPanel {
        private int lastY; // Pour mémoriser l'état précédent (Miller/Diff)

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String bits = inputField.getText().replaceAll("[^01]", "");
            if (bits.isEmpty()) return;

            int x = 50;
            int baseline = getHeight() / 2;
            int yHigh = baseline - AMPLITUDE;
            int yLow = baseline + AMPLITUDE;

            // Initialisation du niveau de départ
            lastY = yLow;
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.BLUE);

            for (int i = 0; i < bits.length(); i++) {
                char bit = bits.charAt(i);
                char nextBit = (i + 1 < bits.length()) ? bits.charAt(i + 1) : ' ';
                String mode = (String) codeSelector.getSelectedItem();

                switch (mode) {
                    case "NRZ":
                        drawNRZ(g2, bit, x, yHigh, yLow);
                        break;
                    case "Manchester":
                        drawManchester(g2, bit, x, yHigh, yLow);
                        break;
                    case "Manchester Différentiel":
                        drawManchesterDiff(g2, bit, x, yHigh, yLow);
                        break;
                    case "Miller":
                        drawMiller(g2, bit, nextBit, x, yHigh, yLow);
                        break;
                }

                // Dessin du texte du bit et grille
                g2.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke(1));
                g2.drawString(String.valueOf(bit), x + STEP/2 - 5, baseline - 60);
                g2.drawLine(x + STEP, baseline - 70, x + STEP, baseline + 70);
                g2.setStroke(new BasicStroke(3));
                g2.setColor(Color.BLUE);

                x += STEP;
            }
        }

        private void drawNRZ(Graphics2D g, char bit, int x, int yH, int yL) {
            int targetY = (bit == '1') ? yH : yL;
            if (x > 50) g.drawLine(x, lastY, x, targetY); // Transition verticale
            g.drawLine(x, targetY, x + STEP, targetY);
            lastY = targetY;
        }

        private void drawManchester(Graphics2D g, char bit, int x, int yH, int yL) {
            if (bit == '0') { // Transition montante (Bas -> Haut)
                g.drawLine(x, yL, x + STEP/2, yL);
                g.drawLine(x + STEP/2, yL, x + STEP/2, yH);
                g.drawLine(x + STEP/2, yH, x + STEP, yH);
            } else { // Transition descendante (Haut -> Bas)
                g.drawLine(x, yH, x + STEP/2, yH);
                g.drawLine(x + STEP/2, yH, x + STEP/2, yL);
                g.drawLine(x + STEP/2, yL, x + STEP, yL);
            }
        }

        private void drawManchesterDiff(Graphics2D g, char bit, int x, int yH, int yL) {
            // Si bit='0', transition au début du bit. Si '1', pas de transition au début.
            // Il y a TOUJOURS une transition au milieu.
            if (bit == '0') lastY = (lastY == yH) ? yL : yH;

            int midY = (lastY == yH) ? yL : yH;
            g.drawLine(x, lastY, x + STEP/2, lastY); // 1ère moitié
            g.drawLine(x + STEP/2, lastY, x + STEP/2, midY); // Milieu
            g.drawLine(x + STEP/2, midY, x + STEP, midY); // 2ème moitié
            lastY = midY;
        }

        private void drawMiller(Graphics2D g, char bit, char nextBit, int x, int yH, int yL) {
            if (bit == '1') {
                // Transition au milieu du bit
                int nextY = (lastY == yH) ? yL : yH;
                g.drawLine(x, lastY, x + STEP/2, lastY);
                g.drawLine(x + STEP/2, lastY, x + STEP/2, nextY);
                g.drawLine(x + STEP/2, nextY, x + STEP, nextY);
                lastY = nextY;
            } else { // bit == '0'
                // Pas de transition au milieu. Transition à la fin si le suivant est '0'.
                g.drawLine(x, lastY, x + STEP, lastY);
                if (nextBit == '0') {
                    int nextY = (lastY == yH) ? yL : yH;
                    g.drawLine(x + STEP, lastY, x + STEP, nextY);
                    lastY = nextY;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulateurSignaux().setVisible(true));
    }
}