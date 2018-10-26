package org.bb.vityok.novinar.ui;

import java.util.Comparator;
import java.util.List;

import org.bb.vityok.novinar.core.Channel;
import org.bb.vityok.novinar.core.Outline;

import javafx.application.Platform;

import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 * Encapsulates Outline and serves as the view to represent its data to the
 * user.
 */
public class OutlineTreeItem extends TreeItem<Outline> {
    /**
     *
     */
    private final NovinarApp novinarApp;

    private final ImageView imvProblem;
    private final ImageView imvFeed;

    public OutlineTreeItem(NovinarApp novinarApp, Outline ol) {
	super(ol);
	this.novinarApp = novinarApp;
	imvProblem = new ImageView(novinarApp.imageProblem);
	imvFeed = new ImageView(novinarApp.imageFeed);

	rebuildChildren();
	if (ol.isChannel()) {
	    // register for various status updates
	    Channel chan = ol.getChannel();
	    final ImageView defaultIcon = chan.hasProblems() ? imvProblem : imvFeed;
	    setGraphic(defaultIcon);
	    chan.hasProblemsProperty()
		    .addListener((ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
			System.out.println("change: " + ov);
			// change the icon for this item. if the new value is true,
			// it means there are problems
			final ImageView newIcon = newVal ? imvProblem : imvFeed;
			Platform.runLater(() -> setGraphic(newIcon));
		    });
	}
    }

    @Override
    public String toString() {
	return ((Outline) getValue()).toString();
    }

    private ObservableList<TreeItem<Outline>> buildChildren() {
	Outline ol = getValue();

	if (ol != null && ol.hasChildren()) {
	    ObservableList<TreeItem<Outline>> children = FXCollections.observableArrayList();
	    List<Outline> outlines = ol.getChildren();
	    outlines.sort(Comparator.comparing(Outline::getTitle));

	    outlines.forEach((childOutline) -> {
		children.add(new OutlineTreeItem(this.novinarApp, childOutline));
	    });

	    if (ol.isRoot()) {
		System.out.println("_____van: Trash bin added");
		children.add(new TrashBinTreeItem(novinarApp));
	    }

	    return children;
	}
	return FXCollections.emptyObservableList();
    }

    public void rebuildChildren() {
	getChildren().setAll(buildChildren());
    }
} // end class OutlineTreeItem
