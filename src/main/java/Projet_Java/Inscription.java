package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Properties;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public class Inscription {

    private Properties dbProperties;

    public Inscription() {
        dbProperties = new Properties();
        try {
            dbProperties.load(getClass().getResourceAsStream("/db.properties"));
        } catch (Exception e) {
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

        JLabel title = new JLabel("Inscription", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(title, gbc);

        JLabel namelabel = new JLabel("Pseudo :");
        namelabel.setFont(new Font("Arial", Font.PLAIN, 16));
        namelabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        contentPanel.add(namelabel, gbc);

        JTextField nameField = new JTextField(20);
        gbc.gridy = 2;
        contentPanel.add(nameField, gbc);

        JLabel emailLabel = new JLabel("Email :");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emailLabel.setForeground(Color.WHITE);
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        contentPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20);
        gbc.gridy = 4;
        contentPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Mot de passe :");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridy = 5;
        contentPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridy = 6;
        contentPanel.add(passwordField, gbc);

        JButton submitButton = new JButton("S'inscrire");
        submitButton.setBackground(new Color(41, 128, 185));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setBorderPainted(false);
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 50, 10, 50);
        contentPanel.add(submitButton, gbc);

        JPanel loginPanel = new JPanel();
        loginPanel.setOpaque(false);
        JLabel loginRedirect = new JLabel("Déjà Inscrit ? ");
        loginRedirect.setFont(new Font("Arial", Font.PLAIN, 14));
        loginRedirect.setForeground(Color.WHITE);
        JButton loginButton = new JButton("Se connecter");
        loginButton.setBackground(new Color(231, 76, 60));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBorderPainted(false);
        loginPanel.add(loginRedirect);
        loginPanel.add(loginButton);
        gbc.gridy = 9;
        contentPanel.add(loginPanel, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        frame.add(mainPanel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> handleInscription(nameField.getText(), emailField.getText(), new String(passwordField.getPassword()), frame));
        loginButton.addActionListener(e -> new Connexion().afficherConnexion());
        loginButton.addActionListener(e -> frame.dispose());

        submitButton.addActionListener(e -> {
            handleInscription(nameField.getText(), emailField.getText(), new String(passwordField.getPassword()), frame);
        });
    }

    private void handleInscription(String name, String email, String password, JFrame frame) {
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pseudo manquant.", "Erreur", JOptionPane.ERROR_MESSAGE);
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
