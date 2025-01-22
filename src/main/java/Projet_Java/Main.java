package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    private JFrame frame;

    public static void main(String[] args) {
        new Main().createMainFrame();  // Créer la fenêtre principale
    }

    public void createMainFrame() {
        // Créer la fenêtre principale
        frame = new JFrame("Accueil");
        frame.setSize(800, 600); // Taille de la fenêtre
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Centrer la fenêtre
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Création du JPanel principal avec un BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.add(mainPanel);

        // Création de la barre de navigation
        JPanel navBar = new JPanel();
        navBar.setBackground(new Color(33, 37, 41)); // Couleur sombre pour la navbar
        navBar.setLayout(new FlowLayout(FlowLayout.RIGHT)); // Aligner à droite

        // Création du bouton Inscription
        JButton inscriptionButton = new JButton("Inscription");
        inscriptionButton.setBackground(new Color(0, 123, 255)); // Bleu
        inscriptionButton.setForeground(Color.WHITE);
        inscriptionButton.setFont(new Font("Arial", Font.PLAIN, 14));

        // Action du bouton Inscription
        inscriptionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Inscription().afficherInscription();
            }
        });

        // Ajouter le bouton à la navbar
        navBar.add(inscriptionButton);

        // Ajouter la navbar à la fenêtre principale
        mainPanel.add(navBar, BorderLayout.NORTH);

        // Afficher la fenêtre
        frame.setVisible(true);
    }
}
