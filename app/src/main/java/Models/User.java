package Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class User {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("residentName")
    @Expose
    private String residentName;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("passwordHash")
    @Expose
    private String password;

    @SerializedName("apartmentInformation")
    @Expose
    private String apartmentInformation;

    @SerializedName("role")
    @Expose
    private String role;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public String getApartmentInformation() {
        return apartmentInformation;
    }

    public void setApartmentInformation(String apartmentInformation) {
        this.apartmentInformation = apartmentInformation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}