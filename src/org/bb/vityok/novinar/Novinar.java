package org.bb.vityok.novinar;

import java.io.File;

import java.util.List;
import java.util.LinkedList;

import org.bb.vityok.novinar.db.Backend;
import org.bb.vityok.novinar.db.NewsItemDAO;

import org.bb.vityok.novinar.feed.FeedReader;

/** Encapsulates feed management and news items management operations
 * into a single logical unit.
 */
public class Novinar
{
    private OPMLManager oman;
    private Backend dbend;
    private NewsItemDAO niDAO;
    private FeedReader reader;

    public Novinar() {
        this(OPMLManager.DEFAULT_OPML_FILE,
             Backend.DEFAULT_DB_NAME);
    }

    public Novinar(File opmlFile, String dbName) {
        oman = new OPMLManager(opmlFile);
        dbend = new Backend(dbName);
        niDAO = new NewsItemDAO(dbend);
        reader = new FeedReader(this);
    }

    public void setup ()
        throws Exception
    {
        dbend.setup();
    }

    public void close ()
        throws Exception
    {
        System.out.println("Novinar core shutting down");
        dbend.close();
    }

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

    public void loadFeeds()
        throws Exception
    {
        reader.loadFeeds();
    }

    public void loadFeed(Channel chan)
        throws Exception
    {
        reader.loadFeed(chan);
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
}
