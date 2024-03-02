package ro.ubbcluj.map.socialnetworkgui.domain;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utilizator extends Entity<Long> {
    private String firstName;
    private String lastName;
    private List<Utilizator> friends;
    private int community;
    private CheckBox selected;


    public Utilizator(){
        this.friends = new ArrayList<>();
        this.selected = new CheckBox();
    }

    public Utilizator(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.community = 0; //no community
        this.friends = new ArrayList<>();
        this.selected = new CheckBox();
    }

    public void setSelected(CheckBox selected) {
        this.selected = selected;
    }

    public CheckBox getSelected() {
        return selected;
    }

    public void addFriend(Utilizator friend) {
        this.friends.add(friend);
    }

    public void setCommunity(int _community) {
        this.community = _community;
    }

    public int getCommunity() { return this.community; }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Utilizator> getFriends() {
        return friends;
    }

    public void deleteFriends() {
        friends = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ID: " + id + " - " + firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator)) return false;
        Utilizator that = (Utilizator) o;
        return getFirstName().equals(that.getFirstName()) &&
                getLastName().equals(that.getLastName()) &&
                getFriends().equals(that.getFriends());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getFriends());
    }

    public boolean equals(Utilizator other) {
        return Objects.equals(this.id, other.getId());
    }
}