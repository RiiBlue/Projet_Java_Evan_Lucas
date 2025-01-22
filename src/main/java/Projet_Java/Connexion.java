package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import org.mindrot.jbcrypt.BCrypt;

public class Connexion {

    private Properties dbProperties;

    public Connexion() {
        dbProperties = new Properties();
        try (FileInputStream fis = new FileInputStream("C:\\Users\\geret\\IdeaProjects\\Projet_Java_Evan_Lucas\\src\\main\\resources\\db.properties")) {
            dbProperties.load(fis);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erreur de chargement des propriétés de la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void afficherConnexion() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Connexion");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(242, 242, 242));
        panel.setPreferredSize(new Dimension(400, 300));

        JPanel containerPanel = new JPanel(new GridBagLayout());
        containerPanel.setBackground(new Color(242, 242, 242));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        containerPanel.add(panel, gbc);

        JLabel emailLabel = new JLabel("Email :");
        JTextField emailField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Mot de passe :");
        JPasswordField passwordField = new JPasswordField(20);

        JButton submitButton = new JButton("Connexion");
        submitButton.setBackground(Color.WHITE);
        submitButton.setForeground(Color.BLACK);
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setPreferredSize(new Dimension(150, 40));
        submitButton.setFocusPainted(false);
        submitButton.setOpaque(true);
        submitButton.setContentAreaFilled(true);

        panel.add(Box.createVerticalStrut(20));
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(submitButton);

        frame.add(containerPanel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> handleConnexion(emailField.getText(), new String(passwordField.getPassword())));
    }

    private void handleConnexion(String email, String password) {
        if (email == null || email.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Veuillez entrer un email.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password == null || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Veuillez entrer un mot de passe.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            if (!isEmailInDatabase(connection, email)) {
                JOptionPane.showMessageDialog(null, "L'email n'existe pas dans la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isPasswordCorrect(connection, email, password)) {
                JOptionPane.showMessageDialog(null, "Mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(null, "Connexion réussie !", "Succès", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isEmailInDatabase(Connection connection, String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean isPasswordCorrect(Connection connection, String email, String password) throws SQLException {
        String query = "SELECT password FROM users WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("password");
                    return BCrypt.checkpw(password, hashedPassword);
                }
            }
        }
        return false;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Connexion().afficherConnexion());
    }
}
