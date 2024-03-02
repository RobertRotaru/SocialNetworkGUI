package ro.ubbcluj.map.socialnetworkgui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.controlers.MessageAlert;
import ro.ubbcluj.map.socialnetworkgui.controlers.ProfileController;
import ro.ubbcluj.map.socialnetworkgui.controlers.SignUpController;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;

import java.io.IOException;

public class LoginWindow {

    private SocialNetworkService service;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private Stage stage;

    public void setService(SocialNetworkService service) {
        this.service = service;
    }

    @FXML
    TextField usernameFld;

    @FXML
    PasswordField passFld;

    @FXML
    Button loginBttn;

    @FXML
    public void initialize() {
        clearFields();
    }

    private void clearFields() {
        usernameFld.setText("");
        passFld.setText("");
    }

    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader profileLoader = new FXMLLoader();
            profileLoader.setLocation(getClass().getResource("views/signup-view.fxml"));

            AnchorPane root = profileLoader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);

            dialogStage.setScene(scene);

            SignUpController signUpController = profileLoader.getController();
            signUpController.setService(service);
            signUpController.setStage(dialogStage);

            dialogStage.setTitle("Sign up");

            dialogStage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameFld.getText();
        String pass = passFld.getText();

        var optionalUser = service.validateCredentials(username, pass);

        if(optionalUser.isPresent()) {
            stage.close();

            try {
                FXMLLoader profileLoader = new FXMLLoader();
                profileLoader.setLocation(getClass().getResource("views/profile-view.fxml"));

                AnchorPane root = profileLoader.load();

                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);

                Scene scene = new Scene(root);

                dialogStage.setScene(scene);

                ProfileController profileController = profileLoader.getController();
                profileController.setUser(optionalUser.get());
                profileController.setService(service);
                profileController.setStage(dialogStage);

                dialogStage.setTitle(profileController.getUser().getFirstName() + " " +
                        profileController.getUser().getLastName());

                dialogStage.show();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        else {
            MessageAlert.showErrorMessage(null, "Wrong login credentials!");
            clearFields();
        }
    }

}
