package Projet_Java;

public class SessionManager {
    private static String currentUserEmail;
    private static String currentUserRole;
    private static String currentUserPseudo;
    private static String currentUserId;  // Ajout de l'ID utilisateur
    private static boolean isLoggedIn;

    // Méthode de démarrage de session avec l'ID en paramètre
    public static void startSession(String email, String role, String pseudo, String userId) {
        currentUserEmail = email;
        currentUserRole = role;
        currentUserPseudo = pseudo;
        currentUserId = userId;  // Initialisation de l'ID
        isLoggedIn = true;
    }

    // Méthode de fin de session, réinitialisation de l'ID
    public static void endSession() {
        currentUserEmail = null;
        currentUserRole = null;
        currentUserPseudo = null;
        currentUserId = null;  // Réinitialisation de l'ID
        isLoggedIn = false;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static String getCurrentUserPseudo() {
        return currentUserPseudo;
    }

    public static String getCurrentUserId() {
        return currentUserId;  // Méthode pour récupérer l'ID
    }

    public static boolean isLoggedIn() {
        return isLoggedIn;
    }
}