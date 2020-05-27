package nl.evandikken.evanchat.models;

public class FriendModel {
    private String date;

    public FriendModel(String date) {
        this.date = date;
    }

    public FriendModel(){

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}