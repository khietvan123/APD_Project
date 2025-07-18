package com.khietvan.hotelreservation.controllers;

import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainScreenController {

    @FXML
    private Button checkInGuidanceBtn;

    @FXML
    private Button fastCheckInBtn;

    @FXML
    private ImageView gallery;

    private int currentIndex = 0;
    private Timeline timeline;

    @FXML
    public void initialize() {
    }

}
