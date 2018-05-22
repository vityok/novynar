package org.bb.vityok.novinar;

import java.io.IOException;
import java.io.File;

import java.text.SimpleDateFormat;

import java.time.Instant;

import java.util.List;
import java.util.LinkedList;

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
    public static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private OPMLManager oman;
    private Backend dbend;
    private NewsItemDAO niDAO;
    private FeedReader reader;

    private static Logger logger = Logger.getLogger(Novinar.class.getName());

    public enum Status {
        READY, READING_FEEDS, STARTING
    }

    // todo: might be better to refactor into a sender-listeners
    // scheme
    private Status status = Status.STARTING;


    /** Constructs a new Novinar instance using default data location
     * paths or those provided in the system environment.
     */
    public Novinar() {
        this(
             System.getProperty("org.bb.vityok.novinar.opml_file",
                                OPMLManager.DEFAULT_OPML_FILE_NAME),
             System.getProperty("org.bb.vityok.novinar.db_dir",
                                Backend.DEFAULT_DB_NAME));
    }

    public Novinar(String opmlFile, String dbName) {
        try {
            FileHandler fh = new FileHandler("novinar.log");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.FINE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("loading Novinar with: opml=" + opmlFile + "; db=" + dbName);
        oman = new OPMLManager(opmlFile);
        dbend = new Backend(dbName);
        niDAO = new NewsItemDAO(dbend);
        reader = new FeedReader(this);
    }

    public void setup ()
        throws Exception
    {
        // db backend is setup in its constructor

        // dbend.setup();
    }

    /** Shutdown Novinar core.
     *
     * Close and free database resources, stop running threads.
     */
    public void close ()
        throws Exception
    {
        getLogger().severe("Novinar core shutting down");
        reader.interrupt();
        dbend.close();
        getLogger().severe("Novinar core shut down");
    }

    /** Returns all channels defined in the OPML file. */
    public List<Channel> getChannels() {
        return oman.getChannels();
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

        for (Outline child : children) {
            Channel childChannel = child.getChannel();
            if (childChannel != null) {
                channels.add(childChannel);
            } else if (child.hasChildren()) {
                channels.addAll(getChannelsUnder(child));
            }
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
        niDAO.removeNewsItem(item);
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
        if (!reader.isAlive()) {
            reader.start();
        }
    }

    public void loadFeed(Channel chan)
        throws Exception
    {
        reader.loadFeed(chan);
    }

    public void loadFeeds(Outline ol)
        throws Exception
    {
        reader.loadFeed(ol.getChannel());
    }

    public void loadConfig() {
        oman.loadConfig();
    }
    public void storeConfig() {
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
