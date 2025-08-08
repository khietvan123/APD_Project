package com.khietvan.hotelreservation.controllers.Guidance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class NumberOfPeopleController {

    @FXML private TextField noOfAdults;
    @FXML private TextField noOfChildren;
    @FXML private TextField noOfSenior;
    @FXML private Button nextBtn;  // <-- Make sure it's injected!

    @FXML
    private void initialize() {
        // Wire the button action
        nextBtn.setOnAction(this::handleNext);
    }

    public void prefillGuestCounts(int adults, int children, int seniors) {
        noOfAdults.setText(String.valueOf(adults));
        noOfChildren.setText(String.valueOf(children));
        noOfSenior.setText(String.valueOf(seniors));
    }

    private void handleNext(ActionEvent event) {
        int adults = parseIntSafe(noOfAdults.getText());
        int children = parseIntSafe(noOfChildren.getText());
        int seniors = parseIntSafe(noOfSenior.getText());
        int total = adults + children + seniors;

        if (total == 0) {
            showAlert("Please enter at least 1 guest.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/khietvan/hotelreservation/Guidance/date-in-out-room.fxml"));

            Parent root = loader.load();

            DateInOutRoomController controller = loader.getController();
            controller.setGuestCounts(adults, children, seniors);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Dates and Rooms");
            stage.show();

            // Close current window
            ((Stage) nextBtn.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to load the next step.");
        }
    }

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
