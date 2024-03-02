package ro.ubbcluj.map.socialnetworkgui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.LoginWindow;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UsersChangedEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.io.IOException;

public class ProfileInfoController implements Observer<UsersChangedEvent> {

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private Stage dialogStage;
    private Utilizator user;
    private SocialNetworkService service;

    public void setUser(Utilizator user) {
        this.user = user;
        nameLbl.setText(user.getFirstName() + " " + user.getLastName());
    }

    public void setService(SocialNetworkService service) {
        this.service = service;
        friendsLbl.setText("Number of friends: " + service.getFriends(user).size());
    }

    @FXML
    Button logoutBttn;

    @FXML
    Label nameLbl;

    @FXML
    Label friendsLbl;

    @FXML
    public void initialize() {
        nameLbl.setText("");
        friendsLbl.setText("");
    }

    @FXML
    public void handleLogout() {
        dialogStage.close();

        try {
            FXMLLoader loginLoader = new FXMLLoader();
            loginLoader.setLocation(getClass().getResource("views/login-view.fxml"));

            AnchorPane root = loginLoader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);

            dialogStage.setScene(scene);

            LoginWindow loginWindow = loginLoader.getController();
            loginWindow.setService(service);
            loginWindow.setStage(dialogStage);

            dialogStage.setTitle("Welcome to the SocialNetwork!");

            dialogStage.show();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(UsersChangedEvent usersChangedEvent) {
        nameLbl.setText(user.getFirstName() + " " + user.getLastName());
        friendsLbl.setText("Number of friends: " + service.getFriends(user).size());
    }
}
