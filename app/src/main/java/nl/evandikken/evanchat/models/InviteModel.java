package nl.evandikken.evanchat.models;

public class InviteModel {
    private String sent_by;

    public InviteModel(String sent_by) {
        this.sent_by = sent_by;
    }

    public InviteModel(){

    }

    public String getSent_by() {
        return sent_by;
    }

    public void setSent_by(String sent_by) {
        this.sent_by = sent_by;
    }
}