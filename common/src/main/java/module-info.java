module com.common {

    exports com.common.model;
    exports com.common.network;

    opens com.common to javafx.fxml;
}