package Projet_Java;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    public void afficherInventory() {
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

        // Déclaration de la connexion en dehors du try
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

            JPanel navBar = new JPanel();
            navBar.setBackground(new Color(33, 37, 41));

            navBar.setLayout(new FlowLayout(FlowLayout.LEFT));
            JButton accButton = new JButton("Accueil");
            accButton.setBackground(new Color(0, 123, 255));
            accButton.setForeground(new Color(0, 123, 255));
            accButton.setFont(new Font("Arial", Font.PLAIN, 14));
            accButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.dispose();
                    new Main().createMainFrame(SessionManager.getCurrentUserEmail());
                }
            });
            navBar.add(accButton);

            navBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
            JButton productButton = new JButton("Produits");
            productButton.setBackground(new Color(253, 189, 1));
            productButton.setForeground(new Color(253, 189, 1));
            productButton.setFont(new Font("Arial", Font.PLAIN, 14));
            productButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.dispose();
                    new Items().afficherItem();
                }
            });
            navBar.add(productButton);

            JButton logoutButton = new JButton("Déconnexion");
            logoutButton.setBackground(new Color(220, 53, 69)); // Rouge
            logoutButton.setForeground(new Color(220, 53, 69));
            logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
            logoutButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SessionManager.endSession();
                    frame.dispose();
                    new Connexion().afficherConnexion();
                }
            });

            if (SessionManager.isLoggedIn()) {
                navBar.add(logoutButton);
                JLabel userLabel = new JLabel("Connecté en tant que : " + SessionManager.getCurrentUserPseudo() + SessionManager.getCurrentUserId());
                userLabel.setForeground(Color.WHITE);
                userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                navBar.add(userLabel);
            }

            JPanel contentPanel = new JPanel(new GridBagLayout());
            contentPanel.setBackground(new Color(242, 242, 242));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 50, 5, 50);
            gbc.weightx = 1;

            JLabel title = new JLabel("Magasins", SwingConstants.CENTER);
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

            JButton showButton = new JButton("Afficher");
            gbc.gridy = 7;
            gbc.insets = new Insets(-30, 750, 30, 750);
            contentPanel.add(showButton, gbc);

            loadStore(storeComboBox);

            mainPanel.add(contentPanel, BorderLayout.CENTER);

            frame.add(mainPanel);
            frame.setVisible(true);

            showButton.addActionListener(e -> {
                storeList(storeComboBox.getSelectedItem().toString(), frame);
            });

            mainPanel.add(navBar, BorderLayout.NORTH);
            frame.setVisible(true);

            mainPanel.add(navBar, BorderLayout.NORTH);

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

    private void loadStore(JComboBox<String> storeComboBox) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT name_store FROM store " +
                    "JOIN store_employees ON store.id = store_employees.store_id " +
                    "WHERE store_employees.user_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                String userId = SessionManager.getCurrentUserId();
                stmt.setString(1, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    storeComboBox.addItem(rs.getString("name_store"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }


        private ResultSet getItemsForStore(Connection connection, String storeId) throws SQLException {
            String query = "SELECT items.name, inventory.item_id, inventory.store_id, inventory.price, inventory.quantity FROM inventory " +
                    "JOIN items ON inventory.item_id = items.id " +
                    "WHERE inventory.store_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, storeId);
            return stmt.executeQuery();
        }

    private void storeList(String email, JFrame frame) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            setStore(connection, email);
            JOptionPane.showMessageDialog(null, "Email supprimé avec succès de la liste blanche !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            new WhiteList().afficherWhiteList();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setStore(Connection connection, String email) throws SQLException {
        String query = "SELECT * FROM white_list WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        }
    }
}


