package com.khietvan.hotelreservation.controllers;

import com.khietvan.hotelreservation.models.GalleryImage;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class MainScreenController {

    @FXML
    private Button checkInGuidanceBtn;

    @FXML
    private Button fastCheckInBtn;

    @FXML
    private ImageView gallery;

    private final List<GalleryImage> imageList = new ArrayList<>();
    private int currentIndex = 0;
    private Timeline timeline;

    @FXML
    public void initialize() {
    }

}
