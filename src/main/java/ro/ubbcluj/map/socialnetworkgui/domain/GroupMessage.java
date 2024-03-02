package ro.ubbcluj.map.socialnetworkgui.domain;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public class GroupMessage extends Entity<Long> {
    public Long getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(Long idGroup) {
        this.idGroup = idGroup;
    }

    private Long idGroup;
    private Utilizator from;
    private String text;
    private OffsetDateTime date;

    public GroupMessage() {
        this.date = OffsetDateTime.now();
    }

    public GroupMessage(Long idGroup, Utilizator from, String text) {
        this.from = from;
        this.text = text;
        this.idGroup = idGroup;
        this.date = OffsetDateTime.now(ZoneId.systemDefault());
    }

    public Utilizator getFrom() {
        return from;
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

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }
}
