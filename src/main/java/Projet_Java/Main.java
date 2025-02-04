package Projet_Java;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import org.mindrot.jbcrypt.BCrypt;

public class Main {
    private JFrame frame;
    private Properties dbProperties;
    private String pseudo, email, role, store;
    private DefaultTableModel model;

    public Main() {
        dbProperties = new Properties();
        try {
            dbProperties.load(getClass().getResourceAsStream("/db.properties"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur de chargement des propriétés de la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createMainFrame(String email) {
        frame = new JFrame("Accueil");
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getContentPane().setBackground(new Color(240, 240, 240));

        // Récupérer les informations de l'utilisateur
        loadUserInfo(email);

        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.add(mainPanel);

        JPanel navBar = createNavBar(frame);
        mainPanel.add(navBar, BorderLayout.NORTH);

        JPanel dashboard = createDashboardPanel();
        mainPanel.add(dashboard, BorderLayout.WEST);

        JScrollPane tablePanel = createEmployeeTable();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private JPanel createNavBar(JFrame frame) {
        JPanel navBar = new JPanel();
        navBar.setBackground(new Color(33, 37, 41));
        navBar.setLayout(new FlowLayout(FlowLayout.RIGHT));

        String role = SessionManager.getCurrentUserRole(); // Assurer que tu as une méthode qui récupère le rôle

        if ("administrateur".equals(role)) {
            JButton choiceStoreButton = new JButton("Choix Magasin");
            choiceStoreButton.setBackground(new Color(0, 123, 255));
            choiceStoreButton.setForeground(new Color(0, 123, 255));
            choiceStoreButton.setFont(new Font("Arial", Font.PLAIN, 14));
            choiceStoreButton.addActionListener(e -> {
                frame.dispose();
                new ChoixMagasin().afficherChoixMagasin(SessionManager.getCurrentUserRole());
            });
            navBar.add(choiceStoreButton);

            JButton whiteListButton = new JButton("Liste blanche");
            whiteListButton.setBackground(new Color(0, 123, 255));
            whiteListButton.setForeground(new Color(0, 123, 255));
            whiteListButton.setFont(new Font("Arial", Font.PLAIN, 14));
            whiteListButton.addActionListener(e -> {
                frame.dispose();
                new WhiteList().afficherWhiteList(SessionManager.getCurrentUserRole());
            });
            navBar.add(whiteListButton);

            JButton manageEmployeeButton = new JButton("Gérer Employés");
            manageEmployeeButton.setBackground(new Color(40, 167, 69)); // Couleur verte
            manageEmployeeButton.setForeground(new Color(40, 167, 69));
            manageEmployeeButton.setFont(new Font("Arial", Font.PLAIN, 14));
            manageEmployeeButton.addActionListener(e -> {
                frame.dispose();
                new ManageEmployee().afficherManageEmployee(SessionManager.getCurrentUserRole());
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
                    new Inventory().afficherInventory(SessionManager.getCurrentUserRole());
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

        JButton ManageStore = new JButton("Gestion Store");
        ManageStore.setBackground(new Color(220, 53, 69)); // Rouge
        ManageStore.setForeground(new Color(220, 53, 69));
        ManageStore.setFont(new Font("Arial", Font.PLAIN, 14));
        ManageStore.addActionListener(e -> {
            SessionManager.endSession();
            frame.dispose();
            new StoreManager().afficherGestionStore();
        });
        navBar.add(ManageStore);
        return navBar;
    }

    private void loadUserInfo(String userEmail) {
        if (SessionManager.isLoggedIn()) {
            pseudo = SessionManager.getCurrentUserPseudo();
            email = SessionManager.getCurrentUserEmail();
            role = SessionManager.getCurrentUserRole();
            store = getStoreForUser(SessionManager.getCurrentUserId());
        } else {
            try (Connection connection = DriverManager.getConnection(
                    dbProperties.getProperty("db.url"),
                    dbProperties.getProperty("db.username"),
                    dbProperties.getProperty("db.password"))) {

                String query = "SELECT u.pseudo, u.email, u.rôle, s.name_store FROM users u " +
                        "LEFT JOIN store_employees se ON u.id = se.user_id " +
                        "LEFT JOIN store s ON se.store_id = s.id " +
                        "WHERE u.email = ?";

                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, userEmail);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            pseudo = rs.getString("pseudo");
                            email = rs.getString("email");
                            role = rs.getString("rôle");
                            store = rs.getString("name_store") != null ? rs.getString("name_store") : "Non assigné";
                        }
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erreur lors de la récupération des informations utilisateur.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getStoreForUser(String userId) {
        String store = "Non assigné";
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT s.name_store FROM store s " +
                    "JOIN store_employees se ON s.id = se.store_id " +
                    "WHERE se.user_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        store = rs.getString("name_store");
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la récupération du magasin.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return store;
    }


    private JScrollPane createEmployeeTable() {
        String[] columnNames = {"Pseudo", "Email", "Rôle", "Magasin"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query;
            if ("administrateur".equals(role)) {
                query = "SELECT u.pseudo, u.email, u.rôle, s.name_store FROM users u " +
                        "LEFT JOIN store_employees se ON u.id = se.user_id " +
                        "LEFT JOIN store s ON se.store_id = s.id " +
                        "WHERE u.rôle = 'employé'";
            } else {
                query = "SELECT u.pseudo, u.email, u.rôle, s.name_store FROM users u " +
                        "LEFT JOIN store_employees se ON u.id = se.user_id " +
                        "LEFT JOIN store s ON se.store_id = s.id " +
                        "WHERE u.rôle = 'employé' AND s.name_store = ?";
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                if (!"administrateur".equals(role)) {
                    stmt.setString(1, store);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{rs.getString("pseudo"), rs.getString("email"), rs.getString("rôle"), rs.getString("name_store")});
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la récupération des employés.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        JTable table = new JTable(model);
        return new JScrollPane(table);
    }

    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel();
        dashboard.setLayout(new BoxLayout(dashboard, BoxLayout.Y_AXIS));
        dashboard.setPreferredSize(new Dimension(250, frame.getHeight()));
        dashboard.setBackground(new Color(44, 62, 80));

        // Panel pour le titre
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));  // Disposition horizontale pour centrer le titre
        titlePanel.setBackground(new Color(44, 62, 80));  // Couleur de fond

        JLabel titleLabel = new JLabel("Profil Utilisateur");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dashboard.add(Box.createVerticalStrut(20));  // Espacement avant le titre
        titlePanel.add(Box.createHorizontalGlue());  // Pour centrer le titre horizontalement
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createHorizontalGlue());  // Pour centrer le titre horizontalement

        // Panel pour les informations de l'utilisateur
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));  // Disposition verticale pour les infos utilisateur
        userInfoPanel.setBackground(new Color(44, 62, 80));  // Couleur de fond

        JLabel userLabel = new JLabel("Pseudo : " + pseudo);
        JLabel emailLabel = new JLabel("Email : " + email);
        JLabel roleLabel = new JLabel("Rôle : " + role);
        JLabel storeLabel = new JLabel("Magasin : " + store);

        userLabel.setForeground(Color.WHITE);
        emailLabel.setForeground(Color.WHITE);
        roleLabel.setForeground(Color.WHITE);
        storeLabel.setForeground(Color.WHITE);

        userLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        storeLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        storeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userInfoPanel.add(userLabel);
        userInfoPanel.add(emailLabel);
        userInfoPanel.add(roleLabel);
        userInfoPanel.add(storeLabel);

        // Panel pour les boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));  // Disposition verticale pour les boutons
        buttonPanel.setBackground(new Color(44, 62, 80));  // Couleur de fond

        JButton changeNameButton = new JButton("Changer de pseudo");
        JButton changePasswordButton = new JButton("Changer de mot de passe");

        changeNameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        changePasswordButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        changeNameButton.addActionListener(e -> changePseudo());
        changePasswordButton.addActionListener(e -> changePassword());

        buttonPanel.add(changeNameButton);
        buttonPanel.add(Box.createVerticalStrut(10));  // Espacement entre les boutons
        buttonPanel.add(changePasswordButton);

        // Ajouter chaque panel au dashboard
        dashboard.add(titlePanel);
        dashboard.add(Box.createVerticalStrut(20));
        dashboard.add(userInfoPanel);
        dashboard.add(Box.createVerticalStrut(20));
        dashboard.add(buttonPanel);

        return dashboard;
    }
    private void changePseudo() {
        JTextField pseudoField = new JTextField(pseudo);  // Remplir le champ avec le pseudo actuel

        // Créer un panneau pour le pseudo
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Nouveau pseudo :"));
        panel.add(pseudoField);

        // Afficher la boîte de dialogue
        int option = JOptionPane.showConfirmDialog(frame, panel, "Changer de pseudo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String newPseudo = pseudoField.getText();
            if (!newPseudo.isEmpty() && !newPseudo.equals(pseudo)) {
                try (Connection connection = DriverManager.getConnection(
                        dbProperties.getProperty("db.url"),
                        dbProperties.getProperty("db.username"),
                        dbProperties.getProperty("db.password"))) {

                    String updateQuery = "UPDATE users SET pseudo = ? WHERE email = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                        stmt.setString(1, newPseudo);
                        stmt.setString(2, email);
                        int rowsUpdated = stmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            JOptionPane.showMessageDialog(frame, "Pseudo mis à jour avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                            pseudo = newPseudo;  // Mettre à jour le pseudo localement
                            frame.dispose();
                            new Connexion().afficherConnexion();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour du pseudo.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour du pseudo.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Veuillez entrer un pseudo valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changePassword() {
        JPasswordField oldPasswordField = new JPasswordField(20);
        JPasswordField newPasswordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);

        // Créer un panneau pour les champs de mot de passe
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Ancien mot de passe :"));
        panel.add(oldPasswordField);
        panel.add(new JLabel("Nouveau mot de passe :"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Confirmer le mot de passe :"));
        panel.add(confirmPasswordField);

        // Afficher la boîte de dialogue
        int option = JOptionPane.showConfirmDialog(frame, panel, "Changer de mot de passe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Vérifier si les mots de passe correspondent et si l'ancien mot de passe est correct
            if (newPassword.equals(confirmPassword)) {
                try (Connection connection = DriverManager.getConnection(
                        dbProperties.getProperty("db.url"),
                        dbProperties.getProperty("db.username"),
                        dbProperties.getProperty("db.password"))) {

                    // Vérifier l'ancien mot de passe
                    String checkPasswordQuery = "SELECT password FROM users WHERE email = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(checkPasswordQuery)) {
                        stmt.setString(1, email);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                String storedPassword = rs.getString("password");
                                if (BCrypt.checkpw(oldPassword, storedPassword)) {
                                    String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                                    String updatePasswordQuery = "UPDATE users SET password = ? WHERE email = ?";
                                    try (PreparedStatement updateStmt = connection.prepareStatement(updatePasswordQuery)) {
                                        updateStmt.setString(1, hashedPassword);  // Utiliser le nouveau mot de passe
                                        updateStmt.setString(2, email);
                                        int rowsUpdated = updateStmt.executeUpdate();
                                        if (rowsUpdated > 0) {
                                            JOptionPane.showMessageDialog(frame, "Mot de passe mis à jour avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour du mot de passe.", "Erreur", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(frame, "Ancien mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour du mot de passe.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Les mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}