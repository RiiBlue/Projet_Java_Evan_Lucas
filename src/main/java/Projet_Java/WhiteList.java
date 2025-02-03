package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Properties;
import java.util.regex.Pattern;
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

    private JPanel createNavBar(JFrame frame) {
        JPanel navBar = new JPanel();
        navBar.setBackground(new Color(33, 37, 41));
        navBar.setLayout(new FlowLayout(FlowLayout.RIGHT));

        String role = SessionManager.getCurrentUserRole(); // Assurer que tu as une méthode qui récupère le rôle

        if ("administrateur".equals(role)) {
            // Si c'est l'admin, on affiche le bouton Choix Magasin
            JButton choiceStoreButton = new JButton("Choix Magasin");
            choiceStoreButton.setBackground(new Color(0, 123, 255));
            choiceStoreButton.setForeground(new Color(0, 123, 255));
            choiceStoreButton.setFont(new Font("Arial", Font.PLAIN, 14));
            choiceStoreButton.addActionListener(e -> {
                frame.dispose();
                new ChoixMagasin().afficherChoixMagasin();
            });
            navBar.add(choiceStoreButton);

            JButton whiteListButton = new JButton("Liste blanche");
            whiteListButton.setBackground(new Color(0, 123, 255));
            whiteListButton.setForeground(new Color(0, 123, 255));
            whiteListButton.setFont(new Font("Arial", Font.PLAIN, 14));
            whiteListButton.addActionListener(e -> {
                frame.dispose();
                new WhiteList().afficherWhiteList();
            });
            navBar.add(whiteListButton);

            JButton manageEmployeeButton = new JButton("Gérer Employés");
            manageEmployeeButton.setBackground(new Color(40, 167, 69)); // Couleur verte
            manageEmployeeButton.setForeground(new Color(40, 167, 69));
            manageEmployeeButton.setFont(new Font("Arial", Font.PLAIN, 14));
            manageEmployeeButton.addActionListener(e -> {
                frame.dispose();
                new ManageEmployee().afficherManageEmployee();
            });
            navBar.add(manageEmployeeButton);
        } else {
            JButton inventoryButton = new JButton("Inventaire");
            inventoryButton.setBackground(new Color(220, 53, 69)); // Rouge
            inventoryButton.setForeground(new Color(220, 53, 69));
            inventoryButton.setFont(new Font("Arial", Font.PLAIN, 14));
            inventoryButton.addActionListener(e -> {
                if (SessionManager.isLoggedIn()) {
                    frame.dispose();
                    new Inventory().afficherInventory();
                } else {
                    JOptionPane.showMessageDialog(null, "Vous devez être connecté pour accéder à l'inventaire.");
                    new Connexion().afficherConnexion();
                }
            });
            navBar.add(inventoryButton);
        }

        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.setBackground(new Color(220, 53, 69)); // Rouge
        logoutButton.setForeground(new Color(220, 53, 69));
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.addActionListener(e -> {
            SessionManager.endSession();
            frame.dispose();
            new Connexion().afficherConnexion();
        });

        if (SessionManager.isLoggedIn()) {
            navBar.add(logoutButton);
        }
        return navBar;
    }

    public void afficherWhiteList() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!SessionManager.isLoggedIn() || !"administrateur".equals(SessionManager.getCurrentUserRole())) {
            JOptionPane.showMessageDialog(null, "Accès non autorisé.", "Erreur", JOptionPane.ERROR_MESSAGE);
            new Connexion().afficherConnexion();
            return;
        }

        JFrame frame = new JFrame("WhiteList");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Ajout de la barre de navigation
        JPanel navBar = createNavBar(frame);
        mainPanel.add(navBar, BorderLayout.NORTH);

        // Panel principal avec les champs et boutons
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(242, 242, 242));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 50, 5, 50);
        gbc.weightx = 1;

        // Titre de la page
        JLabel title = new JLabel("White Liste", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 50, 30, 50);
        contentPanel.add(title, gbc);

        // Label et champ pour l'email
        JLabel emailLabel = new JLabel("Email :", SwingConstants.LEFT);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 25));
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(emailField.getPreferredSize().width, 100));
        gbc.gridy = 4;
        contentPanel.add(emailField, gbc);

        // Bouton d'ajout
        JButton submitButton = new JButton("Ajouter");
        gbc.gridy = 7;
        gbc.insets = new Insets(30, 750, 30, 750);
        contentPanel.add(submitButton, gbc);

        // Label pour choisir un employé
        JLabel employeeLabel = new JLabel("Sélectionner un employé:");
        gbc.gridy = 1;
        contentPanel.add(employeeLabel, gbc);

        // Liste déroulante des employés
        JComboBox<String> employeeComboBox = new JComboBox<>();
        gbc.gridy = 2;
        contentPanel.add(employeeComboBox, gbc);

        // Bouton de suppression
        JButton deleteButton = new JButton("Retirer");
        gbc.gridy = 7;
        gbc.insets = new Insets(-30, 750, 30, 750);
        contentPanel.add(deleteButton, gbc);

        // Chargement des employés dans la liste déroulante
        loadEmployee(employeeComboBox);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> {
            handleWhiteList(emailField.getText(), frame);
        });

        deleteButton.addActionListener(e -> {
            delWhiteList(employeeComboBox.getSelectedItem().toString(), frame);
        });
    }

    private void loadEmployee(JComboBox<String> employeeComboBox) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT email FROM white_list WHERE email != 'administrateur@istore.fr'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                employeeComboBox.addItem(rs.getString("email"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
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
            new WhiteList().afficherWhiteList();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void delWhiteList(String email, JFrame frame) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            deleteUser(connection, email);
            JOptionPane.showMessageDialog(null, "Email supprimé avec succès de la liste blanche !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            new WhiteList().afficherWhiteList();

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

    private void deleteUser(Connection connection, String email) throws SQLException {
        String query = "DELETE FROM white_list WHERE email = ?";
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
