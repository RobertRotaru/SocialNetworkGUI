module ro.ubbcluj.map.socialnetworkgui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;

    opens ro.ubbcluj.map.socialnetworkgui to javafx.fxml;
    opens ro.ubbcluj.map.socialnetworkgui.controlers to javafx.fxml;
    opens ro.ubbcluj.map.socialnetworkgui.domain to javafx.base;
    opens ro.ubbcluj.map.socialnetworkgui.service to javafx.fxml;
    opens ro.ubbcluj.map.socialnetworkgui.repository.dbrepos to javafx.fxml;
    exports ro.ubbcluj.map.socialnetworkgui;
    exports ro.ubbcluj.map.socialnetworkgui.console;
    opens ro.ubbcluj.map.socialnetworkgui.console to javafx.fxml;
}