package org.bb.vityok.novinator.ui;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Insets;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.bb.vityok.novinator.NewsItem;
import org.bb.vityok.novinator.db.Backend;
import org.bb.vityok.novinator.feed.FeedReader;


/** Primary UI controller implementing the glue binding all components
 * of the application together.
 */
public class NovinatorApp extends Application {

    /** Table view with the current selection of news items. */
    private TableView itemsTable = null;

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


    private VBox buildItemsTable() {
	itemsTable = new TableView();

        itemsTable.setEditable(false);

        TableColumn titleCol = new TableColumn("Title");
        TableColumn authorCol = new TableColumn("Author");
        TableColumn dateCol = new TableColumn("Date");

	titleCol.setCellValueFactory(new PropertyValueFactory<NewsItem,String>("title"));
	authorCol.setCellValueFactory(new PropertyValueFactory<NewsItem,String>("author"));
	dateCol.setCellValueFactory(new PropertyValueFactory<NewsItem,String>("date"));

        itemsTable.getColumns().add(titleCol);
	itemsTable.getColumns().add(authorCol);
	itemsTable.getColumns().add(dateCol);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().add(itemsTable);

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

        hbox.getChildren().addAll(buildItemsTable(), btn);
	return hbox;
    }

    // LIFE CYCLE OF THE APPLICATION

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
	Backend.getInstance().close();
    }
}
