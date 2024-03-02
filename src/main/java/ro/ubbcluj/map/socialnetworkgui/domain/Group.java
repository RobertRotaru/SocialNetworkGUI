package ro.ubbcluj.map.socialnetworkgui.domain;

import java.util.ArrayList;
import java.util.List;

public class Group extends Entity<Long> {
    private ArrayList<Utilizator> users;

    public Group(ArrayList<Utilizator> users) {
        this.users = users;
    }

    public ArrayList<Utilizator> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<Utilizator> users) {
        this.users = users;
    }
}
