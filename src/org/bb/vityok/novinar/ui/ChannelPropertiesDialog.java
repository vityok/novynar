package org.bb.vityok.novinar.ui;

import org.bb.vityok.novinar.core.UpdatePeriod;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

class ChannelPropertiesDialog extends Alert
{
    final GridPane grid = new GridPane();
    final TextField fldUrl = new TextField();
    final TextField fldTitle = new TextField();
    final CheckBox cbIgnoreOnBoot = new CheckBox();
    final ComboBox<UpdatePeriod> cbxUpdatePeriod = new ComboBox<>();
    final Label lblProblems = new Label("Problems: ");
    final Label txtProblems = new Label("");

    public ChannelPropertiesDialog(String title) {
        super(Alert.AlertType.INFORMATION);
        setTitle(title);
        setHeaderText("Specify channel parameters");
        // ButtonType.OK is there by default
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        fldUrl.setPromptText("Enter URL");
        fldTitle.setPromptText("Channel title");
        fldUrl.setPrefWidth(400);
        fldTitle.setPrefWidth(400);

        grid.add(new Label("Feed URL:"), 0, 0);
        grid.add(fldUrl, 1, 0);

        grid.add(new Label("Feed title:"), 0, 1);
        grid.add(fldTitle, 1, 1);

        grid.add(new Label("Ignore on boot:"), 0, 2);
        grid.add(cbIgnoreOnBoot, 1, 2);

        cbxUpdatePeriod.getItems().setAll(UpdatePeriod.values());
        cbxUpdatePeriod.setValue(UpdatePeriod.HOURS_3);
        grid.add(new Label("Update period:"), 0, 3);
        grid.add(cbxUpdatePeriod, 1, 3);
        
        grid.add(lblProblems, 0, 4);
        grid.add(txtProblems, 1, 4);

        getDialogPane().setContent(grid);

        init();
    }

    public void init() {
        // Request focus on the URL field by default.
        Platform.runLater(() -> fldUrl.requestFocus());
    }

    public void execute() {
        showAndWait().ifPresent(response -> handleResponse(response));
    }

    public void handleResponse(ButtonType result) {
    }
} // end ChannelPropertiesDialog