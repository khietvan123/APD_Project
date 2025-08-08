package com.khietvan.hotelreservation.controllers.Admin;

import com.khietvan.hotelreservation.dao.DiscountDAO;
import com.khietvan.hotelreservation.dao.GuestDAO;
import com.khietvan.hotelreservation.models.Discount;
import com.khietvan.hotelreservation.models.Guest;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

public class DiscountController {

    @FXML private ComboBox<Discount> discountTitle;
    @FXML private Button applyBtn;
    @FXML private Button cancelBtn;

    private Guest guest; // the guest we’re applying the discount to

    @FXML
    public void initialize() {
        // Render discount items nicely
        discountTitle.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Discount d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) setText(null);
                else setText(d.getTitle() + " (" + d.getAmount() + "%)");
            }
        });
        discountTitle.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Discount d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) setText(null);
                else setText(d.getTitle() + " (" + d.getAmount() + "%)");
            }
        });

        // Load list from DB
        List<Discount> all = DiscountDAO.getAllDiscounts();
        discountTitle.setItems(FXCollections.observableArrayList(all));

        // Buttons
        applyBtn.setOnAction(e -> onApply());
        cancelBtn.setOnAction(e -> close());
    }

    /** Called by the opener to inject the selected guest */
    public void setGuest(Guest guest) {
        this.guest = guest;

        // Preselect existing discount if any
        if (guest != null && guest.getDiscount() != null) {
            // try to find same id in the list
            Discount current = guest.getDiscount();
            discountTitle.getItems().stream()
                    .filter(d -> d.getDiscountId() == current.getDiscountId())
                    .findFirst()
                    .ifPresent(discountTitle::setValue);
        }
    }

    private void onApply() {
        if (guest == null) {
            new Alert(Alert.AlertType.WARNING, "No guest selected.").showAndWait();
            return;
        }
        Discount chosen = discountTitle.getValue();
        if (chosen == null) {
            new Alert(Alert.AlertType.INFORMATION, "Please choose a promotion.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Apply Discount");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Apply " + chosen.getAmount() + "% off with \"" + chosen.getTitle() + "\" promotion to "
                        + guest.getName() + "?"
        );

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = GuestDAO.updateGuestDiscount(guest.getGuestId(), chosen.getDiscountId());
                if (ok) {
                    // update in-memory model so tables refresh correctly
                    guest.setDiscount(chosen);
                    new Alert(Alert.AlertType.INFORMATION,
                            "Discount applied successfully to " + guest.getName() + ".").showAndWait();
                    close();
                } else {
                    new Alert(Alert.AlertType.ERROR,
                            "Failed to apply discount. Please try again.").showAndWait();
                }
            }
        });
    }

    private void close() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
