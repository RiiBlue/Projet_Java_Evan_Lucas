package Projet_Java;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Properties;

public class Inventory {

    private Properties dbProperties;

    public Inventory() {
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

    public void afficherInventory(String email) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!SessionManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Vous devez être connecté pour accéder à l'inventaire.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = SessionManager.getCurrentUserId();

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    dbProperties.getProperty("db.url"),
                    dbProperties.getProperty("db.username"),
                    dbProperties.getProperty("db.password"));

            String storeId = getStoreIdForUser(connection, userId);
            if (storeId == null) {
                JOptionPane.showMessageDialog(null, "Aucun magasin associé à cet utilisateur.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ResultSet items = getItemsForStore(connection, storeId);

            JFrame frame = new JFrame("Gestion de l'Inventaire");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            JPanel mainPanel = new JPanel(new BorderLayout());
            frame.add(mainPanel);

            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBackground(new Color(242, 242, 242));
            JLabel title = new JLabel("Inventaire", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 24));
            contentPanel.add(title, BorderLayout.NORTH);

            mainPanel.add(contentPanel, BorderLayout.NORTH);
            mainPanel.add(createNavBar(frame), BorderLayout.NORTH);

            JPanel spacePanel = new JPanel();
            spacePanel.setPreferredSize(new Dimension(0, 20)); // Crée un espace de 20px
            mainPanel.add(spacePanel, BorderLayout.CENTER);

            String[] columns = {"Nom", "Quantité", "Prix"};
            DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            while (items.next()) {
                String name = items.getString("name");
                int quantity = items.getInt("quantity");
                double price = items.getDouble("price");
                tableModel.addRow(new Object[]{name, quantity, price});
            }

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton addButton = new JButton("Ajouter un Item");
            addButton.setFont(new Font("Arial", Font.PLAIN, 16));
            addButton.setPreferredSize(new Dimension(200, 50));
            Connection finalConnection = connection;
            addButton.addActionListener(e -> showAddItemDialog(frame, finalConnection, storeId));
            buttonsPanel.add(addButton);

            JButton deleteButton = new JButton("Supprimer un Item");
            deleteButton.setFont(new Font("Arial", Font.PLAIN, 16));
            deleteButton.setPreferredSize(new Dimension(200, 50));
            deleteButton.addActionListener(e -> showDeleteItemDialog(frame, finalConnection, storeId));
            buttonsPanel.add(deleteButton);

            JButton updateButton = new JButton("Mettre à jour un Item");
            updateButton.setFont(new Font("Arial", Font.PLAIN, 16));
            updateButton.setPreferredSize(new Dimension(200, 50));
            updateButton.addActionListener(e -> showUpdateItemDialog(frame, finalConnection, storeId));
            buttonsPanel.add(updateButton);

            mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

            frame.setVisible(true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getStoreIdForUser(Connection connection, String userId) throws SQLException {
        String query = "SELECT store_id FROM store_employees WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("store_id");
            }
        }
        return null;
    }

    private ResultSet getItemsForStore(Connection connection, String storeId) throws SQLException {
        String query = "SELECT items.name, inventory.item_id, inventory.store_id, inventory.price, inventory.quantity FROM inventory " +
                "JOIN items ON inventory.item_id = items.id " +
                "WHERE inventory.store_id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, storeId);
        return stmt.executeQuery();
    }

    private void showAddItemDialog(JFrame frame, Connection connection, String storeId) {
        JTextField nameField = new JTextField(20);
        JTextField quantityField = new JTextField(20);
        JTextField priceField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Nom de l'item :")); panel.add(nameField);
        panel.add(new JLabel("Quantité :")); panel.add(quantityField);
        panel.add(new JLabel("Prix :")); panel.add(priceField);

        if (JOptionPane.showConfirmDialog(frame, panel, "Ajouter un Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String quantityStr = quantityField.getText();
            String priceStr = priceField.getText();

            if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Tous les champs doivent être remplis", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                double price = Double.parseDouble(priceStr);

                String itemQuery = "INSERT INTO items (name) VALUES (?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)";
                String inventoryQuery = "INSERT INTO inventory (store_id, item_id, price, quantity) VALUES (?, LAST_INSERT_ID(), ?, ?)";

                try (PreparedStatement stmt1 = connection.prepareStatement(itemQuery);
                     PreparedStatement stmt2 = connection.prepareStatement(inventoryQuery)) {

                    stmt1.setString(1, name);
                    stmt1.executeUpdate();

                    stmt2.setString(1, storeId);
                    stmt2.setDouble(2, price);
                    stmt2.setInt(3, quantity);
                    stmt2.executeUpdate();

                    JOptionPane.showMessageDialog(frame, "Item ajouté avec succès.");
                    frame.dispose();
                    afficherInventory(SessionManager.getCurrentUserRole());
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Quantité et prix doivent être des nombres valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Erreur lors de l'ajout de l'item.", "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void showDeleteItemDialog(JFrame frame, Connection connection, String storeId) {
        JTextField nameField = new JTextField(20);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Nom de l'item à supprimer :"));
        panel.add(nameField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Supprimer un Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Veuillez entrer un nom d'item.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Normalisation du nom
            name = name.toLowerCase();

            try {
                String deleteInventoryQuery = "DELETE FROM inventory WHERE store_id = ? AND item_id = (SELECT id FROM items WHERE LOWER(name) = ?)";
                try (PreparedStatement stmt = connection.prepareStatement(deleteInventoryQuery)) {
                    stmt.setString(1, storeId);
                    stmt.setString(2, name);
                    int rowsAffectedInventory = stmt.executeUpdate();

                    if (rowsAffectedInventory > 0) {
                        String deleteItemQuery = "DELETE FROM items WHERE LOWER(name) = ?";
                        try (PreparedStatement stmt2 = connection.prepareStatement(deleteItemQuery)) {
                            stmt2.setString(1, name);
                            int rowsAffectedItem = stmt2.executeUpdate();

                            if (rowsAffectedItem > 0) {
                                JOptionPane.showMessageDialog(frame, "Item supprimé avec succès.");
                                frame.dispose();
                                afficherInventory(SessionManager.getCurrentUserRole());
                            } else {
                                JOptionPane.showMessageDialog(frame, "Item introuvable dans la table items.", "Erreur", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Item introuvable dans l'inventaire.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Erreur lors de la suppression de l'item.", "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    private void showUpdateItemDialog(JFrame frame, Connection connection, String storeId) {
        JTextField nameField = new JTextField(20);
        JTextField quantityField = new JTextField(20);
        JTextField priceField = new JTextField(20);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Nom de l'item :"));
        panel.add(nameField);
        panel.add(new JLabel("Nouvelle Quantité :"));
        panel.add(quantityField);
        panel.add(new JLabel("Nouveau Prix :"));
        panel.add(priceField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Mettre à jour un Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String quantityStr = quantityField.getText();
            String priceStr = priceField.getText();

            if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Tous les champs doivent être remplis", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                double price = Double.parseDouble(priceStr);

                String checkItemQuery = "SELECT * FROM inventory WHERE store_id = ? AND item_id = (SELECT id FROM items WHERE LOWER(name) = ?)";
                try (PreparedStatement stmt = connection.prepareStatement(checkItemQuery)) {
                    stmt.setString(1, storeId);
                    stmt.setString(2, name.toLowerCase());  // On normalise le nom
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String updateInventoryQuery = "UPDATE inventory SET price = ?, quantity = ? WHERE store_id = ? AND item_id = (SELECT id FROM items WHERE LOWER(name) = ?)";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateInventoryQuery)) {
                            updateStmt.setDouble(1, price);
                            updateStmt.setInt(2, quantity);
                            updateStmt.setString(3, storeId);
                            updateStmt.setString(4, name.toLowerCase());
                            updateStmt.executeUpdate();

                            JOptionPane.showMessageDialog(frame, "Item mis à jour avec succès.");
                            frame.dispose();
                            afficherInventory(SessionManager.getCurrentUserRole());  // Rafraîchir l'inventaire
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Item introuvable dans l'inventaire.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Quantité et prix doivent être des nombres valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour de l'item.", "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}
