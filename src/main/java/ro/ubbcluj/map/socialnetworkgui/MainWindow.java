package ro.ubbcluj.map.socialnetworkgui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.controlers.*;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.FriendshipAlreadyExistsException;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UsersChangedEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MainWindow implements Observer<UsersChangedEvent> {
        SocialNetworkService service;

        ObservableList<Utilizator> model = FXCollections.observableArrayList();

        ObservableList<Integer> valuesChoice = FXCollections.observableArrayList();

        List<Utilizator> selectedUsers = new ArrayList<>();

        @FXML
        AnchorPane usersview;

        @FXML
        TableView<Utilizator> usersTable;

        @FXML
        TableColumn<Utilizator, Long> idColumn;

        @FXML
        TableColumn<Utilizator, String> surnameColumn;

        @FXML
        TableColumn<Utilizator, String> firstNameColumn;

        @FXML
        TableColumn<Utilizator, CheckBox> checkboxColumn;

        @FXML
        Button addUserBttn;

        @FXML
        TextField firstNameTxtFld;

        @FXML
        TextField surnameTxtFld;

        @FXML
        ChoiceBox<Integer> choiceBox;

        @FXML
        TextField pageSizeFld;

//        ContextMenu menuFor2 = new ContextMenu();
        ContextMenu menuFor1 = new ContextMenu();

        MenuItem optFriendship = getMenuItemForLine("Send request");
        MenuItem optShowRequest = getMenuItemForLine("Show pending requests");
        MenuItem optInbox = getMenuItemForLine("Show inbox");

        public void setUserService(SocialNetworkService userService) {
                service = userService;

                service.setPageSizeUsers(8);
                pageSizeFld.setText(String.valueOf(8));

                service.addObserver(this);

                Long noPages = Long.parseLong(service.getNoPagesUsers());

                for(int i = 1; i <= noPages; ++i) {
                        valuesChoice.add(i);
                }

                choiceBox.setItems(valuesChoice);

                choiceBox.setValue(1);

                initModel(1);
        }

        private MenuItem getMenuItemForLine(String menuName)
        {
                Label menuLabel = new Label(menuName);
                MenuItem menuItem = new MenuItem();
                menuItem.setGraphic(menuLabel);
                return menuItem;
        }

        private void setSelected() {
                selectedUsers = usersTable.getSelectionModel().getSelectedItems();
//                System.out.println(selectedUsers.size());
        }

        @FXML
        public void initialize() {
                idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
                firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
                surnameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
                checkboxColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));

                usersTable.getSelectionModel().setSelectionMode(
                       SelectionMode.MULTIPLE
                );

                usersTable.setRowFactory((tv) -> {
                        TableRow<Utilizator> row = new TableRow<>();
                        row.setOnMouseClicked(event -> {
                                setSelected();
                                if(event.getButton() == MouseButton.PRIMARY &&
                                        event.getClickCount() == 1 && !row.isEmpty()) {
                                        Utilizator user = row.getItem();
                                        setFields(user);
                                        if(selectedUsers.size() == 1) {
                                                row.setContextMenu(menuFor1);
                                        }
                                }
                                else if(event.getButton() == MouseButton.PRIMARY &&
                                        event.getClickCount() == 1 && row.isEmpty()) {
                                        clearFields();
                                }
//                                else if(event.getButton() == MouseButton.SECONDARY &&
//                                        selectedUsers.size() == 2) {
//                                        row.setContextMenu(menuFor2);
//                                }
                        });
                        return row;
                });

                choiceBox.valueProperty().addListener(new ChangeListener<Integer>() {
                        @Override
                        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                                choiceBox.setValue(newValue);
                                if(newValue == null)
                                        newValue = 1;
                                initModel(newValue);
                        }
                });

                pageSizeFld.setOnAction(event -> handleChangePageSize());

                optFriendship.setOnAction((event) -> handleMakeFriends());

                optShowRequest.setOnAction((event) -> handleShowRequest());

                optInbox.setOnAction(event -> handleInbox());

                menuFor1.getItems().addAll(optFriendship, optShowRequest, optInbox);

                usersTable.setItems(model);
        }

        private void initModel(int page) {
                Iterable<Utilizator> users = service.getUsersOnPage(page);
                List<Utilizator> usersList = StreamSupport.stream(users.spliterator(), false)
                        .collect(Collectors.toList());
                model.setAll(usersList);
        }

        private void clearFields() {
                firstNameTxtFld.setPromptText("Ion");
                firstNameTxtFld.setText("");
                surnameTxtFld.setPromptText("Popescu");
                surnameTxtFld.setText("");
        }

        private void setFields(Utilizator user) {
                firstNameTxtFld.setText(user.getFirstName());
                surnameTxtFld.setText(user.getLastName());
        }

        private void handleInbox() {
                try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("views/inbox-view.fxml"));

                        AnchorPane root = loader.load();

                        Utilizator user = selectedUsers.get(0);

                        Stage dialogStage = new Stage();
                        dialogStage.setTitle("Inbox for " + user.getFirstName() + " " + user.getLastName());
                        dialogStage.initModality(Modality.WINDOW_MODAL);

                        Scene scene = new Scene(root);

                        dialogStage.setScene(scene);

                        InboxController inboxController =
                                loader.getController();

                        inboxController.setUser(user);
                        inboxController.setService(service);

                        dialogStage.show();

                }catch (ValidationException |FriendshipAlreadyExistsException e) {
                        MessageAlert.showErrorMessage(null, e.getMessage());
                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
        }

        private void handleShowRequest() {
                try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("views/requests-view.fxml"));

                        AnchorPane root = loader.load();

                        Stage dialogStage = new Stage();
                        dialogStage.setTitle("Requests");
                        dialogStage.initModality(Modality.WINDOW_MODAL);

                        Scene scene = new Scene(root);

                        dialogStage.setScene(scene);

                        RequestsController requestsController =
                                loader.getController();

                        requestsController.setTo(selectedUsers.get(0));
                        requestsController.setService(service);
                        requestsController.setStage(dialogStage);

                        dialogStage.show();

                }catch (ValidationException |FriendshipAlreadyExistsException e) {
                        MessageAlert.showErrorMessage(null, e.getMessage());
                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
        }

        private void handleMakeFriends() {
                try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("views/possible-friends-view.fxml"));

                        AnchorPane root = loader.load();

                        Stage dialogStage = new Stage();
                        dialogStage.setTitle("Possible friends");
                        dialogStage.initModality(Modality.WINDOW_MODAL);

                        Scene scene = new Scene(root);

                        dialogStage.setScene(scene);

                        PossibleFriendsController possibleFriendsController =
                                loader.getController();

                        possibleFriendsController.setFromUser(selectedUsers.get(0));
                        possibleFriendsController.setStage(dialogStage);
                        possibleFriendsController.setService(service);

                        dialogStage.show();

                }catch (ValidationException |FriendshipAlreadyExistsException e) {
                        MessageAlert.showErrorMessage(null, e.getMessage());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }

//        private void showMessages(List<Utilizator> selectedUsers) {
//                try{
//                        FXMLLoader loader = new FXMLLoader();
//                        loader.setLocation(getClass().getResource("views/messages-view.fxml"));
//
//                        System.out.println(loader.getLocation());
//
//                        AnchorPane root = loader.load();
//
//                        Stage dialogStage = new Stage();
//                        dialogStage.setTitle("Messages");
//                        dialogStage.initModality(Modality.WINDOW_MODAL);
//
//                        Scene scene = new Scene(root);
//
//                        dialogStage.setScene(scene);
//
//                        MessagesController editMessageViewController = loader.getController();
//
//                        Utilizator user1 = selectedUsers.get(0);
//                        Utilizator user2 = selectedUsers.get(1);
//
//                        editMessageViewController.setService(service, user1, user2);
//
//                        dialogStage.show();
//                } catch (IOException e) {
//                        throw new RuntimeException(e);
//                }
//        }
//
//        private void handleGetMessages() {
//                showMessages(selectedUsers);
//        }

        @FXML
        public void handleSave(ActionEvent ev) {
                String firstName = firstNameTxtFld.getText();
                String surname = surnameTxtFld.getText();
                Utilizator user = new Utilizator(firstName, surname);

                saveUser(user);
                clearFields();
        }

        @FXML
        public void handleUpdate(ActionEvent ev) {
                Utilizator selectedUser = usersTable.getSelectionModel().getSelectedItem();
                selectedUser.setFirstName(firstNameTxtFld.getText());
                selectedUser.setLastName(surnameTxtFld.getText());

                try {
                        Optional<Utilizator> taskResult = service.updateUser(selectedUser);
                        if(taskResult.isPresent()) {
                                throw new ValidationException("The user could not be updated!");
                        }
                        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Updated user",
                                "User with ID: " + selectedUser.getId() + " has been updated with " +
                                        selectedUser.getFirstName() + " " + selectedUser.getLastName());
                }catch (ValidationException e) {
                        MessageAlert.showErrorMessage(null, e.getMessage());
                }
        }

        @FXML
        public void handleDelete(ActionEvent ev) {
                List<Utilizator> deletedUsers = new ArrayList<>();
                for(var bean : model) {
                        if(bean.getSelected().isSelected()) {
                                deletedUsers.add(bean);
                        }
                }

                try{
                        deletedUsers.forEach((user) -> {
                                Optional<Utilizator> taskResult;
                                try {
                                        taskResult = service.deleteUser(user.getId());
                                } catch (FileNotFoundException e) {
                                        throw new RuntimeException(e);
                                }
                                if(taskResult.isEmpty()) {
                                        throw new ValidationException("The user with id " + user.getId() +
                                                " could not be deleted!");
                                }
                        });
                        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Deleted users",
                                "The users have been deleted successfully!");
                }catch (ValidationException e) {
                        MessageAlert.showErrorMessage(null, e.getMessage());
                }
        }

        private void saveUser(Utilizator user) {
                try {
                        Optional<Utilizator> addedUser = service.addUser(user);
                        if(addedUser.isPresent()) {
                                throw new ValidationException("The user could not be added!");
                        }
                        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Saved user",
                                "User " + user.getFirstName() + " " + user.getLastName() +
                                " has been added successfully!");
                }catch(ValidationException e) {
                        MessageAlert.showErrorMessage(null, e.getMessage());
                }
        }

        private void handleChangePageSize() {
                String pageSizeStr = pageSizeFld.getText();
                try{
                        int temp = Integer.parseInt(pageSizeStr);
                        int noUsers = service.getNoUsers();
                        int pageSize = (Math.min(temp, noUsers));
                        if(pageSize != 0) {
                                service.setPageSizeUsers(pageSize);

                                List<Integer> vals = new ArrayList<>();

                                for(int i = 1; i <= Integer.parseInt(service.getNoPagesUsers()); ++i) {
                                        vals.add(i);
                                }

                                valuesChoice.setAll(vals);

                                choiceBox.setValue(1);

                                initModel(1);
                        }
                }catch(NumberFormatException ignore) {}
        }

        @Override
        public void update(UsersChangedEvent usersChangedEvent) {
                initModel(service.getUsersPage());
        }
}
