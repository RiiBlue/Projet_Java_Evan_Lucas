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
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.add(mainPanel);

        JPanel navBar = new JPanel();
        navBar.setBackground(new Color(33, 37, 41));
        navBar.setLayout(new FlowLayout(FlowLayout.RIGHT));

        String role = getUserRole(email);

        if ("administrateur".equals(role)) {
            JButton whiteListButton = new JButton("Liste blanche");
            whiteListButton.setBackground(new Color(0, 123, 255));
            whiteListButton.setForeground(new Color(0, 123, 255));
            whiteListButton.setFont(new Font("Arial", Font.PLAIN, 14));
            whiteListButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.dispose();
                    new WhiteList().afficherWhiteList();
                }
            });
            navBar.add(whiteListButton);

            JButton manageEmployeeButton = new JButton("Gérer Employés");
            manageEmployeeButton.setBackground(new Color(40, 167, 69)); // Couleur verte
            manageEmployeeButton.setForeground(new Color(40, 167, 69));
            manageEmployeeButton.setFont(new Font("Arial", Font.PLAIN, 14));
            manageEmployeeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.dispose();
                    new ManageEmployee().afficherManageEmployee(); // Appel à la méthode pour afficher la page ManageEmployee
                }
            });
            navBar.add(manageEmployeeButton);
        }

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

        JButton inventoryButton = new JButton("Inventaire");
        inventoryButton.setBackground(new Color(220, 53, 69)); // Rouge
        inventoryButton.setForeground(new Color(220, 53, 69));
        inventoryButton.setFont(new Font("Arial", Font.PLAIN, 14));
        inventoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SessionManager.isLoggedIn()) {
                    frame.dispose();
                    new Inventory().afficherInventory();
                } else {
                    JOptionPane.showMessageDialog(null, "Vous devez être connecté pour accéder à l'inventaire.");
                    new Connexion().afficherConnexion();
                }
            }
        });
        navBar.add(inventoryButton);


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

    }

    private String getUserRole(String email) {
        try (Connection connection = DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.username"),
                dbProperties.getProperty("db.password"))) {

            String query = "SELECT rôle FROM users WHERE email = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("rôle");
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la vérification du rôle de l'utilisateur.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
}
