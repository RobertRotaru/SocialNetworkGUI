package ro.ubbcluj.map.socialnetworkgui.controlers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.InboxController;
import ro.ubbcluj.map.socialnetworkgui.domain.Group;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UsersChangedEvent;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class GroupController {
    private Utilizator user;
    private SocialNetworkService service;
    private Stage stage;
    private InboxController controller;

    ObservableList<String> model = FXCollections.observableArrayList();
    ObservableList<Long> modelId = FXCollections.observableArrayList();

    @FXML
    ListView<String> listView;

    @FXML
    Button makeGrpBttn;

    @FXML
    public void initialize() {
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setItems(model);
    }

    public void setDialog(Stage stage) {
        this.stage = stage;
    }

    public void setUser(Utilizator user){
        this.user = user;
    }

    public void setService(SocialNetworkService service) {
        this.service = service;
        initModel();
    }

    public void setMainController(InboxController controller) {
        this.controller = controller;
    }
    private void initModel() {
        List<Utilizator> users = service.getFriends(user);
        users.forEach((user) -> {
            model.add(user.getFirstName() + " " + user.getLastName());
            modelId.add(user.getId());
        });
    }


    @FXML
    public void handleNewGroup() {
        var indexes = listView.getSelectionModel().getSelectedIndices();

        List<Long> list = new ArrayList<>();
        indexes.forEach(index -> {
            var user = service.getUser(modelId.get(index));
            user.ifPresent(value -> list.add(value.getId()));
        });
        list.add(user.getId());

        Object[] obj =  list.toArray();

        try {
            var group = service.makeGroup(obj);
            group.ifPresent(value -> controller.setGroup(value));
            controller.setDest(null);

            MessageAlert.showMessage(stage, Alert.AlertType.CONFIRMATION, "New Group",
                    "The group has been created successfully");

            stage.close();

            controller.update(new UsersChangedEvent());
        }catch(ValidationException e) {
            MessageAlert.showErrorMessage(stage, e.getMessage());
        }
    }

}
