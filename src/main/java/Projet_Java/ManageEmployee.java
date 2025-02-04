package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Properties;

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

        String role = SessionManager.getCurrentUserRole(); // Assurer que tu as une méthode qui récupère le rôle

        JButton choiceAccueil = new JButton("Accueil");
        choiceAccueil.setBackground(new Color(0, 0, 0));
        choiceAccueil.setForeground(new Color(0, 0, 0));
        choiceAccueil.setFont(new Font("Arial", Font.PLAIN, 14));
        choiceAccueil.addActionListener(e -> {
            frame.dispose();
            new Main().createMainFrame((SessionManager.getCurrentUserRole()));
        });
        navBar.add(choiceAccueil);
        if ("administrateur".equals(role)) {

            // Si c'est l'admin, on affiche le bouton Choix Magasin
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
        return navBar;
    }

    public void afficherManageEmployee(String email) {
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

        JFrame frame = new JFrame("Gérer les employés");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));

        JPanel navBar = createNavBar(frame);
        mainPanel.add(navBar, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(255, 255, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 200, 15, 200);
        gbc.weightx = 1;

        JLabel title = new JLabel("Gérer les employés", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(new Color(52, 58, 64));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(title, gbc);

        JLabel storeLabel = new JLabel("Sélectionner un magasin:");
        storeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 1;
        contentPanel.add(storeLabel, gbc);

        JComboBox<String> storeComboBox = new JComboBox<>();
        storeComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridy = 2;
        contentPanel.add(storeComboBox, gbc);

        loadStores(storeComboBox);

        JLabel emailLabel = new JLabel("Sélectionner un employé (Email):");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 3;
        contentPanel.add(emailLabel, gbc);

        JComboBox<String> emailComboBox = new JComboBox<>();
        emailComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridy = 4;
        contentPanel.add(emailComboBox, gbc);

        loadEmployeeEmails(emailComboBox);

        storeComboBox.setPreferredSize(new Dimension(200, 40));
        emailComboBox.setPreferredSize(new Dimension(200, 40));


        JButton updateButton = new JButton("Mettre à jour");
        updateButton.setFont(new Font("Arial", Font.BOLD, 18));
        updateButton.setBackground(new Color(40, 167, 69));
        updateButton.setForeground(Color.WHITE);
        updateButton.setBorderPainted(false);
        updateButton.setFocusPainted(false);
        gbc.gridy = 5;
        gbc.insets = new Insets(30, 200, 30, 200);
        contentPanel.add(updateButton, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        frame.add(mainPanel);
        frame.setVisible(true);

        updateButton.addActionListener(e -> {
            String selectedEmail = (String) emailComboBox.getSelectedItem();
            String selectedStore = (String) storeComboBox.getSelectedItem();
            if (selectedStore != null && selectedEmail != null) {
                int storeId = getStoreIdByName(selectedStore);
                int employeeId = getEmployeeIdByEmail(selectedEmail);
                if (storeId != -1 && employeeId != -1) {
                    updateEmployeeStore(employeeId, storeId);
                    JOptionPane.showMessageDialog(null, "Employé ajouté au magasin avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                    new ManageEmployee().afficherManageEmployee(SessionManager.getCurrentUserRole());
                } else {
                    JOptionPane.showMessageDialog(null, "Erreur : impossible de lier l'employé au magasin.", "Erreur", JOptionPane.ERROR_MESSAGE);
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
