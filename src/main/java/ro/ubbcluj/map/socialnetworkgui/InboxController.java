package ro.ubbcluj.map.socialnetworkgui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import ro.ubbcluj.map.socialnetworkgui.controlers.*;
import ro.ubbcluj.map.socialnetworkgui.domain.*;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.FriendshipAlreadyExistsException;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.MessageDBService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.UserDBService;
import ro.ubbcluj.map.socialnetworkgui.utils.DateConverter;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UsersChangedEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class InboxController implements Observer<UsersChangedEvent> {

    private SocialNetworkService service;
    private Utilizator user;

    public void setDest(Utilizator dest) {
        this.dest = dest;
    }

    private Utilizator dest;

    public void setGroup(Group group) {
        this.group = group;
    }

    private Group group;

    @FXML
    ListView<String> usersList;

    @FXML
    ListView<String> messagesList;

    @FXML
    TextField messageFld;

    @FXML
    Button sendBttn;

    @FXML
    Label userLabel;

    ObservableList<String> usersModel = FXCollections.observableArrayList();

    Map<String, Group> mapGroups = new HashMap<>();

    Map<String, GroupMessage>  mapMessages = new HashMap<>();

    ObservableList<String> messageModel = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        usersList.setOnMouseClicked(event -> {
            var cell = usersList.getSelectionModel().getSelectedItem();
            if(event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY
                && !cell.isEmpty()) {
                userLabel.setText(cell);
                if(cell.contains(",")) {
                    this.dest = null;
                    this.group = mapGroups.get(cell);
                }
                else {
                    this.group = null;
                    var names = cell.split("\\s");

                    this.dest = UserDBService.executeSQLSelect(
                            "select * from users u where u.first_name like '%" + names[0] + "%'" +
                                    " and u.last_name like '%" + names[1] + "%'"
                    ).iterator().next();
                }
                messagesList.setCellFactory(data -> new PositionedListViewCell(this.dest, this.group, this));
                initMessagesModel();
            }
        });

        messagesList.setCellFactory(data -> new PositionedListViewCell(this.dest, this.group, this));

        usersList.setItems(usersModel);

        messagesList.setItems(messageModel);

    }

    static class PositionedListViewCell extends ListCell<String> {

        private final Utilizator dest;
        private final Group group;
        private final InboxController controller;

        public PositionedListViewCell(Utilizator dest, Group group, InboxController controller) {
            this.dest = dest;
            this.group = group;
            this.controller = controller;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                HBox hBox = new HBox();
                if(this.dest != null) {
                    Label labelTime;
                    Label labelText;

                    String[] datas = item.split("\\s");
                    if (datas[0].startsWith("(")) {
                        hBox.setAlignment(Pos.CENTER_RIGHT);

                        StringBuilder text = new StringBuilder();
                        StringBuilder time = new StringBuilder();
                        int ok = 1;
                        for (String data : datas) {
                            if (!data.startsWith("(") && ok == 1) {
                                text.append(data);
                                text.append(" ");
                            } else if (data.startsWith("(") && ok == 1) {
                                ok = 0;
                                time.append(data);
                                time.append(" ");
                            } else if (data.endsWith(")") && ok == 0) {
                                ok = 1;
                                time.append(data);
                                time.append(" ");
                            } else if (ok == 0) {
                                time.append(data);
                                time.append(" ");
                            }
                        }

                        labelText = new Label(text.toString());
                        labelText.setAlignment(Pos.CENTER_RIGHT);
                        labelText.setFont(Font.font(14));

                        labelTime = new Label(time.toString());
                        labelTime.setAlignment(Pos.CENTER_RIGHT);
                        labelTime.setFont(Font.font(10));

                        hBox.getChildren().addAll(labelTime, labelText);
                    } else {
                        hBox.setAlignment(Pos.CENTER_LEFT);

                        StringBuilder text = new StringBuilder();
                        StringBuilder time = new StringBuilder();
                        int ok = 1;
                        for (String data : datas) {
                            if (!data.startsWith("(") && ok == 1) {
                                text.append(data);
                                text.append(" ");
                            } else if (data.startsWith("(") && ok == 1) {
                                ok = 0;
                                time.append(data);
                                time.append(" ");
                            } else if (data.endsWith(")") && ok == 0) {
                                ok = 1;
                                time.append(data);
                                time.append(" ");
                            } else if (ok == 0) {
                                time.append(data);
                                time.append(" ");
                            }
                        }

                        labelText = new Label(text.toString());
                        labelText.setAlignment(Pos.CENTER_LEFT);
                        labelText.setFont(Font.font(14));

                        // Create centered Label
                        labelTime = new Label(time.toString());
                        labelTime.setAlignment(Pos.CENTER_LEFT);
                        labelTime.setFont(Font.font(10));

                        hBox.getChildren().addAll(labelText, labelTime);
                    }
                }
                else if(this.group != null) {
                    Label labelTime;
                    Label labelText;
                    Label labelFrom;

                    String[] datas = item.split("\\s");

                    if(datas[0].startsWith("[")){
                        hBox.setAlignment(Pos.CENTER_LEFT);

                        labelFrom = new Label(datas[0]);
                        labelFrom.setAlignment(Pos.CENTER_LEFT);
                        labelFrom.setFont(Font.font("Verdana", FontWeight.BLACK, 8));

                        int i = 1;
                        StringBuilder text = new StringBuilder();
                        while(!datas[i].startsWith("(")) {
                            text.append(datas[i++]);
                            text.append(" ");
                        }

                        labelText = new Label(text.toString());
                        labelText.setAlignment(Pos.CENTER_LEFT);
                        labelText.setFont(Font.font(14));

                        labelTime = new Label(datas[i] + " " + datas[i+1] + " " + datas[i+2]);
                        labelTime.setAlignment(Pos.CENTER_LEFT);
                        labelTime.setFont(Font.font(10));

                        ImageView reply = new ImageView(
                                "ro/ubbcluj/map/socialnetworkgui/images/replyIcon.jpg");
                        reply.setFitHeight(25);
                        reply.setFitWidth(25);
                        reply.setOnMouseClicked(event -> {
                            if(event.getButton() == MouseButton.PRIMARY &&
                                event.getClickCount() == 1) {
                                this.controller.handleReply(item);
                            }
                        });

                        hBox.getChildren().addAll(labelFrom,labelText,labelTime, reply);
                    }

                    else {
                        hBox.setAlignment(Pos.CENTER_RIGHT);

                        labelTime = new Label(datas[0] + " " + datas[1] + " " + datas[2]);
                        labelTime.setAlignment(Pos.CENTER_RIGHT);
                        labelTime.setFont(Font.font(10));

                        StringBuilder text = new StringBuilder();
                        int i = 3;
                        while(i < datas.length) {
                            text.append(datas[i++]);
                            text.append(" ");
                        }

                        labelText = new Label(text.toString());
                        labelText.setAlignment(Pos.CENTER_RIGHT);
                        labelText.setFont(Font.font(14));

                        hBox.getChildren().addAll(labelTime,labelText);
                    }

                }
                setGraphic(hBox);
            }

        }
    }

    private void initUsersModel() {
        var users = service.getFriendsWithChat(user);
        List<String> strings = new ArrayList<>();
        users.forEach(user -> strings.add(user.getFirstName() + " " + user.getLastName()));

        var groups = service.getGroupsOf(user);
        mapGroups = new HashMap<>();
        groups.forEach(group -> {
            StringBuilder s = new StringBuilder();
            group.getUsers().forEach(user -> {
                if(user.equals(this.user)) {
                    s.append("Dvs., ");
                }
                else {
                    s.append(user.getFirstName()).append(" ").append(user.getLastName()).append(", ");
                }
            });
//            s.delete(s.length()-1, s.length()-1);

            mapGroups.put(s.toString(), group);

            strings.add(s.toString());
        });

        usersModel.setAll(strings);
    }

    private void initMessagesModel() {
        List<String> strings = new ArrayList<>();

        if(dest != null) {
            var messages = service.getMessages(user, dest);

            messages.forEach((message) -> {

                String text;
                if (message.getFrom().equals(user)) {
                    text = DateConverter.convert(message.getDate())
                            + "  " + message.getText();
                } else {
                    text = message.getText() + " " + DateConverter.convert(message.getDate());
                }
                strings.add(text);
            });
        }

        else if(group != null) {

            mapMessages = new HashMap<>();

            var messages = service.getMessagesGroup(group);

            messages.forEach(message -> {
                String text;
                if(message.getFrom().equals(user)) {
                    text = DateConverter.convert(message.getDate()) +
                            " " + message.getText();
                }
                else {
                    text = "[" + message.getFrom().getFirstName() + message.getFrom().getLastName() + "] " +
                            message.getText() + " " + DateConverter.convert(message.getDate());
                }
                mapMessages.put(text, message);
                strings.add(text);
            });
        }

        messageModel.setAll(strings);
    }

    public void setService(SocialNetworkService service) {
        this.service = service;
        service.addObserver(this);
        this.group = null;
        this.dest = null;
        initUsersModel();
    }

    public void setUser(Utilizator user) {
        this.user = user;
    }

    @FXML
    public void handleSendMessage() {
        try {
            if(dest != null) {
                Message message = new Message(
                        user, dest, messageFld.getText()
                );
                service.addMessage(message);
            }
            else if(group != null) {
                GroupMessage groupMessage = new GroupMessage(
                        group.getId(), user, messageFld.getText()
                );
                service.addGroupMessage(groupMessage);
            }
            messageFld.setText("");
        }catch (Exception e) {
            MessageAlert.showErrorMessage(null, "Couldn't send message");
        }
    }

    @FXML
    public void handleNewConvo() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("views/friends-view.fxml"));

            AnchorPane root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Open chat with");
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);

            dialogStage.setScene(scene);

            FriendsController friendsController =
                    loader.getController();

            friendsController.setUser(user);
            friendsController.setService(service);
            friendsController.setMainController(this);
            friendsController.setDialog(dialogStage);

            dialogStage.show();

        }catch (ValidationException | FriendshipAlreadyExistsException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleNewDest(Utilizator dest) {
        this.dest = dest;
        userLabel.setText(dest.getFirstName() + " " + dest.getLastName());
        usersModel.add(dest.getFirstName() + " " + dest.getLastName());
    }

    @FXML
    public void handleNewGroup() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("views/group-view.fxml"));

            AnchorPane root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Make new group with");
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);

            dialogStage.setScene(scene);

            GroupController groupController =
                    loader.getController();

            groupController.setUser(user);
            groupController.setService(service);
            groupController.setMainController(this);
            groupController.setDialog(dialogStage);

            dialogStage.show();

        }catch (ValidationException | FriendshipAlreadyExistsException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleReply(String strMess) {
        try {
            GroupMessage message = mapMessages.get(strMess);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("views/reply-view.fxml"));

            AnchorPane root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Reply");
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);

            dialogStage.setScene(scene);

            ReplyController replyController =
                    loader.getController();

            replyController.setFrom(user);
            replyController.setTo(message.getFrom());
            replyController.setService(service);
            replyController.setStage(dialogStage);

            dialogStage.show();

        }catch (ValidationException | FriendshipAlreadyExistsException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showInfo() {
        if(dest != null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("views/userInfo-view.fxml"));

                AnchorPane root = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Info");
                dialogStage.initModality(Modality.WINDOW_MODAL);

                Scene scene = new Scene(root);

                dialogStage.setScene(scene);

                UserInfoController userInfoController =
                        loader.getController();

                userInfoController.setUser(user);
                userInfoController.setService(service);
                userInfoController.setDest(dest);

                dialogStage.show();

            }catch (ValidationException | FriendshipAlreadyExistsException e) {
                MessageAlert.showErrorMessage(null, e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if(group != null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("views/groupInfo-view.fxml"));

                AnchorPane root = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Info");
                dialogStage.initModality(Modality.WINDOW_MODAL);

                Scene scene = new Scene(root);

                dialogStage.setScene(scene);

                GroupInfoController groupInfoController =
                        loader.getController();

                groupInfoController.setGroup(group);
                groupInfoController.setUser(user);
                groupInfoController.setService(service);

                dialogStage.show();

            }catch (ValidationException | FriendshipAlreadyExistsException e) {
                MessageAlert.showErrorMessage(null, e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void update(UsersChangedEvent usersChangedEvent) {
        initUsersModel();
        initMessagesModel();
    }
}
