package ro.ubbcluj.map.socialnetworkgui.controlers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.InboxController;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;

import java.util.List;

public class FriendsController {

    private Utilizator user;
    private SocialNetworkService service;
    private Stage stage;
    private InboxController controller;

    ObservableList<String> model = FXCollections.observableArrayList();
    ObservableList<Long> modelId = FXCollections.observableArrayList();

    @FXML
    ListView<String> listView;

    private Utilizator getDestFromIdx(int index) {
        var taskResult = service.getUser(modelId.get(index));
        return taskResult.orElse(null);
    }

    @FXML
    public void initialize() {

        listView.setOnMouseClicked((event) ->{
                if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    var dest = getDestFromIdx(listView.getSelectionModel().getSelectedIndex());
                    this.controller.handleNewDest(dest);
                    this.stage.close();
                }
            });

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

}
