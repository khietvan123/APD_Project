module com.khietvan.hotelreservation {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.khietvan.hotelreservation to javafx.fxml;
    exports com.khietvan.hotelreservation;
}