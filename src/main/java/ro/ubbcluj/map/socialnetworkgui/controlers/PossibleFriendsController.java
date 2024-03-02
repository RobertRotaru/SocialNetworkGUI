package ro.ubbcluj.map.socialnetworkgui.controlers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.events.Event;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UsersChangedEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observable;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

public class PossibleFriendsController implements Observer<UsersChangedEvent> {

    ObservableList<String> model = FXCollections.observableArrayList();

    private Utilizator from;
    private Stage stage;
    private SocialNetworkService service;

    @FXML
    ListView<String> usersListView;

    @FXML
    Button sendBttn;

    @FXML
    public void initialize() {
        usersListView.setItems(model);
    }

    public void setService(SocialNetworkService service) {
        this.service = service;
        initModel();
    }

    private void initModel() {
        var users = service.getPotentialFriends(from);
        model.clear();
        users.forEach((user) -> model.add(user.getId() + " : " + user.getFirstName() + " " + user.getLastName()));
    }

    public void setFromUser(Utilizator user) {
        this.from = user;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void handleSendRequest() {
        String userString = usersListView.getSelectionModel().getSelectedItem();
        Long idUser = Long.parseLong(userString.split("\\s")[0]);

        var foundUser = service.getUser(idUser);
        if(foundUser.isPresent()) {
            Utilizator to = foundUser.get();
            try {
                service.addRequest(from, to);
                MessageAlert.showMessage(stage, Alert.AlertType.CONFIRMATION, "Request send",
                        "The friend request from" + from + " to " + to + "has been sent successfully!");

            } catch (ValidationException e) {
                MessageAlert.showErrorMessage(stage, e.getMessage());
            }
        }

    }

    @Override
    public void update(UsersChangedEvent usersChangedEvent) {
        initModel();
    }
}
