package Projet_Java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Properties;

public class Items {

    private Properties dbProperties;

    public Items() {
        dbProperties = new Properties();
        try {
            dbProperties.load(getClass().getResourceAsStream("/db.properties"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur de chargement des propriétés de la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void afficherItem() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Ajouter un Item");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel navBar = new JPanel();
        navBar.setBackground(new Color(33, 37, 41)); // Couleur sombre pour la navbar
        navBar.setLayout(new FlowLayout(FlowLayout.LEFT)); // Aligner à gauche
        JButton homeButton = new JButton("Accueil");
        homeButton.setBackground(new Color(0, 123, 255)); // Bleu
        homeButton.setForeground(Color.WHITE);
        homeButton.setFont(new Font("Arial", Font.PLAIN, 14));

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Main().createMainFrame("");
            }
        });
        navBar.add(homeButton);
        mainPanel.add(navBar, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(242, 242, 242));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 50, 5, 50);
        gbc.weightx = 1;

        JLabel title = new JLabel("Ajouter un Item", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 50, 30, 50);
        contentPanel.add(title, gbc);

        JLabel nameLabel = new JLabel("Nom de l'objet :", SwingConstants.LEFT);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 25));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(nameField.getPreferredSize().width, 100));
        gbc.gridy = 2;
        contentPanel.add(nameField, gbc);

        JLabel priceLabel = new JLabel("Prix :", SwingConstants.LEFT);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 25));
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(priceLabel, gbc);

        JTextField priceField = new JTextField(20);
        priceField.setPreferredSize(new Dimension(priceField.getPreferredSize().width, 100));
        gbc.gridy = 4;
        contentPanel.add(priceField, gbc);

        JLabel quantityLabel = new JLabel("Quantité :", SwingConstants.LEFT);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 25));
        gbc.gridy = 5;
        gbc.gridwidth = 5;
        gbc.insets = new Insets(10, 150, 5, 150);
        contentPanel.add(quantityLabel, gbc);

        JTextField quantityField = new JTextField(20);
        quantityField.setPreferredSize(new Dimension(quantityField.getPreferredSize().width, 100));
        gbc.gridy = 6;
        contentPanel.add(quantityField, gbc);

        JButton submitButton = new JButton("Ajouter");
        gbc.gridy = 7;
        gbc.insets = new Insets(30, 750, 30, 750);
        contentPanel.add(submitButton, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> {
            handleAjoutItem(nameField.getText(), priceField.getText(), quantityField.getText(), frame);
        });
    }

    private void handleAjoutItem(String name, String price, String quantity, JFrame frame) {
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nom de l'objet manquant.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidPrice(price)) {
            JOptionPane.showMessageDialog(null, "Veuillez entrer un prix valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidQuantity(quantity)) {
            JOptionPane.showMessageDialog(null, "Veuillez entrer une quantité.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            insertItem(connection, name, Double.parseDouble(price), Integer.parseInt(quantity));
            JOptionPane.showMessageDialog(null, "Item ajouté avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);

            frame.dispose();
            new Main().createMainFrame("");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidPrice(String price) {
        try {
            double parsedPrice = Double.parseDouble(price);
            return parsedPrice > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

        private boolean isValidQuantity(String quantity) {
            try {
                int parsedQuantity = Integer.parseInt(quantity); // Utiliser Integer.parseInt
                return parsedQuantity > 0; // Vérifier si la quantité est strictement positive
            } catch (NumberFormatException e) {
                return false;
            }
        }

    private void insertItem(Connection connection, String name, double price, int quantity) throws SQLException {
        String query = "INSERT INTO items (name, price, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        }
    }
}
