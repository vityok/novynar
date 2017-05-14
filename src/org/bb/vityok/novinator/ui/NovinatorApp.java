package org.bb.vityok.novinator.ui;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.bb.vityok.novinator.db.Backend;
import org.bb.vityok.novinator.feed.FeedReader;

public class NovinatorApp extends Application {

    public static void start(String[] args) {
        launch(args);
    }

    private HBox buildMenuBar() {
	HBox box = new HBox();
	// **** MENU BAR
	MenuBar menuBar = new MenuBar();

        // --- Menu File
        Menu menuFile = new Menu("File");

	MenuItem clear = new MenuItem("Clear");
	clear.setOnAction(new EventHandler<ActionEvent>() {
		public void handle(ActionEvent t) {
		    box.setVisible(false);
		}
	    });
        menuFile.getItems().addAll(clear);

	MenuItem exit = new MenuItem("Exit");
	exit.setOnAction(new EventHandler<ActionEvent>() {
		public void handle(ActionEvent t) {
		    Platform.exit();
		}
	    });
        menuFile.getItems().addAll(exit);

        // --- Menu Edit
        Menu menuEdit = new Menu("Edit");

        // --- Menu View
        Menu menuView = new Menu("View");

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);

	box.getChildren().addAll(menuBar);
	return box;
    }

    public VBox buildFeedsTree() {
	// **** FEEDS TREE
	VBox vbox = new VBox();

	TreeItem<String> rootItem = new TreeItem<String> ("Inbox");
        rootItem.setExpanded(true);
        for (int i = 1; i < 6; i++) {
            TreeItem<String> item = new TreeItem<String> ("Message" + i);
            rootItem.getChildren().add(item);
        }
        TreeView<String> tree = new TreeView<String> (rootItem);
	vbox.getChildren().addAll(tree);
	return vbox;
    }

    private HBox buildCenterPane() {
	HBox hbox = new HBox();
    	// **** BUTTON

        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
		@Override
		    public void handle(ActionEvent event) {
		    try {
			// Backend.getInstance().setup();
			FeedReader.getInstance().loadFeeds();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    System.out.println("Hello World!");
		}
	    });

        hbox.getChildren().add(btn);
	return hbox;
    }

    @Override
    public void init() {
	// Backend.getInstance().setup();
	// FeedReader.getInstance().loadFeeds();
    }

    @Override
    public void start(Stage primaryStage) {
	BorderPane root = new BorderPane();
	root.setTop(buildMenuBar());
	root.setLeft(buildFeedsTree());
	root.setCenter(buildCenterPane());
	// root.setRight(addFlowPane());
        Scene scene = new Scene(root, 400, 350);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
	System.out.println("Graceful shutdown. Bye-bye");
    }
}
