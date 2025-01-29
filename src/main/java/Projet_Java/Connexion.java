package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import org.mindrot.jbcrypt.BCrypt;

public class Connexion {

    private Properties dbProperties;

    public Connexion() {
        dbProperties = new Properties();
        try {
            dbProperties.load(getClass().getResourceAsStream("/db.properties"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur de chargement des propriétés de la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new Connexion().afficherConnexion();
    }

    public void afficherConnexion() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Connexion");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel navBar = new JPanel();
        navBar.setBackground(new Color(33, 37, 41)); // Couleur sombre pour la navbar
        navBar.setLayout(new FlowLayout(FlowLayout.LEFT)); // Aligner à droite
        JButton homeButton = new JButton("Accueil");
        homeButton.setBackground(new Color(0, 123, 255)); // Bleu
        homeButton.setForeground(Color.WHITE);
        homeButton.setFont(new Font("Arial", Font.PLAIN, 14));

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                String email = "";
                new Main().createMainFrame(email);
            }
        });
        navBar.add(homeButton);
        mainPanel.add(navBar, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(242, 242, 242));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 50, 5, 50);
        gbc.weightx = 1;

        JLabel title = new JLabel("Connexion", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 50, 30, 50);
        contentPanel.add(title, gbc);

        JLabel emailLabel = new JLabel("Email :", SwingConstants.LEFT);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20); // Pas besoin de dimension spécifique
        gbc.gridy = 2;
        emailField.setPreferredSize(new Dimension(400, 100)); // Ajuste la taille
        contentPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Mot de passe :", SwingConstants.LEFT);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20); // Pas besoin de dimension spécifique
        gbc.gridy = 4;
        passwordField.setPreferredSize(new Dimension(400, 100));
        contentPanel.add(passwordField, gbc);

        JButton submitButton = new JButton("Se connecter");
        gbc.gridy = 5;
        gbc.insets = new Insets(30, 750, 30, 750);
        contentPanel.add(submitButton, gbc);

        JLabel signupRedirect = new JLabel("Pas encore inscrit ? ");
        signupRedirect.setFont(new Font("Arial", Font.PLAIN, 12));
        signupRedirect.setForeground(Color.BLACK);

        JButton signupButton = new JButton("S'inscrire");
        signupButton.addActionListener(e -> {
            frame.dispose();
            new Inscription().afficherInscription();
        });

        JPanel signupPanel = new JPanel();
        signupPanel.setBackground(new Color(242, 242, 242));
        signupPanel.add(signupRedirect);
        signupPanel.add(signupButton);
        gbc.gridy = 6;
        contentPanel.add(signupPanel, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> {
            handleConnexion(emailField.getText(), new String(passwordField.getPassword()), frame);
        });
    }

    private void handleConnexion(String email, String password, JFrame frame) {
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Email manquant.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Mot de passe manquant.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT password, rôle, pseudo, id FROM users WHERE email = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (BCrypt.checkpw(password, storedPassword)) {
                        String role = rs.getString("rôle");
                        String pseudo = rs.getString("pseudo");
                        int userId = rs.getInt("id");  // Récupérer l'ID
                        SessionManager.startSession(email, role, pseudo, String.valueOf(userId));  // Passer l'ID à la session

                        JOptionPane.showMessageDialog(null, "Connexion réussie !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose();
                        new Main().createMainFrame(email);
                    } else {
                        JOptionPane.showMessageDialog(null, "Mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Email non trouvé.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
