package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Properties;

public class ChoixMagasin {

    private Properties dbProperties;

    public ChoixMagasin() {
        dbProperties = new Properties();
        try {
            dbProperties.load(getClass().getResourceAsStream("/db.properties"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur de chargement des propriétés de la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void afficherChoixMagasin(String email) {
        if (!SessionManager.isLoggedIn() || !"administrateur".equals(SessionManager.getCurrentUserRole())) {
            JOptionPane.showMessageDialog(null, "Accès non autorisé.", "Erreur", JOptionPane.ERROR_MESSAGE);
            new Connexion().afficherConnexion();
            return;
        }

        JFrame frame = new JFrame("Choix Magasin");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 250);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Sélectionner un magasin");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        JComboBox<String> storeComboBox = new JComboBox<>();
        storeComboBox.setMaximumSize(new Dimension(250, 30));
        loadStores(storeComboBox);
        mainPanel.add(storeComboBox);

        mainPanel.add(Box.createVerticalStrut(15));

        JButton updateButton = new JButton("Mettre à jour");
        updateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(updateButton);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedStore = (String) storeComboBox.getSelectedItem();
                if (selectedStore != null) {
                    int storeId = getStoreIdByName(selectedStore);
                    if (storeId != -1) {
                        updateAdminStore(storeId);
                        JOptionPane.showMessageDialog(null, "Magasin sélectionné avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose();
                        new Inventory().afficherInventory(SessionManager.getCurrentUserRole());
                    } else {
                        JOptionPane.showMessageDialog(null, "Erreur: impossible de trouver le magasin.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        frame.add(mainPanel);
        frame.setVisible(true);
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
        return -1;
    }

    private void updateAdminStore(int storeId) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "UPDATE store_employees SET store_id = ? WHERE user_id = (SELECT id FROM users WHERE rôle = 'administrateur')";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, storeId);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de mise à jour dans la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}