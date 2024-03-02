package ro.ubbcluj.map.socialnetworkgui.domain;

import java.util.List;

public class ReplyMessage extends Message {
    private GroupMessage message;

    public ReplyMessage(Utilizator from, Utilizator to, String text, GroupMessage message) {
        super(from, to, text);
        this.message = message;
    }

    public GroupMessage getMessage() {
        return message;
    }

    public void setMessage(GroupMessage message) {
        this.message = message;
    }
}
