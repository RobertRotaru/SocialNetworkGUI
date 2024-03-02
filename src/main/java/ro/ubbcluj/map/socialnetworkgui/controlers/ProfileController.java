package ro.ubbcluj.map.socialnetworkgui.controlers;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.InboxController;
import ro.ubbcluj.map.socialnetworkgui.ProfileInfoController;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;

import java.util.ArrayList;

public class ProfileController {
    public void setStage(Stage stage) {
        this.stage = stage;
        this.profileInfoController.setDialogStage(stage);
    }

    private Stage stage;

    @FXML
    private InboxController inboxController;

    @FXML
    private RequestsController requestsController;

    @FXML
    private PossibleFriendsController discoverController;
    @FXML
    private ProfileInfoController profileInfoController;

    @FXML
    Tab inboxTab;

    @FXML
    Tab requestsTab;

    @FXML
    Tab discoverTab;
    @FXML
    Tab profileInfoTab;

    private SocialNetworkService service;

    public Utilizator getUser() {
        return user;
    }

    private Utilizator user;

    @FXML
    TabPane profileTab;

    public void setService(SocialNetworkService service) {
        this.service = service;
        this.inboxController.setService(service);
        this.requestsController.setService(service);
        this.discoverController.setService(service);
        this.profileInfoController.setService(service);
    }

    public void setUser(Utilizator user) {
        this.user = user;
        this.inboxController.setUser(user);
        this.requestsController.setTo(user);
        this.discoverController.setFromUser(user);
        this.profileInfoController.setUser(user);
    }

    @FXML
    public void initialize() {
        profileTab.getTabs().setAll(inboxTab, requestsTab, discoverTab, profileInfoTab);
        profileTab.getSelectionModel().select(inboxTab);
    }
}
