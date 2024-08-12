package com.example.chatterbox.Models;

public class UserModel {
    private String email;
    private String birthDay;
    private String phone;

    public UserModel(String email, String birthDay, String phone) {
        this.email = email;
        this.birthDay = birthDay;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public String getPhone() {
        return phone;
    }
}
