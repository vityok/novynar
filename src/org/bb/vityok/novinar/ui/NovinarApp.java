package org.bb.vityok.novinar.ui;

import java.util.List;

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
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.bb.vityok.novinar.NewsItem;
import org.bb.vityok.novinar.OPMLManager;
import org.bb.vityok.novinar.db.Backend;
import org.bb.vityok.novinar.db.NewsItemDAO;
import org.bb.vityok.novinar.feed.FeedReader;


/** Primary UI controller that implements the glue binding all
 * components of the application together.
 */
public class NovinarApp extends Application {

    /** Table view with the current selection of news items. */
    private TableView<NewsItem> itemsTable = null;
    private TreeView<OPMLManager.Outline> channelsTree = null;
    private WebView itemView = null;
    private Label itemTitle = null;

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


    /** Encapsulates OPMLManager.Outline and serves as the view to
     * represent its data to the user.
     */
    public class OutlineTreeItem extends TreeItem<OPMLManager.Outline> {
	public OutlineTreeItem(OPMLManager.Outline ol) {
	    super(ol);
	    buildChildren();
	}

	public String toString() {
	    return ((OPMLManager.Outline) getValue()).toString();
	}

	private void buildChildren() {
	    OPMLManager.Outline ol = getValue();
	    if (ol != null && ol.hasChildren()) {
		List<OPMLManager.Outline> outlines = ol.getChildren();
		if (outlines != null) {
		    ObservableList<TreeItem<OPMLManager.Outline>> children = FXCollections.observableArrayList();
		    for (OPMLManager.Outline childOutline : outlines) {
			getChildren().add(new OutlineTreeItem(childOutline));
		    }
		}
	    }
	}
    } // end class OutlineTreeItem


    /** Builds the channels tree in the left side of the window.
     *
     * Uses OPMLManager.Outline class as a data model.
     */
    public VBox buildFeedsTree() {
	// **** FEEDS TREE
	VBox vbox = new VBox();

	OPMLManager.getInstance().loadConfig();

	OPMLManager.Outline root = OPMLManager.getInstance().getRootOutline();
	TreeItem<OPMLManager.Outline> rootItem = new OutlineTreeItem(root);
        rootItem.setExpanded(true);
        channelsTree = new TreeView<OPMLManager.Outline> (rootItem);
        channelsTree.getSelectionModel().selectedItemProperty().addListener( (obs, oldSelection, newSelection) -> {
                TreeItem<OPMLManager.Outline> selectedItem = (TreeItem<OPMLManager.Outline>) newSelection;
                OPMLManager.Outline outline = selectedItem.getValue();
                selectedOutline(outline);
            });
	VBox.setVgrow(channelsTree, Priority.ALWAYS);

	vbox.getChildren().addAll(channelsTree);

	return vbox;
    }


    /** Builds the items in the list of news items. */
    private VBox buildItemsTable() {
	itemsTable = new TableView<NewsItem>();

        itemsTable.setEditable(false);

        TableColumn<NewsItem,String> titleCol = new TableColumn<NewsItem,String>("Title");
        TableColumn<NewsItem,String> authorCol = new TableColumn<NewsItem,String>("Author");
        TableColumn<NewsItem,String> dateCol = new TableColumn<NewsItem,String>("Date");

	titleCol.setCellValueFactory(new PropertyValueFactory<NewsItem,String>("title"));
	authorCol.setCellValueFactory(new PropertyValueFactory<NewsItem,String>("author"));
	dateCol.setCellValueFactory(new PropertyValueFactory<NewsItem,String>("date"));

        itemsTable.getColumns().add(titleCol);
	itemsTable.getColumns().add(authorCol);
	itemsTable.getColumns().add(dateCol);

	itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		if (newSelection != null) {
		    System.out.println("selected: " + newSelection);
		    selectedNewsItem(newSelection);
		}
	    });
	VBox.setVgrow(itemsTable, Priority.ALWAYS);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().add(itemsTable);

	return vbox;
    }


    private void updateItemsTable() {
	try {
	    FeedReader.getInstance().loadFeeds();
	    NewsItemDAO dao = NewsItemDAO.getInstance();
	    ObservableList<NewsItem> items = FXCollections.observableArrayList(dao.getNewsItemByChannel(null));
	    itemsTable.setItems(items);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /** Select only items from the channels selected by the user in
     * the channels tree.
     */
    private void updateItemsTable(OPMLManager.Outline outline) {
	try {
	    NewsItemDAO dao = NewsItemDAO.getInstance();
	    ObservableList<NewsItem> items = FXCollections.observableArrayList(dao.getNewsItemByChannel(outline));
	    itemsTable.setItems(items);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private HBox buildCenterPane() {
	HBox hbox = new HBox();
        hbox.getChildren().addAll(buildItemsTable());
	return hbox;
    }

    private VBox buildContentPane() {
	VBox vbox = new VBox();
	itemTitle = new Label("");
    	// **** BUTTON
	Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
		    try {
			updateItemsTable();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    System.out.println("Hello World!");
		}
	    });

	itemView = new WebView();
	vbox.getChildren().addAll(itemTitle, itemView, btn);
	return vbox;
    }


    /** Status bar in the bottom of the main window. */
    private HBox buildStatusBar() {
        HBox statusbar = new HBox();
        return statusbar;
    }


    /** Handle selected outline in the channels tree. Update the list
     * of news items to contain only those items associated with this
     * outline and its children (when there are any).
     */
    private void selectedOutline(OPMLManager.Outline outline) {
        System.out.println("selected tree item, url=" + outline.getUrl()
                           + " title=" + outline.getTitle()
                           + " id=" + outline.getId());
        updateItemsTable(outline);
    }

    /** Handle news item selection in the list of the news items. */
    private void selectedNewsItem(NewsItem item) {
	if (item == null) {
	    itemView.getEngine().loadContent("<h1>Title</h1>");
	} else {
	    itemView.getEngine().loadContent(item.getDescription());
	    itemTitle.setText(item.getTitle());
	}
    }


    // LIFE CYCLE OF THE APPLICATION

    @Override
    public void init() {
	try {
	    Backend.getInstance().setup();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	// FeedReader.getInstance().loadFeeds();
    }

    @Override
    public void start(Stage primaryStage) {
	BorderPane root = new BorderPane();
	root.setTop(buildMenuBar());
	root.setLeft(buildFeedsTree());
	root.setRight(buildContentPane());
	root.setCenter(buildCenterPane());
        root.setBottom(buildStatusBar());
	// populate items table with the items
	updateItemsTable();
	selectedNewsItem(null);
	// root.setRight(addFlowPane());
        Scene scene = new Scene(root);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
	System.out.println("Graceful shutdown. Bye-bye");
	OPMLManager.getInstance().storeConfig();
	Backend.getInstance().close();
    }
}
