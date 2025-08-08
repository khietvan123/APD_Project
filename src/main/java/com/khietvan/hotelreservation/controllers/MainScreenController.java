package com.khietvan.hotelreservation.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainScreenController {

    @FXML
    private void handleFastCheckIn(ActionEvent event) throws IOException {
        loadScene(event, "/com/khietvan/hotelreservation/FastCheckIn/fast-check-in.fxml", "Fast Check-In");
    }

    @FXML
    private void handleCheckInGuidance(ActionEvent event) throws IOException {
        loadScene(event, "/com/khietvan/hotelreservation/Guidance/Number-of-people.fxml", "Check-In Guidance");
    }

    @FXML
    private void handleAdminLogin(ActionEvent event) throws IOException {
        loadScene(event, "/com/khietvan/hotelreservation/Admin/admin-login.fxml", "Admin Login");
    }

    @FXML
    private void handleCustomerFeedback(ActionEvent event) throws IOException {
        loadScene(event, "/com/khietvan/hotelreservation/Admin/Feedback.fxml", "Customer Feedback");
    }

    // Reusable method to switch scenes
    private void loadScene(ActionEvent event, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }
}
