package org.bb.vityok.novinar;

import java.io.File;

import java.util.List;

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

    public List<Channel> getChannels() { return oman.getChannels(); }
    /** Returns the list of channels located under the given "folder"
     * outline.
     */
    public List<Channel> getChannelsUnder(Outline ol) {
        // todo: traverse the tree and get all channels under this
        // outline
        return getChannels();
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

    public Outline getRootOutline () {
        return oman.getRootOutline();
    }
}
