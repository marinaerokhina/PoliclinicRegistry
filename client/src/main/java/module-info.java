module com.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.common;

    opens com.client to javafx.fxml;
    exports com.client;
}