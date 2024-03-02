package ro.ubbcluj.map.socialnetworkgui;

import javafx.application.Application;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.console.Console;
import ro.ubbcluj.map.socialnetworkgui.console.dbconsole.DBConsole;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        String url = "jdbc:postgresql://localhost:5432/SocialNetwork";
        String user = "postgres";
        String password = "postgres";

        Console<Long, Utilizator> gui = new DBConsole<>(
                url, user, password
        );
        gui.showUI();
    }
}
