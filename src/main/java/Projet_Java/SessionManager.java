package Projet_Java;

public class SessionManager {
    private static String currentUserEmail;
    private static String currentUserRole;
    private static String currentUserPseudo;
    private static boolean isLoggedIn;

    public static void startSession(String email, String role, String pseudo) {
        currentUserEmail = email;
        currentUserRole = role;
        currentUserPseudo = pseudo;
        isLoggedIn = true;
    }

    public static void endSession() {
        currentUserEmail = null;
        currentUserRole = null;
        currentUserPseudo = null;
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

    public static boolean isLoggedIn() {
        return isLoggedIn;
    }
}