package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class ManageEmployee {

    private Properties dbProperties;

    public ManageEmployee() {
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

        String role = SessionManager.getCurrentUserRole();

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

    public void afficherManageEmployee() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!SessionManager.isLoggedIn() && !"administrateur".equals(SessionManager.getCurrentUserRole())) {
            JOptionPane.showMessageDialog(null, "Accès non autorisé.", "Erreur", JOptionPane.ERROR_MESSAGE);
            new Connexion().afficherConnexion();
            return;
        }

        JFrame frame = new JFrame("Manage Employees");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Création du panneau principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Ajout de la barre de navigation
        JPanel navBar = createNavBar(frame);
        mainPanel.add(navBar, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(242, 242, 242));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 50, 5, 50);
        gbc.weightx = 1;

        JLabel title = new JLabel("Manage Employees", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 50, 30, 50);
        contentPanel.add(title, gbc);

        JLabel storeLabel = new JLabel("Sélectionner un magasin:");
        gbc.gridy = 1;
        contentPanel.add(storeLabel, gbc);

        JComboBox<String> storeComboBox = new JComboBox<>();
        gbc.gridy = 2;
        contentPanel.add(storeComboBox, gbc);

        loadStores(storeComboBox);

        JLabel emailLabel = new JLabel("Sélectionner un employé (Email):");
        gbc.gridy = 3;
        contentPanel.add(emailLabel, gbc);

        JComboBox<String> emailComboBox = new JComboBox<>();
        gbc.gridy = 4;
        contentPanel.add(emailComboBox, gbc);

        loadEmployeeEmails(emailComboBox);

        JButton updateButton = new JButton("Mettre à jour");
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 150, 20, 150);
        contentPanel.add(updateButton, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        frame.add(mainPanel);
        frame.setVisible(true);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedEmail = (String) emailComboBox.getSelectedItem();
                String selectedStore = (String) storeComboBox.getSelectedItem();

                if (selectedStore != null && selectedEmail != null) {
                    int storeId = getStoreIdByName(selectedStore);
                    int employeeId = getEmployeeIdByEmail(selectedEmail);

                    if (storeId != -1 && employeeId != -1) {
                        updateEmployeeStore(employeeId, storeId);

                        JOptionPane.showMessageDialog(null, "Employé ajouté au magasin avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose();
                        new ManageEmployee().afficherManageEmployee();
                    } else {
                        JOptionPane.showMessageDialog(null, "Erreur : impossible de lier l'employé au magasin.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private void loadStores(JComboBox<String> storeComboBox) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT name_store FROM store";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                storeComboBox.addItem(rs.getString("name_store"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEmployeeEmails(JComboBox<String> emailComboBox) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT email FROM users WHERE rôle = 'employé'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String email = rs.getString("email");

                // Vérifier si l'employé est déjà lié à un magasin
                if (!isEmployeeLinkedToStore(email)) {
                    emailComboBox.addItem(email);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isEmployeeLinkedToStore(String email) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT * FROM store_employees WHERE user_id = (SELECT id FROM users WHERE email = ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private int getStoreIdByName(String storeName) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT id FROM store WHERE name_store = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, storeName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return -1; // Retourner -1 si le magasin n'est pas trouvé
    }

    private int getEmployeeIdByEmail(String email) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT id FROM users WHERE email = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return -1; // Retourner -1 si l'employé n'est pas trouvé
    }

    private void updateEmployeeStore(int employeeId, int storeId) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "INSERT INTO store_employees (user_id, store_id) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, storeId);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de mise à jour dans la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
