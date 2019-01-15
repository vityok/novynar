package org.bb.vityok.novinar.core;

import java.io.IOException;
import java.io.File;

import java.text.SimpleDateFormat;

import java.time.Instant;

import java.util.List;
import java.util.LinkedList;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.bb.vityok.novinar.db.Backend;
import org.bb.vityok.novinar.db.NewsItemDAO;

import org.bb.vityok.novinar.feed.FeedReader;

/** Encapsulates feed management and news items management operations
 * into a single logical unit.
 *
 * User interface should use this class to operate newsfeeds, retrieve
 * and store data.
 */
public class Novinar
{

    /** System property defining location of the news items
     * database. */
    public static final String PROP_DB_DIR = "org.bb.vityok.novinar.db_dir";

    /** System property defining location of the OPML file with the
     * feeds directory. */
    public static final String PROP_OPML_FILE = "org.bb.vityok.novinar.opml_file";

    private static final Logger logger = Logger.getLogger(Novinar.class.getName());

    private OPMLManager oman;
    private Backend dbend;
    private NewsItemDAO niDAO;
    private FeedReader reader;

    // used to run jobs/tasks in background, for example the periodic
    // channel updater thread or single channel refresh tasks. At the
    // momen at leas one task runs forever: the background refresh
    // thread
    private ExecutorService taskRunner;

    public enum Status {
        READY, READING_FEEDS, STARTING
    }

    // todo: might be better to refactor into a sender-listeners
    // scheme
    private Status status = Status.STARTING;


    /** Constructs a new Novinar instance using default data location
     * paths or those provided in the system environment.
     */
    public Novinar()
    {
        this(
             System.getProperty(PROP_OPML_FILE,
                                OPMLManager.DEFAULT_OPML_FILE_NAME),
             System.getProperty(PROP_DB_DIR,
                                Backend.DEFAULT_DB_NAME));
    }

    public Novinar(String opmlFile, String dbName)
    {
        try {
            FileHandler fh = new FileHandler("novinar.log");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.FINE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.severe("loading Novinar with: opml=" + opmlFile + "; db=" + dbName);
        dbend = new Backend(dbName);
        niDAO = dbend.getNewsItemDAO();
        oman = new OPMLManager(opmlFile);
        reader = new FeedReader(this);

        taskRunner = Executors.newFixedThreadPool(2);
    }

    public void setup ()
        throws Exception
    {
	// start the background channel refresh thread
        reader.start();
        // taskRunner.submit();
    }

    /** Shutdown Novinar core.
     *
     * Close and free database resources, stop running threads.
     */
    public void close ()
        throws Exception
    {
        getLogger().severe("Novinar core shutting down");
        reader.close();
        dbend.close();

        try {
            taskRunner.shutdown();
            taskRunner.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            getLogger().severe("tasks interrupted");
        } finally {
            if (!taskRunner.isTerminated()) {
                getLogger().severe("cancel non-finished tasks");
            }
            taskRunner.shutdownNow();
        }

        getLogger().severe("Novinar core shut down");
    }

    /** Returns all channels defined in the OPML file. */
    public List<Channel> getChannels() {
        return oman.getChannels();
    }

    /** Returns the channel with the given id.
     *
     * @return Channel with the given id or <tt>null</tt> if no such
     * channel could be found.
     */
    public Channel getChannelById(int channelId) {
	return oman.getChannelById(channelId);
    }


    /** Returns the list of channels located under the given "folder"
     * outline.
     *
     * Runs a recursive depth-first traversal of the OPML tree
     * starting from the given outline node.
     *
     * It doesn't guard against return stack exhaustion as it is
     * expected that the trees it will run on will not be very
     * deep/high. Also it is assumed that it will be run on trees,
     * ie. graphs without cycles.
     */
    public List<Channel> getChannelsUnder(Outline ol) {
        List<Channel> channels = new LinkedList<>();
        List<Outline> children = ol.getChildren();

        if (ol.getChannel() == null) {
            // we do have a folder, descend
            for (Outline child : children) {
                Channel childChannel = child.getChannel();
                if (childChannel != null) {
                    channels.add(childChannel);
                } else if (child.hasChildren()) {
                    channels.addAll(getChannelsUnder(child));
                }
            }
        } else {
            // we were given a leaf-channel, nowhere to descend node
            channels.add(ol.getChannel());
        }
        return channels;
    }

    public int getChannelCounter () { return oman.getChannelCounter(); }

    /** Removes channel information from the OPML directory and all
     * its news items from the database.
     */
    public void removeChannel(Channel chan) {
    }

    /** Marks the given news item as removed. The item is not removed
     * from the database as it could potentially be restored upon
     * channel refresh, but is marked as such.
     */
    public void removeNewsItem(NewsItem item)
        throws Exception
    {
        niDAO.trashNewsItem(item);
    }

    public List<NewsItem> getNewsItems()
        throws Exception
    {
        return niDAO.getNewsItemByChannels(null);
    }


    /** Returns news items for the given outline, whether it is a
     * channel or a folder outline.
     *
     * When ol is a folder outline, returns news items for all
     * subchannels of this folder.
     *
     * @return news items for the channels under this outline.
     */
    public List<NewsItem> getNewsItemsFor(Outline ol)
        throws Exception
    {
        Channel chan = ol.getChannel();
        if (chan != null) {
            return getNewsItemsFor(chan);
        } else {
            return niDAO.getNewsItemByChannels(getChannelsUnder(ol));
        }
    }

    /**
     * Returns all news items that were moved to the trash bin by the
     * user (deleted).
     */
    public List<NewsItem> getNewsItemsInTrash()
        throws Exception
    {
	return niDAO.getNewsItemsInTrash();
    }


    public List<NewsItem> getNewsItemsFor(Channel chan)
        throws Exception
    {
        return niDAO.getNewsItemByChannel(chan);
    }

    public void insertOrUpdateItem(Channel chan, NewsItem newsItem)
        throws Exception
    {
        niDAO.insertOrUpdateItem(chan, newsItem);
    }

    public int getTotalNewsItemsCount() { return niDAO.getTotalNewsItemsCount(); }
    public int getUnreadNewsItemsCount() { return niDAO.getUnreadNewsItemsCount(); }
    public int getRemovedNewsItemsCount() { return niDAO.getRemovedNewsItemsCount(); }
    public int getDbSchemaVersion() { return dbend.getSchemaVersion(); }


    public void markNewsItemRead(NewsItem item, boolean isRead)
        throws Exception
    {
        // do nothing if the old value is the same as new
        if (isRead ^ item.getIsRead()) {
            item.setIsRead(isRead);
            niDAO.markItemAsRead(item, isRead);
        }
    }

    public synchronized void loadFeeds()
        throws Exception
    {
        // todo: reader.start() to spawn a background thread once code
        // is ready. might be better to communicate with the reader
        // thread via a task queue
        // reader.loadFeeds();
        // if (!reader.isAlive()) {}
    }

    public void loadFeed(Channel chan)
        throws Exception
    {
        reader.submitLoadFeedTask(chan);
    }

    /** Loads feeds for the given channel outline or channels in the
     * folder.
     */
    public void loadFeeds(Outline ol)
        throws Exception
    {
        List<Channel> channels = getChannelsUnder(ol);
        for (Channel chan : channels) {
            loadFeed(chan);
        }
    }

    public void loadFeedsBg(final Outline ol)
        throws Exception
    {
        reader.loadFeeds();
    }

    public void loadConfig()
    {
        oman.loadConfig();
    }

    public void storeConfig()
    {
        oman.storeConfig();
    }

    /** Returns the root outline in the OPML tree.
     *
     * All other outlines are descendants of this one or of outlines
     * descending from it.
     */
    public Outline getRootOutline () {
        return oman.getRootOutline();
    }

    public static Logger getLogger() {
        return logger;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    /** Purge news items for this channel preceding the given
     * timestamp from the database.
     */
    public void cleanupChannel(Channel chan, Instant ts) {
        if (ts != null) {
            logger.info("cleanup for channel " + chan
                        + " for items before: " + ts);

            niDAO.cleanupChannel(chan, ts);
        }
    }

    public Outline appendChannel(Outline ol, String url, String title) {
        Outline newOl = oman.appendChannel(ol, url, title);
        return newOl;
    }

    public Outline appendFolder(Outline ol, String name) {
        Outline newOl = oman.appendFolder(ol, name);
        return newOl;
    }

    /** Deletes selected Outline and all its children from the OPML
     * tree and all news items from the database.
     */
    public void removeOPMLEntry(Outline ol) {
        if (ol.isFolder()) {
            // todo: this is a folder outline and it might require
            // some sort of a recursion
            getChannelsUnder(ol);
        } else {
            // this is just a single channel entry
            removeChannel(ol);
        }
    }

    public void removeChannel(Outline ol) {
        Channel channel = ol.getChannel();
        logger.info("removing channel: " + channel);
        oman.removeEntry(ol);
        niDAO.removeChannelItems(channel);
    }
}
