package edu.wearpark.backend.util;

public class CommonValidationUtil {
    public static boolean validatePassword(String password) {
        if(password == null) return false;
        if(password.length() < 8) return false;
        if(!password.matches(".*[a-z].*")) return false;
        if(!password.matches(".*[A-Z].*")) return false;
        if(!password.matches(".*[0-9].*")) return false;
        return password.matches(".*[!@#$%?&*()\\-+=\"':;\\[\\]{}].*");
    }
    public static boolean validateEmail(String email) {
        if(email == null) return false;
        if(email.length() < 4) return false;
        return email.matches(
                "^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$"
        );
    }
}
