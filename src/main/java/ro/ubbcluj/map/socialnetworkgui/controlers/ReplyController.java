package ro.ubbcluj.map.socialnetworkgui.controlers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.InboxController;
import ro.ubbcluj.map.socialnetworkgui.domain.Message;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UsersChangedEvent;

public class ReplyController {

    private Utilizator to;
    private Utilizator from;
    private SocialNetworkService service;
    private Stage stage;

    @FXML
    TextField txtFld;

    @FXML
    public void sendReply() {
        Message message = new Message(
                from, to, "R: " + txtFld.getText()
        );
        service.addMessage(message);
        stage.close();
    }

    public void setTo(Utilizator to) {
        this.to = to;
    }

    public void setFrom(Utilizator from) {
        this.from = from;
    }

    public void setService(SocialNetworkService service) {
        this.service = service;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
