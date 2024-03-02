package ro.ubbcluj.map.socialnetworkgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.controlers.ProfileController;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.FriendshipDBService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.MessageDBService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.RequestDBService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.UserDBService;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import static javafx.application.Application.launch;

public class GUIConsole extends Application {

    UserDBService userService;
    FriendshipDBService friendshipService;
    MessageDBService messageService;
    RequestDBService requestDBService;
    SocialNetworkService service;

    Rectangle rectangle;



    public static void main(String[] args) {
//        System.out.println("ok");
        launch(args);
    }

    private void initView(Stage primaryStage) throws IOException {

        Group root = new Group();
        rectangle = new Rectangle(0, 0, Color.RED);
        root.getChildren().addAll(rectangle);

        FXMLLoader loginLoader = new FXMLLoader();
        loginLoader.setLocation(getClass().getResource("views/login-view.fxml"));
        AnchorPane usersLayout = loginLoader.load();
        root.getChildren().addAll(usersLayout);

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);

        LoginWindow loginWindow = loginLoader.getController();

        loginWindow.setService(service);
        loginWindow.setStage(primaryStage);

        primaryStage.setTitle("Welcome to the SocialNetwork");

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/SocialNetwork";
        String user = "postgres";
        String password = "postgres";

//        Console<Long, Utilizator> gui = new DBConsole<>(
//                url, user, password
//        );
//        gui.showUI();

        userService = new UserDBService(
                url, user, password
        );

        friendshipService = new FriendshipDBService(
                url, user, password
        );

        messageService = new MessageDBService(
                url, user, password
        );

        requestDBService = new RequestDBService(
                url, user, password
        );

        service = new SocialNetworkService(userService, friendshipService, messageService, requestDBService,
                url, user, password);

        initView(primaryStage);
//        primaryStage.setWidth(700);
        primaryStage.show();

    }


}
