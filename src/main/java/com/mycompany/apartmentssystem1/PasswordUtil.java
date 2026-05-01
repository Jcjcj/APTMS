package com.mycompany.apartmentssystem1;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // hash password
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // check password
    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}