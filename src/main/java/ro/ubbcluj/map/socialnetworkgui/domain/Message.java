package ro.ubbcluj.map.socialnetworkgui.domain;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class Message extends Entity<Long> {
    private Utilizator from;
    private Utilizator to;
    private String text;
    private OffsetDateTime date;

    public Message() {
        this.date = OffsetDateTime.now();
    }

    public Message(Utilizator from, Utilizator to, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.date = OffsetDateTime.now(ZoneId.systemDefault());
    }

    public Utilizator getFrom() {
        return from;
    }

    public Utilizator getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setFrom(Utilizator from) {
        this.from = from;
    }

    public void setTo(Utilizator to) {
        this.to = to;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return from.getFirstName()+ " " + from.getLastName() + " -> "
                + to.getFirstName() + " " + to.getLastName() + " : "
                + text;
    }
}
