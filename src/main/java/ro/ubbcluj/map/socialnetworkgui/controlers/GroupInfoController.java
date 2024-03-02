package ro.ubbcluj.map.socialnetworkgui.controlers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import ro.ubbcluj.map.socialnetworkgui.domain.Group;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoController {
    @FXML
    ListView<String> usersList;

    ObservableList<String> model = FXCollections.observableArrayList();

    private Group group;
    private Utilizator user;
    private SocialNetworkService service;


    public void setGroup(Group group) {
        this.group = group;
    }

    public void setUser(Utilizator user) {
        this.user = user;
    }

    public void setService(SocialNetworkService service) {
        this.service = service;
        initModel();
    }

    @FXML
    public void initialize() {
        usersList.setItems(model);
    }

    public void initModel() {
        List<String> strings = new ArrayList<>();
        group.getUsers().forEach(user -> {
            if(user.equals(this.user)) {
                strings.add("Dvs.");
            }
            else {
                strings.add(user.getFirstName() + " " + user.getLastName());
            }
        });
        model.setAll(strings);
    }
}
