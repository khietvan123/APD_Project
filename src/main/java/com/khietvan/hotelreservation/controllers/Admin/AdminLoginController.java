package com.khietvan.hotelreservation.controllers.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class AdminLoginController {

    @FXML private TextField userName;
    @FXML private TextField password;
    @FXML private Button loginBtn;
    @FXML private Button cancelBtn;

    // Hardcoded valid credentials
    private final Map<String, String> validAdmins = Map.of(
            "Joanne", "Jo@anne",
            "admin", "password",
            "Devon", "Fiisher"
    );

    @FXML
    public void initialize() {
        loginBtn.setOnAction(this::handleLogin);
        cancelBtn.setOnAction(this::handleCancel);
    }

    private void handleLogin(ActionEvent event) {
        String username = userName.getText().trim();
        String pwd = password.getText().trim();

        if (validAdmins.containsKey(username) && validAdmins.get(username).equals(pwd)) {
            try {
                URL fxmlUrl = getClass().getResource("/com/khietvan/hotelreservation/Admin/admin-management.fxml");
                System.out.println("FXML URL: " + fxmlUrl); // Check if null

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Admin Management");
                stage.show();

                ((Stage)((Node)event.getSource()).getScene().getWindow()).close();

            } catch (IOException e) {
                e.printStackTrace(); // This will help you diagnose
                showAlert("Failed to load Admin Management screen.");
            }
        } else {
            showAlert("Invalid username or password.");
        }
    }


    private void handleCancel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/khietvan/hotelreservation/main-screen.fxml" // adjust path if needed
            ));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Hotel Reservation");
            stage.show();

            ((Stage)((Node)event.getSource()).getScene().getWindow()).close();

        } catch (IOException e) {
            showAlert("Unable to return to the main screen.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
