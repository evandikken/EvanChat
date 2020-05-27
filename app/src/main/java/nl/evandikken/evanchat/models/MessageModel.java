package nl.evandikken.evanchat.models;

public class MessageModel {
    private String message, sent_by, key, date;

    public MessageModel(String message, String sent_by, String key, String date) {
        this.message = message;
        this.sent_by = sent_by;
        this.key = key;
        this.date = date;
    }

    public MessageModel(){

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSent_by() {
        return sent_by;
    }

    public void setSent_by(String sent_by) {
        this.sent_by = sent_by;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}