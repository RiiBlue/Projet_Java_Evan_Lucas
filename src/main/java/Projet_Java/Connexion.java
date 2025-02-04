package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Properties;
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

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(44, 62, 80), getWidth(), getHeight(), new Color(52, 152, 219));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        JLabel title = new JLabel("Connexion", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(title, gbc);

        JLabel emailLabel = new JLabel("Email :");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emailLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        contentPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20);
        gbc.gridy = 2;
        contentPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Mot de passe :");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridy = 3;
        contentPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridy = 4;
        contentPanel.add(passwordField, gbc);

        JButton submitButton = new JButton("Se connecter");
        submitButton.setBackground(new Color(41, 128, 185));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setBorderPainted(false);
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 50, 10, 50);
        contentPanel.add(submitButton, gbc);

        JPanel signupPanel = new JPanel();
        signupPanel.setOpaque(false);
        JLabel signupRedirect = new JLabel("Pas encore inscrit ? ");
        signupRedirect.setFont(new Font("Arial", Font.PLAIN, 14));
        signupRedirect.setForeground(Color.WHITE);
        JButton signupButton = new JButton("S'inscrire");
        signupButton.setBackground(new Color(231, 76, 60));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFont(new Font("Arial", Font.BOLD, 14));
        signupButton.setBorderPainted(false);
        signupPanel.add(signupRedirect);
        signupPanel.add(signupButton);
        gbc.gridy = 6;
        contentPanel.add(signupPanel, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        frame.add(mainPanel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> handleConnexion(emailField.getText(), new String(passwordField.getPassword()), frame));
        signupButton.addActionListener(e -> new Inscription().afficherInscription());
        signupButton.addActionListener(e -> frame.dispose());
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
                        int userId = rs.getInt("id");
                        SessionManager.startSession(email, role, pseudo, String.valueOf(userId));
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