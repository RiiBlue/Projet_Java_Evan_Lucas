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

        // Action du bouton Accueil
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Fermer la fenêtre actuelle
                frame.dispose();
                // Ouvrir la fenêtre principale
                new Main().createMainFrame();
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

        JLabel title = new JLabel("Inscription", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 50, 30, 50);
        contentPanel.add(title, gbc);

        JLabel nameLabel = new JLabel("Pseudo :", SwingConstants.LEFT);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 25));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(nameField.getPreferredSize().width, 100));
        gbc.gridy = 2;
        contentPanel.add(nameField, gbc);

        JLabel emailLabel = new JLabel("Email :", SwingConstants.LEFT);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 25));
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(emailField.getPreferredSize().width, 100));
        gbc.gridy = 4;
        contentPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Mot de passe :", SwingConstants.LEFT);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 25));
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 100));
        gbc.gridy = 6;
        contentPanel.add(passwordField, gbc);

        JButton submitButton = new JButton("S'inscrire");
        gbc.gridy = 7;
        gbc.insets = new Insets(30, 750, 30, 750);
        contentPanel.add(submitButton, gbc);

        JLabel loginRedirect = new JLabel("Déjà inscrit ? ");
        loginRedirect.setFont(new Font("Arial", Font.PLAIN, 12));
        loginRedirect.setForeground(Color.BLACK);

        JButton loginButton = new JButton("Connexion");
        loginButton.addActionListener(e -> {
            frame.dispose();
            new Connexion().afficherConnexion();
        });

        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(new Color(242, 242, 242));
        loginPanel.add(loginRedirect);
        loginPanel.add(loginButton);
        gbc.gridy = 8;
        contentPanel.add(loginPanel, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> {
            handleInscription(nameField.getText(), emailField.getText(), new String(passwordField.getPassword()), frame);
        });
    }

    private void handleInscription(String name, String email, String password, JFrame frame) {
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

            if (!isEmailInWhitelist(connection, email)) {
                JOptionPane.showMessageDialog(null, "L'email n'est pas sur liste blanche.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEmailAlreadyUsed(connection, email)) {
                JOptionPane.showMessageDialog(null, "Cet email est déjà utilisé.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            insertUser(connection, name, email, hashedPassword);
            JOptionPane.showMessageDialog(null, "Inscription réussie !", "Succès", JOptionPane.INFORMATION_MESSAGE);

            frame.dispose();
            new Connexion().afficherConnexion();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", email);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                Pattern.compile(".*[A-Z].*").matcher(password).find() &&
                Pattern.compile(".*[a-z].*").matcher(password).find() &&
                Pattern.compile(".*\\d.*").matcher(password).find() &&
                Pattern.compile(".*[!@#$%^&*(),.?\\\":{}|<>].*").matcher(password).find();
    }

    private boolean isEmailInWhitelist(Connection connection, String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM white_list WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean isEmailAlreadyUsed(Connection connection, String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void insertUser(Connection connection, String name, String email, String password) throws SQLException {
        String query = "INSERT INTO users (pseudo, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();
        }
    }
}
