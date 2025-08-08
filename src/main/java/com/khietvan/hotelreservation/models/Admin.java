package com.khietvan.hotelreservation.models;

public class Admin {
    private int adminId;
    private String username;
    private String password;

    public Admin(int adminId, String username, String password) {
        this.adminId = adminId;
        this.username = username;
        this.password = password;
    }

    public boolean login(String inputUser, String inputPass) {
        return this.username.equals(inputUser) && this.password.equals(inputPass);
    }

    public int getAdminId() { return adminId; }
    public String getUsername() { return username; }
}
