package org.bb.vityok.novinar.ui;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.bb.vityok.novinar.core.NewsItem;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Pop-up dialog with the wikipedia cite news/web autogenerated
 * template.
 */
class ShareNewsItemDialog extends Alert
{
    /**
     * 
     */
    private final NovinarApp novinarApp;
    final GridPane grid = new GridPane();
    final TextArea ta = new TextArea();
    final VBox pane = new VBox();
    final CheckBox cbWrap = new CheckBox();
    final CheckBox cbName = new CheckBox();
    final CheckBox cbAuthor = new CheckBox();
    final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault());;

    public ShareNewsItemDialog(NovinarApp novinarApp, String title) {
        super(Alert.AlertType.INFORMATION);
        this.novinarApp = novinarApp;
        setTitle(title);
        setHeaderText("Wiki reference");
        // ButtonType.OK is there by default, we don't need any
        // other buttons as nothing is being done here

        // select all ta (generated template text) text upon text field activation (either
        // with the mouse click or keyboard navigation)
        ta.focusedProperty().addListener(new ChangeListener<Boolean>() {
    	    @Override
    	    public void changed(ObservableValue ov, Boolean t, Boolean t1) {

    		Platform.runLater(new Runnable() {
    			@Override
    			public void run() {
    			    if (ta.isFocused() && !ta.getText().isEmpty()) {
    				ta.selectAll();
    			    }
    			}
    		    });
    	    }
    	});
        ta.setEditable(false);

        genText();

        cbWrap.setOnAction((ActionEvent evt) -> genText());
        grid.add(new Label("Wrap lines"), 0, 0);
        grid.add(cbWrap, 1, 0);

        cbName.setOnAction((ActionEvent evt) -> genText());
        grid.add(new Label("Name reference"), 0, 1);
        grid.add(cbName, 1, 1);
        
        cbAuthor.setOnAction((ActionEvent evt) -> genText());
        grid.add(new Label("Specify author"), 0, 2);
        grid.add(cbAuthor, 1, 2);

        pane.getChildren().addAll(ta, grid);
        getDialogPane().setContent(pane);
        init();
    }

    /** Actually generates the reference text and inserts it into
     * the textarea. */
    public void genText() {
        final NewsItem item = this.novinarApp.itemsTable.getSelectionModel().getSelectedItem();
        final String strDate = format.format(item.getDateCalendar());
        final String nl = cbWrap.isSelected() ? "\n" : "";
        final String strPublisher = this.novinarApp.novinar.getChannelById(item.getChannelId()).getTitle();
        final String creator = item.getCreator();

        // guess ref name
        String name = "";
        if (cbName.isSelected()) {
    	if (strPublisher != null
    	    && strPublisher.length() < 4) { 
    	    name = " name=\""
    		+ strPublisher.toLowerCase() + "."
    		+ strDate + "\"";
    	} else {
    	    name = " name=\"" + strDate + "\"";
    	}
        }
        
        String author = "";
        if (cbAuthor.isSelected()
    	    && creator != null
    	    && !creator.isEmpty()) {
    	author = " | author = " + creator + nl;
        }

        // produce ref text
        String wikiRef = "<ref" + name + ">{{cite web" + nl
    	+ " | url = " + item.getLink() + nl
    	+ " | title = " + item.getTitle() + nl
    	+ author
    	+ " | publisher = " + strPublisher + nl
    	+ " | date = " + strDate + nl
    	+ "}}</ref>";
        ta.setText(wikiRef);
    }

    public void init() {
        // Request focus on the URL field by default.
        Platform.runLater(() -> ta.requestFocus());
    }

    public void execute() {
        showAndWait().ifPresent(response -> handleResponse(response));
    }

    public void handleResponse(ButtonType result) {
    }

} // end ShareNewsItemDialog