package ro.ubbcluj.map.socialnetworkgui.controlers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Callback;
import ro.ubbcluj.map.socialnetworkgui.domain.Request;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.DateConverter;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UsersChangedEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.time.OffsetDateTime;

public class RequestsController implements Observer<UsersChangedEvent> {
    private SocialNetworkService service;
    private Utilizator to;
    private Stage stage;

    ObservableList<Request> model = FXCollections.observableArrayList();

    @FXML
    TableView<Request> requestsTable;

    @FXML
    TableColumn<Request, String> fromColumn;

    @FXML
    TableColumn<Request, String> statusColumn;

    @FXML
    TableColumn<Request, String> dateColumn;

    ContextMenu contextMenu = new ContextMenu();

    MenuItem acceptedItem = getMenuItemForLine("Accept");
    MenuItem rejectedItem = getMenuItemForLine("Reject");

    private MenuItem getMenuItemForLine(String menuName)
    {
        Label menuLabel = new Label(menuName);
        MenuItem menuItem = new MenuItem();
        menuItem.setGraphic(menuLabel);
        return menuItem;
    }

    public void setService(SocialNetworkService service) {
        this.service = service;
        service.addObserver(this);
        initModel();
    }

    private void initModel() {
        var requests = service.getRequests(to);
        model.setAll(requests);
    }

    @FXML
    public void initialize() {
        fromColumn.setCellValueFactory((celldata) -> new ReadOnlyStringWrapper(
                celldata.getValue().getFrom().getFirstName() + " " +
                        celldata.getValue().getFrom().getLastName()
        ));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory((celldata) -> new ReadOnlyStringWrapper(
                DateConverter.convert(celldata.getValue().getDate())
        ));

        requestsTable.setRowFactory((tv) -> {
            TableRow<Request> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                    row.setContextMenu(contextMenu);
                }
            });
            return row;
        });

        acceptedItem.setOnAction(event -> handleAccept());

        rejectedItem.setOnAction(event -> handleReject());

        contextMenu.getItems().addAll(acceptedItem, rejectedItem);

        requestsTable.setItems(model);

    }

    public void setTo(Utilizator to) {
        this.to = to;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void handleAccept() {
        try {
            Request request = requestsTable.getSelectionModel().getSelectedItem();
            Utilizator from = request.getFrom();
            service.acceptRequest(from, to);
            MessageAlert.showMessage(stage, Alert.AlertType.CONFIRMATION, "Accepted request",
                    "Request " + request + " has been accepted successfully!");

        } catch (Exception e) {
            MessageAlert.showErrorMessage(stage, e.getMessage());
        }
    }

    private void handleReject() {
        try {
            Request request = requestsTable.getSelectionModel().getSelectedItem();
            Utilizator from = request.getFrom();
            service.rejectRequest(from, to);
            MessageAlert.showMessage(stage, Alert.AlertType.CONFIRMATION, "Rejected request",
                    "Request " + request + " has been rejected successfully!");

        } catch (Exception e) {
            MessageAlert.showErrorMessage(stage, e.getMessage());
        }
    }

    @Override
    public void update(UsersChangedEvent usersChangedEvent) {
        initModel();
    }
}
