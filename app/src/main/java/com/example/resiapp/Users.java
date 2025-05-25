package com.example.resiapp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class Users {

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("residentName")
    @Expose
    private String residentName;
    @SerializedName("apartmentInformation")
    @Expose
    private String aparment;

    @SerializedName("role")
    @Expose
    private String role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public String getAparment() {
        return aparment;
    }

    public void setAparment(String aparment) {
        this.aparment = aparment;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}