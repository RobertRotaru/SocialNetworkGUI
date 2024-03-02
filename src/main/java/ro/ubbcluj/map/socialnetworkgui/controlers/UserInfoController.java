package ro.ubbcluj.map.socialnetworkgui.controlers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;

import java.util.Objects;

public class UserInfoController {
    @FXML
    Label userLbl = new Label();

    @FXML
    Label frndLbl = new Label();

    private Utilizator dest;
    private String destText;
    private Utilizator user;
    private SocialNetworkService service;

    public void setDest(Utilizator dest) {
        this.dest = dest;
        this.destText = dest.getFirstName() + " " + dest.getLastName();
        setLabels();
    }

    public void setUser(Utilizator user) {
        this.user = user;
    }

    public void setService(SocialNetworkService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {

        userLbl.setText("");

        frndLbl.setText("");
    }

    private void setLabels() {
        userLbl.setText(destText);

        var foundFriendship = service.getFriendship(this.user.getId(), this.dest.getId());
        foundFriendship.ifPresent(friendship -> frndLbl.setText("You are friends with this users since: " +
                friendship.getDate()));
    }
}
