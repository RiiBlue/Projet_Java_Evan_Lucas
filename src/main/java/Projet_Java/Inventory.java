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

    private ResultSet getItemsForStore(Connection connection, String storeId) throws SQLException {
        String query = "SELECT items.name, items.price, items.quantity FROM items " +
                "JOIN inventory ON inventory.item_id = items.id " +
                "WHERE inventory.store_id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, storeId);
        return stmt.executeQuery();
    }
}
