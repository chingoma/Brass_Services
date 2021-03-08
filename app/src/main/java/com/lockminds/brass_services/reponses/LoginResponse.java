package com.lockminds.brass_services.reponses;

public class LoginResponse {

    Boolean status;
    String policy_url,message,id,name,token,photo_url,email,change_details,change_picture,phone_number,team_email,team_phone,team_name,team_address;

    public LoginResponse() {}

    public LoginResponse(Boolean status,String policy_url,String team_address, String message, String id, String name, String token, String photo_url, String email, String change_details, String change_picture, String phone_number, String team_email, String team_phone, String team_name) {
        this.policy_url = policy_url;
        this.team_address = team_address;
        this.status = status;
        this.message = message;
        this.id = id;
        this.name = name;
        this.token = token;
        this.photo_url = photo_url;
        this.email = email;
        this.change_details = change_details;
        this.change_picture = change_picture;
        this.phone_number = phone_number;
        this.team_email = team_email;
        this.team_phone = team_phone;
        this.team_name = team_name;
    }

    public String getPolicy_url() {
        return policy_url;
    }

    public void setPolicy_url(String policy_url) {
        this.policy_url = policy_url;
    }

    public String getTeam_address() {
        return team_address;
    }

    public void setTeam_address(String team_address) {
        this.team_address = team_address;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getChange_details() {
        return change_details;
    }

    public void setChange_details(String change_details) {
        this.change_details = change_details;
    }

    public String getChange_picture() {
        return change_picture;
    }

    public void setChange_picture(String change_picture) {
        this.change_picture = change_picture;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getTeam_email() {
        return team_email;
    }

    public void setTeam_email(String team_email) {
        this.team_email = team_email;
    }

    public String getTeam_phone() {
        return team_phone;
    }

    public void setTeam_phone(String team_phone) {
        this.team_phone = team_phone;
    }

    public String getTeam_name() {
        return team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }
}
