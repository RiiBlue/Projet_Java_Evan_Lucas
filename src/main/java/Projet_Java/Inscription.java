package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Properties;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.IOException;

public class Inscription {

    private Properties dbProperties;

    public Inscription() {
        dbProperties = new Properties();
        try (FileInputStream fis = new FileInputStream("C:\\Users\\geret\\IdeaProjects\\Projet_Java_Evan_Lucas\\src\\main\\resources\\db.properties")) {
            dbProperties.load(fis);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erreur de chargement des propriétés de la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void afficherInscription() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Inscription");
        frame.setSize(600, 400); // Taille de la fenêtre
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Centrer la fenêtre à l'écran

        //Affichage vertical de tout les panels
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(242, 242, 242));
        panel.setPreferredSize(new Dimension(400, 300)); // Limiter la taille du panel

        //Panel centré
        JPanel containerPanel = new JPanel(new GridBagLayout());
        containerPanel.setBackground(new Color(242, 242, 242));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        containerPanel.add(panel, gbc);

        JLabel nameLabel = new JLabel("Nom de l'entreprise :");
        JTextField nameField = new JTextField(20);

        JLabel emailLabel = new JLabel("Email :");
        JTextField emailField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Mot de passe :");
        JPasswordField passwordField = new JPasswordField(20);

        //tout les trucs du boutons
        JButton submitButton = new JButton("S'inscrire");
        submitButton.setBackground(Color.WHITE); // Fond blanc
        submitButton.setForeground(Color.BLACK); // Texte noir
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setPreferredSize(new Dimension(150, 40));
        submitButton.setFocusPainted(false);
        submitButton.setOpaque(true); // On s'assure que le fond du bouton est totalement opaque
        submitButton.setContentAreaFilled(true); // On désactive les zones de remplissage transparentes

        //pour espacement et ajustement des bloc
        panel.add(Box.createVerticalStrut(20)); // Espacement
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(10)); // Espacement
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(10)); // Espacement
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(20)); // Espacement
        panel.add(submitButton);

        frame.add(containerPanel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> handleInscription(nameField.getText(), emailField.getText(), new String(passwordField.getPassword())));
    }

    private void handleInscription(String name, String email, String password) {
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nom de l'entreprise manquant.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(null, "Veuillez entrer un email valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidPassword(password)) {
            JOptionPane.showMessageDialog(null, "Veuillez entrer un mot de passe valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            // Vérification de la liste blanche
            if (!isEmailInWhitelist(connection, email)) {
                JOptionPane.showMessageDialog(null, "L'email n'est pas sur liste blanche.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Vérification des doublons d'email
            if (isEmailAlreadyUsed(connection, email)) {
                JOptionPane.showMessageDialog(null, "Cet email est déjà utilisé.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insertion dans la base de données
            insertUser(connection, name, email, password);
            JOptionPane.showMessageDialog(null, "Inscription réussie !", "Succès", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    //tout est dans le titre
    private boolean isValidEmail(String email) {
        return Pattern.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", email);
    }
    //tout est dans le titre
    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                Pattern.compile(".*[A-Z].*").matcher(password).find() &&
                Pattern.compile(".*[a-z].*").matcher(password).find() &&
                Pattern.compile(".*\\d.*").matcher(password).find() &&
                Pattern.compile(".*[!@#$%^&*(),.?\\\":{}|<>].*").matcher(password).find();
    }
    //tout est dans le titre
    private boolean isEmailInWhitelist(Connection connection, String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM white_list WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    //tout est dans le titre
    private boolean isEmailAlreadyUsed(Connection connection, String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    //tout est dans le titre
    private void insertUser(Connection connection, String name, String email, String password) throws SQLException {
        String query = "INSERT INTO users (name_compagny, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();
        }
    }
    //lancement du main bg
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Inscription().afficherInscription());
    }
}
