module com.khietvan.hotelreservation {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;


    opens com.khietvan.hotelreservation to javafx.fxml;
    opens com.khietvan.hotelreservation.models to javafx.base;
    opens com.khietvan.hotelreservation.controllers to javafx.fxml;
    opens com.khietvan.hotelreservation.controllers.fastCheckIn to javafx.fxml;
    opens com.khietvan.hotelreservation.controllers.Guidance to javafx.fxml;
    opens com.khietvan.hotelreservation.controllers.Admin to javafx.fxml;
    exports com.khietvan.hotelreservation.controllers.Admin;
    exports com.khietvan.hotelreservation.controllers.Guidance;
    exports com.khietvan.hotelreservation;
}