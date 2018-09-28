package org.bb.vityok.novinar.ui;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Optional;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;

import javafx.css.PseudoClass;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Insets;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.Group;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import org.bb.vityok.novinar.core.Channel;
import org.bb.vityok.novinar.core.NewsItem;
import org.bb.vityok.novinar.core.Novinar;
import org.bb.vityok.novinar.core.Outline;
import org.bb.vityok.novinar.core.UpdatePeriod;


/** Primary UI controller.
 *
 * Relies on the Novinar class for business logic.
 */
public class NovinarApp extends Application
{
    private Novinar novinar;

    private Scene primaryScene;

    /** Table view with the current selection of news items. */
    private TableView<NewsItem> itemsTable = null;
    private TreeView<Outline> channelsTree = null;
    private WebView itemView = null;
    private Label itemTitle = null;
    private Label itemAuthor = null;
    private Label itemTimestamp = null;
    // contains URL of the currently viewed item
    private TextField itemLink = null;
    // The "visit" link next to the link text
    private Hyperlink itemLinkLink = null;

    private static Logger logger = Logger.getLogger("org.bb.vityok.novinar.ui");


    public static void start(String[] args) {
        launch(args);
    }


    private HBox buildMenuBar() {
	HBox box = new HBox();
	// **** MENU BAR
	MenuBar menuBar = new MenuBar();

        // --- Menu File
        Menu menuFile = new Menu("_File");
	menuFile.setAccelerator(KeyCombination.keyCombination("ALT+F"));

	MenuItem info = new MenuItem("Info...");
	info.setOnAction((ActionEvent evt) -> showInfoDialog());
        menuFile.getItems().addAll(info);

	MenuItem refresh = new MenuItem("Refresh feeds");
	refresh.setOnAction((ActionEvent evt) -> updateItemsTable());
        menuFile.getItems().addAll(refresh);

	MenuItem exit = new MenuItem("Exit");
	exit.setOnAction((ActionEvent evt) -> Platform.exit());
        menuFile.getItems().addAll(exit);

        // --- Menu Edit
        Menu menuEdit = new Menu("Edit");

        // --- Menu View
        Menu menuView = new Menu("View");

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);

	box.getChildren().addAll(menuBar);
	HBox.setHgrow(menuBar, Priority.ALWAYS);
	return box;
    }

    private final Image imageProblem = new Image(getClass().getResourceAsStream("/icons/1x/twotone_report_problem_black_18dp.png"));
    private final Image imageFeed = new Image(getClass().getResourceAsStream("/icons/1x/twotone_rss_feed_black_18dp.png"));

    /** Encapsulates Outline and serves as the view to
     * represent its data to the user.
     */
    public class OutlineTreeItem extends TreeItem<Outline> {
	private final ImageView imvProblem = new ImageView(imageProblem);
	private final ImageView imvFeed = new ImageView(imageFeed);
	
	public OutlineTreeItem(Outline ol) {	    
	    super(ol);
	    rebuildChildren();
	    if (ol.isChannel()) {
		// register for various status updates
		Channel chan = ol.getChannel();
		final ImageView defaultIcon = chan.hasProblems() ? imvProblem : imvFeed;
		setGraphic(defaultIcon);
		chan.hasProblemsProperty().addListener((ObservableValue<? extends Boolean> ov,
			Boolean oldVal, Boolean newVal) ->
		{
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
                outlines.forEach((childOutline) -> {
                        children.add(new OutlineTreeItem(childOutline));
                    });
                return children;
	    }
            return FXCollections.emptyObservableList();
	}

        public void rebuildChildren() {
	    getChildren().setAll(buildChildren());
        }
    } // end class OutlineTreeItem


    public void feedsTreeCtxProperties() {
        Outline selectedOl = channelsTree.getSelectionModel().getSelectedItem().getValue();
        System.out.println("Menu Item Clicked!" + selectedOl);

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(new Group(new Text(25, 25, "Hello World!")));
        dialog.setScene(scene);
        dialog.show();
    }

    /** Refresh the selected feed or feeds in the selected folder. */
    public void feedsTreeCtxRefresh() {
        Outline ol = channelsTree.getSelectionModel().getSelectedItem().getValue();
        try {
            novinar.loadFeedsBg(ol);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed to refresh new feed", e);
        }
    }

    /** Builds the channels tree in the left side of the window.
     *
     * Uses Outline class as a data model.
     */
    public VBox buildFeedsTree() {
	// **** FEEDS TREE
	final VBox vbox = new VBox();

	novinar.loadConfig();
        // immediately store config in case if there were new channels
        // with autogenerated ids, to preserve integrity with the db
	novinar.storeConfig();

	Outline root = novinar.getRootOutline();
	TreeItem<Outline> rootItem = new OutlineTreeItem(root);
        rootItem.setExpanded(true);
        channelsTree = new TreeView<> (rootItem);
        channelsTree
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        TreeItem<Outline> selectedItem = newSelection;
                        Outline outline = selectedItem.getValue();
                        selectedOutline(outline);
                    }
                });

        // instantiate the context menu for the channels TreeView. It
        // appears that clicking with the secondary mouse button on an
        // item makes it "selected", and therefore accessible to the
        // menu item handler code
        MenuItem ctxRefresh = new MenuItem("Refresh");
        ctxRefresh.setOnAction(ae -> feedsTreeCtxRefresh());

        MenuItem ctxNewFolder = new MenuItem("New folder...");
        ctxNewFolder.setOnAction(ae -> feedsTreeCtxProperties());

        MenuItem ctxNewChannel = new MenuItem("New channel...");
        ctxNewChannel.setOnAction(ae -> showAddChannelDialog());

        MenuItem ctxRemove = new MenuItem("Remove...");
        ctxRemove.setOnAction(ae -> feedsTreeCtxProperties());

        MenuItem ctxProperties = new MenuItem("Properties...");
        ctxProperties.setOnAction(ae -> showEditChannelDialog());

        ContextMenu channelsContextMenu = new ContextMenu();
        channelsContextMenu.getItems().addAll(ctxRefresh,
                                              new SeparatorMenuItem(),
                                              ctxNewFolder,
                                              ctxNewChannel,
                                              new SeparatorMenuItem(),
                                              ctxRemove,
                                              new SeparatorMenuItem(),
                                              ctxProperties);
        channelsTree.setContextMenu(channelsContextMenu);

	VBox.setVgrow(channelsTree, Priority.ALWAYS);

	// New folder
        final Button btnFolder = new Button();
	btnFolder.setTooltip(new Tooltip("Create new folder"));
	Image imageFolder = new Image(getClass().getResourceAsStream("/icons/1x/twotone_create_new_folder_black_18dp.png"));
	btnFolder.setGraphic(new ImageView(imageFolder));
        btnFolder.setOnAction((ActionEvent e) -> showAddFolderDialog());

        // add new channel
        final Button btnChannel = new Button();
	btnChannel.setTooltip(new Tooltip("Add new channel"));
	Image imageChannel = new Image(getClass().getResourceAsStream("/icons/1x/twotone_rss_feed_black_18dp.png"));
	btnChannel.setGraphic(new ImageView(imageChannel));
        btnChannel.setOnAction((ActionEvent e) -> showAddChannelDialog());

	// Properties
        final Button btnProperties = new Button();
	btnProperties.setTooltip(new Tooltip("Channel properties"));
	Image imageProperties = new Image(getClass().getResourceAsStream("/icons/1x/twotone_edit_black_18dp.png"));
	btnProperties.setGraphic(new ImageView(imageProperties));
        btnProperties.setOnAction((ActionEvent e) -> showEditChannelDialog());

	// Remove
        final Button btnRemove = new Button();
	btnRemove.setTooltip(new Tooltip("Remove outline"));
	Image imageRemove = new Image(getClass().getResourceAsStream("/icons/1x/twotone_delete_forever_black_18dp.png"));
	btnRemove.setGraphic(new ImageView(imageRemove));
        btnRemove.setOnAction((ActionEvent e) -> showRemoveOutlineDialog());

        ToolBar tbChannels = new ToolBar(
                                         btnFolder,
                                         btnChannel,
                                         btnProperties,
                                         btnRemove
                                         );

	vbox.getChildren().addAll(tbChannels, channelsTree);

	return vbox;
    } // end buildFeedsTree


    /** Builds the items in the list of news items. */
    private VBox buildItemsTable() {
	itemsTable = new TableView<>();

        itemsTable.setEditable(false);
        itemsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // from: https://stackoverflow.com/a/27016798/ in order to
        // mark unread news item rows with the bold font. This is
        // accomplished by introducing two pseudo classes and
        // activating them whenever row contents (item) or its isRead
        // property changes
        PseudoClass clsRead = PseudoClass.getPseudoClass("readItem");
        PseudoClass clsUnread = PseudoClass.getPseudoClass("unreadItem");

        itemsTable.setRowFactory(tv -> {
                TableRow<NewsItem> row = new TableRow<>();
                ChangeListener<Boolean> readListener = (obs, oldRead, newRead) -> {
                    boolean gotRead = newRead.booleanValue();
                    row.pseudoClassStateChanged(clsRead, gotRead);
                    row.pseudoClassStateChanged(clsUnread, !gotRead);

                };
                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                        if (oldItem != null) {
                            oldItem.isReadProperty().removeListener(readListener);
                        }
                        if (newItem != null) {
                            newItem.isReadProperty().addListener(readListener);
                            row.pseudoClassStateChanged(clsRead, newItem.getIsRead());
                            row.pseudoClassStateChanged(clsUnread, !newItem.getIsRead());
                        } else {
                            row.pseudoClassStateChanged(clsRead, false);
                            row.pseudoClassStateChanged(clsUnread, false);
                        }
                    });
                return row ;
            });

        // create three columns by default
        TableColumn<NewsItem,String> titleCol = new TableColumn<>("Title");
        titleCol.setPrefWidth(350);
        titleCol.setSortable(true);

        TableColumn<NewsItem,String> authorCol = new TableColumn<>("Author");
        authorCol.setPrefWidth(30);

        TableColumn<NewsItem,String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(100);
        dateCol.setSortable(true);

	titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
	authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
	dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        itemsTable.getColumns().add(titleCol);
	itemsTable.getColumns().add(authorCol);
	itemsTable.getColumns().add(dateCol);

        // handle selected news items
	itemsTable
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldSelection, newSelection) -> {
		if (newSelection != null) {
		    logger.fine("selected: " + newSelection);
		    selectedNewsItem(newSelection);
		}
	    });

        itemsTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(final KeyEvent keyEvent) {
                    final NewsItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();

                    if (selectedItem != null) {
                        // handle DEL key press, todo: handle multiple selected items
                        if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                            itemsTable.getItems().remove(itemsTable.getItems().indexOf(selectedItem));
                        }
                    }
                }
            } );

	VBox.setVgrow(itemsTable, Priority.ALWAYS);

	// next item
        Button btnNext = new Button();
	btnNext.setTooltip(new Tooltip("Step to the next item"));
	Image imageNext = new Image(getClass().getResourceAsStream("/icons/1x/twotone_arrow_drop_down_black_18dp.png"));
	btnNext.setGraphic(new ImageView(imageNext));
        btnNext.setOnAction((ActionEvent e) -> {
		itemsTable.getSelectionModel().selectNext();
	    });

	// previous item
        Button btnPrev = new Button();
	btnPrev.setTooltip(new Tooltip("Step to the previous item"));
	Image imagePrev = new Image(getClass().getResourceAsStream("/icons/1x/twotone_arrow_drop_up_black_18dp.png"));
	btnPrev.setGraphic(new ImageView(imagePrev));
        btnPrev.setOnAction((ActionEvent e) -> {
		itemsTable.getSelectionModel().selectPrevious();
	    });

	// share item
        Button btnShare = new Button();
	btnShare.setTooltip(new Tooltip("Generate wiki ref text"));
	Image imageShare = new Image(getClass().getResourceAsStream("/icons/1x/twotone_share_black_18dp.png"));
	btnShare.setGraphic(new ImageView(imageShare));
        btnShare.setOnAction((ActionEvent e) -> showShareDialog() );

	// remove item
        Button btnRemove = new Button();
	btnRemove.setTooltip(new Tooltip("Remove item"));
	Image imageRemove = new Image(getClass().getResourceAsStream("/icons/1x/twotone_delete_forever_black_18dp.png"));
	btnRemove.setGraphic(new ImageView(imageRemove));
        btnRemove.setOnAction((ActionEvent e) -> {
		final NewsItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
		if (selectedItem != null) {
		    itemsTable.getItems().remove(itemsTable.getItems().indexOf(selectedItem));
		}
	    });

        ToolBar tbItems = new ToolBar(btnNext, btnPrev, btnShare, btnRemove);

        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(0, 3, 0, 3));
        vbox.getChildren().addAll(tbItems, itemsTable);

	return vbox;
    } // end buildItemsTable

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


    /** Show dialog with the new channel properties. */
    private void showAddChannelDialog() {
        final ChannelPropertiesDialog dialog = new ChannelPropertiesDialog("Add channel")
            {
                @Override
                public void handleResponse(ButtonType response) {
                    if (response == ButtonType.OK) {
                        // add a new channel under the currently selected outline
                        OutlineTreeItem parent = (OutlineTreeItem) channelsTree.getSelectionModel().getSelectedItem();
                        Outline ol = novinar.appendChannel(parent.getValue(), fldUrl.getText(), fldTitle.getText());
                        ol.setIgnoreOnBoot(cbIgnoreOnBoot.isSelected());
                        ol.setUpdatePeriod(cbxUpdatePeriod.getValue());
                        parent.rebuildChildren();
                        try {
                            novinar.loadFeeds(ol);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "failed to refresh new feed", e);
                        }
                    }

                }
            };

        dialog.execute();
    }

    /** Edit properties of an existing channel. */
    public void showEditChannelDialog() {
        final TreeItem<Outline> item = channelsTree.getSelectionModel().getSelectedItem();
        final Outline ol = item.getValue();
        final Channel chan = ol.getChannel();

        final ChannelPropertiesDialog dialog = new ChannelPropertiesDialog("Edit channel")
            {
                @Override
                public void init() {
                    fldUrl.setText(chan.getLink());
                    fldTitle.setText(chan.getTitle());
                    cbIgnoreOnBoot.setSelected(ol.getIgnoreOnBoot());
                    cbxUpdatePeriod.setValue(ol.getUpdatePeriod());
                    if (chan.hasProblems()) {
                    	txtProblems.setText("Problems: " + chan.getProblems());
                    } else {
                    	txtProblems.setText("No problems detected so far");
                    }
                }

                @Override
                public void handleResponse(ButtonType response) {
                    if (response == ButtonType.OK) {
                        chan.setLink(fldUrl.getText());
                        chan.setTitle(fldTitle.getText());

                        ol.setIgnoreOnBoot(cbIgnoreOnBoot.isSelected());
                        ol.setProperty(Outline.P_UPDATE_PERIOD, cbxUpdatePeriod.getValue().getCode());

                        try {
                            novinar.loadFeeds(ol);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "failed to refresh the feed", e);
                        }
                    }

                }
            };

        dialog.execute();

    }

    private void showRemoveOutlineDialog() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Remove entry");
        dialog.setHeaderText("This will permanently remove information. Are you sure?");
        // ButtonType.OK is there by default
        // dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // add a new channel under the currently selected outline
                    TreeItem<Outline> item = channelsTree.getSelectionModel().getSelectedItem();
                    OutlineTreeItem parent = (OutlineTreeItem)item.getParent();
                    Outline ol = item.getValue();
                    novinar.removeOPMLEntry(ol);
                    parent.rebuildChildren();
                }
            });
    }

    private void showAddFolderDialog() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Add folder");
        dialog.setHeaderText("Folder name");
        // ButtonType.OK is there by default
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField fldName = new TextField();
        fldName.setPromptText("Enter name");

        grid.add(new Label("Folder name:"), 0, 0);
        grid.add(fldName, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the URL field by default.
        Platform.runLater(() -> fldName.requestFocus());

        dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // add a new channel under the currently selected outline
                    OutlineTreeItem parent = (OutlineTreeItem) channelsTree.getSelectionModel().getSelectedItem();
                    Outline ol = novinar.appendFolder(parent.getValue(), fldName.getText());
                    parent.rebuildChildren();
                }
            });
    }

    private void showInfoDialog() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Information");
        dialog.setHeaderText("Novinar, a news reader and aggegator");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Channels:"), 0, 0);
        grid.add(new Label(Integer.toString(novinar.getChannels().size())), 1, 0);

        grid.add(new Label("Total news items:"), 0, 1);
        grid.add(new Label(Integer.toString(novinar.getTotalNewsItemsCount())), 1, 1);

        grid.add(new Label("Unread:"), 0, 2);
        grid.add(new Label(Integer.toString(novinar.getUnreadNewsItemsCount())), 1, 2);

        grid.add(new Label("To be removed:"), 0, 3);
        grid.add(new Label(Integer.toString(novinar.getRemovedNewsItemsCount())), 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait();
    }

    /**
     * Pop-up dialog with the wikipedia cite news/web autogenerated
     * template.
     */
    class ShareNewsItemDialog extends Alert
    {
        final GridPane grid = new GridPane();
	final TextArea ta = new TextArea();
	final VBox pane = new VBox();
	final CheckBox cbWrap = new CheckBox();
	final CheckBox cbName = new CheckBox();
	final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	    .withZone(ZoneId.systemDefault());;

	public ShareNewsItemDialog(String title) {
	    super(Alert.AlertType.INFORMATION);
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

	    pane.getChildren().addAll(ta, grid);
            getDialogPane().setContent(pane);
	    init();
	}

	/** Actually generates the reference text and inserts it into
	 * the textarea. */
	public void genText() {
	    final NewsItem item = itemsTable.getSelectionModel().getSelectedItem();
	    final String strDate = format.format(item.getDateCalendar());
	    final String lw = cbWrap.isSelected() ? "\n" : "";
	    final String strPublisher = novinar.getChannelById(item.getChannelId()).getTitle();
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
	    if (creator != null && !creator.isEmpty()) {
		author = " | author = " + creator;
	    }

	    // produce ref text
	    String wikiRef = "<ref" + name + ">{{cite web" + lw
		+ " | url = " + item.getLink() + lw
		+ " | title = " + item.getTitle() + lw
		+ author + lw
		+ " | publisher = " + strPublisher + lw
		+ " | date = " + strDate + lw
		+ " }}</ref>";
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

    /** Shows dialog with the text area with generated references for
     * Wikipedia. */
    public void showShareDialog() {
	final ShareNewsItemDialog dialog = new ShareNewsItemDialog("Share item");
	dialog.execute();
    }

    private void updateItemsTable() {
        try {
            novinar.loadFeeds();
            updateItemsTable(novinar.getRootOutline());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Select only items from the channels selected by the user in
     * the channels tree.
     */
    private void updateItemsTable(Outline ol) {
        try {
            ObservableList<NewsItem> items = FXCollections.observableArrayList(novinar.getNewsItemsFor(ol));
            itemsTable.setItems(items);

            // track changes to the list of news items, namely, when a
            // user "removes" selected items
            items.addListener(new ListChangeListener<NewsItem>() {
                    @Override
                    public void onChanged(ListChangeListener.Change<? extends NewsItem> c) {
                        while (c.next()) {
                            if (c.wasRemoved()) {
                                for (NewsItem item : c.getRemoved()) {
                                    try {
                                        novinar.removeNewsItem(item);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Node buildCenterPane() {
        SplitPane centerPane = new SplitPane();

        centerPane.getItems().addAll(buildFeedsTree(),
				     buildItemsTable(),
				     buildContentPane());
        centerPane.setDividerPositions(0.12f, 0.55f);
	return centerPane;
    }


    private VBox buildContentPane() {
	VBox vbox = new VBox();
	itemTitle = new Label("");
        itemTitle.setFont(Font.font("Helvetica", FontWeight.BOLD, 18));
        itemTitle.setWrapText(true);

        HBox itemMetaBox = new HBox();
        itemAuthor = new Label("Creator: ");
        itemTimestamp = new Label("Timestamp: ");
        itemMetaBox.getChildren().addAll(itemAuthor, itemTimestamp);

	itemView = new WebView();
        WebEngine webEngine = itemView.getEngine();
        webEngine.setJavaScriptEnabled(false);
        webEngine.setUserStyleSheetLocation(getClass().getResource("style.css").toString());
        VBox.setVgrow(itemView, Priority.ALWAYS);

	HBox itemLinksBox = new HBox();
        itemLink = new TextField("");
        itemLink.setFont(Font.font("Helvetica", FontWeight.LIGHT, 12));
	// select all itemLink text upon text field activation (either
	// with the mouse click or keyboard navigation)
	itemLink.focusedProperty().addListener(new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue ov, Boolean t, Boolean t1) {

		    Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
				if (itemLink.isFocused() && !itemLink.getText().isEmpty()) {
				    itemLink.selectAll();
				}
			    }
			});
		}
	    });

	itemLinkLink = new Hyperlink();
	itemLinkLink.setText("Visit");
	itemLinkLink.setOnAction((ActionEvent evt) -> {
		itemView.getEngine().load(itemLink.getText());
	    });
	itemLinksBox.getChildren().addAll(itemLink, itemLinkLink);
	HBox.setHgrow(itemLink, Priority.ALWAYS);

	vbox.getChildren().addAll(itemTitle, itemMetaBox, itemView, itemLinksBox);
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
    private void selectedOutline(Outline ol) {
        logger.fine("selected tree item, title=" + ol.getTitle());
        updateItemsTable(ol);
    }

    /** Handle news item selection in the list of the news items.
     *
     * That is, do all the necessary stuff after the user clicks on
     * the item in the list of news items.
     */
    private void selectedNewsItem(NewsItem item) {
        try {
            if (item == null) {
                itemView.getEngine().loadContent("<em></em>");
            } else {
                itemView.getEngine().loadContent(item.getDescription());
                itemTitle.setText(item.getTitle());
                itemAuthor.setText("Creator: " + item.getCreator());
                itemTimestamp.setText(" Timestamp: " + item.getDate());
                itemLink.setText(item.getLink());
                novinar.markNewsItemRead(item, true);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed to load news item", e);
        }
    }


    // LIFE CYCLE OF THE APPLICATION

    @Override
    public void init() {
	try {
            novinar = new Novinar();
	    novinar.setup();
	} catch (Exception e) {
            logger.log(Level.SEVERE, "failure during initialization", e);
	}
    }

    @Override
    public void start(Stage primaryStage) {
	BorderPane root = new BorderPane();
	root.setTop(buildMenuBar());
	root.setCenter(buildCenterPane());
        root.setBottom(buildStatusBar());
	// populate items table with the items
	updateItemsTable();
	selectedNewsItem(null);
        primaryScene = new Scene(root);
        primaryScene.getStylesheets().add(getClass().getResource("novinar.css").toExternalForm());

        primaryStage.setTitle("Novinar news reader");
        primaryStage.setScene(primaryScene);

        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
	logger.info("Graceful shutdown. Bye-bye");
        try {
            novinar.storeConfig();
            novinar.close();
        } catch (Exception e) {
            logger.severe("Graceful shutdown. failed: " + e);
            e.printStackTrace();
        }
    }
}
