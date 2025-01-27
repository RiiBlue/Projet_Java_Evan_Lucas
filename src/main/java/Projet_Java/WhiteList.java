
package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Properties;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.IOException;
import org.mindrot.jbcrypt.BCrypt;

public class WhiteList {

    private Properties dbProperties;

    public WhiteList() {
        dbProperties = new Properties();
        try {
            dbProperties.load(getClass().getResourceAsStream("/db.properties"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur de chargement des propriétés de la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void afficherWhiteList() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!SessionManager.isLoggedIn() || !"administrateur".equals(SessionManager.getCurrentUserRole())) {
            JOptionPane.showMessageDialog(null, "Accès non autorisé.", "Erreur", JOptionPane.ERROR_MESSAGE);
            new Main().createMainFrame("");
            return;
        }

        JFrame frame = new JFrame("WhiteList");
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
                new Main().createMainFrame(SessionManager.getCurrentUserEmail());
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

        JLabel title = new JLabel("White Liste", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 50, 30, 50);
        contentPanel.add(title, gbc);

        JLabel emailLabel = new JLabel("Email :", SwingConstants.LEFT);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 25));
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(emailField.getPreferredSize().width, 100));
        gbc.gridy = 4;
        contentPanel.add(emailField, gbc);

        JButton submitButton = new JButton("Ajouter");
        gbc.gridy = 7;
        gbc.insets = new Insets(30, 750, 30, 750);
        contentPanel.add(submitButton, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> {
            handleWhiteList(emailField.getText(), frame);
        });
    }

    private void handleWhiteList(String email, JFrame frame) {
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(null, "Veuillez entrer un email valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            if (isEmailInWhitelist(connection, email)) {
                JOptionPane.showMessageDialog(null, "L'email est déjà sur la liste blanche.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            insertUser(connection, email);
            JOptionPane.showMessageDialog(null, "Email ajouté avec succès à la liste blanche !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertUser(Connection connection, String email) throws SQLException {
        String query = "INSERT INTO white_list (email) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        }
    }

    private boolean isEmailInWhitelist(Connection connection, String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM white_list WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", email);
    }
}