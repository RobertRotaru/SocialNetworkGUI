package ro.ubbcluj.map.socialnetworkgui.controlers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;

public class SignUpController {
    public SocialNetworkService getService() {
        return service;
    }

    public void setService(SocialNetworkService service) {
        this.service = service;
    }

    private SocialNetworkService service;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private Stage stage;

    @FXML
    TextField usernameFld;

    @FXML
    TextField passFld;

    @FXML
    TextField nameFld;

    @FXML
    private void handleSignUp()
    {
        String username = usernameFld.getText();
        String password = passFld.getText();
        String name = nameFld.getText();

        String[] names = name.split(" ");

        try {
            Utilizator user = new Utilizator(names[0], names[1]);

            var addedUser = service.addUser(user);
            Utilizator newUser = addedUser.get();
            service.addNewUserCredentials(newUser, username, password);

            MessageAlert.showMessage(stage, Alert.AlertType.CONFIRMATION, "Info", "The new user has been added!");

            stage.close();
        }
        catch(RuntimeException e) {
            MessageAlert.showErrorMessage(stage, e.getMessage());
        }
    }
}
