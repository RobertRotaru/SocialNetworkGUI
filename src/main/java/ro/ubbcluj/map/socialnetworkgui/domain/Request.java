package ro.ubbcluj.map.socialnetworkgui.domain;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class Request extends Entity<Long> {
    private Utilizator to;
    private Utilizator from;
    private OffsetDateTime date;
    private RequestStatus status;

    public Request(Utilizator from, Utilizator to) {
        this.from = from;
        this.to = to;
        this.date = OffsetDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    public void accept() {
        this.status = RequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = RequestStatus.REJECTED;
    }

    public Utilizator getFrom() {
        return from;
    }

    public void setFrom(Utilizator from) {
        this.from = from;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Utilizator getTo() {
        return to;
    }

    public void setTo(Utilizator to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return from + " -> " + to + " at " + date + " : " + status;
    }
}
