module com.server {
    //requires javafx.controls;
    //requires javafx.fxml;
    requires java.sql;
    requires java.prefs;
    requires com.common;


  //  opens com.server to javafx.fxml;
    exports com.server;
}