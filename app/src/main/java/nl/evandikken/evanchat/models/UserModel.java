package nl.evandikken.evanchat.models;

public class UserModel {
    private String email, key, profile_picture, status, username;

    public UserModel(){

    }

    public UserModel(String email, String key, String profile_picture, String status, String username) {
        this.email = email;
        this.key = key;
        this.profile_picture = profile_picture;
        this.status = status;
        this.username = username;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(String profile_picture) {
        this.profile_picture = profile_picture;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}