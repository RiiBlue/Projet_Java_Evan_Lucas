package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class Main {

    private JFrame frame;
    private Properties dbProperties;
    private String pseudo, email, role, store;

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
        frame.getContentPane().setBackground(new Color(255, 0, 0));

        // Récupérer les informations de l'utilisateur
        loadUserInfo(email);

        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.add(mainPanel);

        JPanel navBar = createNavBar();
        mainPanel.add(navBar, BorderLayout.NORTH);

        JPanel dashboard = createDashboardPanel();
        mainPanel.add(dashboard, BorderLayout.WEST);

        frame.setVisible(true);
    }

    private JPanel createNavBar() {
        JPanel navBar = new JPanel();
        navBar.setBackground(new Color(33, 37, 41));
        navBar.setLayout(new FlowLayout(FlowLayout.RIGHT));

        if ("administrateur".equals(role)) {
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
        }

        JButton productButton = new JButton("Produits");
        productButton.setBackground(new Color(253, 189, 1));
        productButton.setForeground(new Color(253, 189, 1));
        productButton.setFont(new Font("Arial", Font.PLAIN, 14));
        productButton.addActionListener(e -> {
            frame.dispose();
            new Items().afficherItem();
        });
        navBar.add(productButton);

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

    private void loadUserInfo(String userEmail) {
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

        changeNameButton.addActionListener(e -> JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter."));
        changePasswordButton.addActionListener(e -> JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter."));

        buttonPanel.add(changeNameButton);
        buttonPanel.add(Box.createVerticalStrut(10));  // Espacement entre les boutons
        buttonPanel.add(changePasswordButton);

        // Ajouter chaque panel au dashboard
        dashboard.add(titlePanel);  // Ajouter le panel du titre
        dashboard.add(Box.createVerticalStrut(20));  // Espacement après le titre
        dashboard.add(userInfoPanel);  // Ajouter le panel des infos utilisateur
        dashboard.add(Box.createVerticalStrut(20));  // Espacement après les infos utilisateur
        dashboard.add(buttonPanel);  // Ajouter le panel des boutons

        return dashboard;
    }

}