package org.bb.vityok.novinar.ui;

import org.w3c.dom.Node;

import org.bb.vityok.novinar.core.Outline;
import org.bb.vityok.novinar.core.OPMLManager;

import javafx.collections.FXCollections;

import javafx.scene.control.TreeItem;

import javafx.scene.image.ImageView;

/** This node represents the trash bin in the outlines tree.
 *
 * <p>An outline is not used here as this node doesn't represent any
 * outlines (either folders or channels). It is a placeholder for a
 * "virtual" folder - trash bin.
 */
public class TrashBinTreeItem extends TreeItem<Outline> {

    final NovinarApp novinarApp;
    final ImageView imvTrash;
    
    public TrashBinTreeItem(NovinarApp novinarApp, Outline ol) {
	super(ol);
	System.out.println("_____van: Trash bin <init>");

	this.novinarApp = novinarApp;

	imvTrash = new ImageView(novinarApp.imageTrash);

	setGraphic(imvTrash);

	getChildren().setAll(FXCollections.emptyObservableList());
    }

    @Override
    public String toString() {
	return "Trash";
    }

}
