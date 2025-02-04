package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Properties;

public class StoreManager {
    private Properties dbProperties;

    public StoreManager() {
        // Chargement des propriétés de la base de données
        dbProperties = new Properties();
        try {
            dbProperties.load(getClass().getResourceAsStream("/db.properties"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur de chargement des propriétés de la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"));
    }

    public void afficherGestionStore() {
        // Créer la fenêtre principale
        JFrame frame = new JFrame("Gestion des Magasins");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Créer un bouton pour ajouter ou supprimer un magasin
        JButton addStoreButton = new JButton("Ajouter un magasin");
        JButton removeStoreButton = new JButton("Supprimer un magasin");

        addStoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStore();
            }
        });

        removeStoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeStore();
            }
        });

        // Ajouter les boutons à la fenêtre
        JPanel panel = new JPanel();
        panel.add(addStoreButton);
        panel.add(removeStoreButton);
        frame.add(panel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    // Méthode pour ajouter un magasin
    private void addStore() {
        String name = JOptionPane.showInputDialog(null, "Entrez le nom du magasin :");
        if (name != null && !name.trim().isEmpty()) {
            try (Connection connection = getConnection()) {
                String query = "INSERT INTO store (name_store) VALUES (?)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, name);
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Magasin ajouté avec succès !");
                    } else {
                        JOptionPane.showMessageDialog(null, "Erreur lors de l'ajout du magasin.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erreur de base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Le nom du magasin ne peut pas être vide.");
        }
    }

    // Méthode pour supprimer un magasin
    private void removeStore() {
        // Récupérer les magasins existants
        try (Connection connection = getConnection()) {
            String query = "SELECT id, name_store FROM store";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                while (resultSet.next()) {
                    model.addElement(resultSet.getString("name_store"));
                }

                JComboBox<String> storeComboBox = new JComboBox<>(model);
                int option = JOptionPane.showConfirmDialog(null, storeComboBox, "Sélectionner un magasin à supprimer", JOptionPane.OK_CANCEL_OPTION);

                if (option == JOptionPane.OK_OPTION) {
                    String selectedStore = (String) storeComboBox.getSelectedItem();
                    if (selectedStore != null) {
                        deleteStore(selectedStore);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Méthode pour supprimer le magasin et les liens associés dans l'inventaire et store_employees
    private void deleteStore(String storeName) {
        try (Connection connection = getConnection()) {
            // Récupérer l'ID du magasin à supprimer
            String getStoreIdQuery = "SELECT id FROM store WHERE name_store = ?";
            try (PreparedStatement statement = connection.prepareStatement(getStoreIdQuery)) {
                statement.setString(1, storeName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int storeId = resultSet.getInt("id");

                        // Supprimer les enregistrements liés dans store_employees et inventory
                        String deleteEmployeesQuery = "DELETE FROM store_employees WHERE store_id = ?";
                        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteEmployeesQuery)) {
                            deleteStatement.setInt(1, storeId);
                            deleteStatement.executeUpdate();
                        }

                        String deleteInventoryQuery = "DELETE FROM inventory WHERE store_id = ?";
                        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteInventoryQuery)) {
                            deleteStatement.setInt(1, storeId);
                            deleteStatement.executeUpdate();
                        }

                        // Supprimer le magasin
                        String deleteStoreQuery = "DELETE FROM store WHERE id = ?";
                        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteStoreQuery)) {
                            deleteStatement.setInt(1, storeId);
                            int rowsAffected = deleteStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(null, "Magasin supprimé avec succès !");
                            } else {
                                JOptionPane.showMessageDialog(null, "Erreur lors de la suppression du magasin.");
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
