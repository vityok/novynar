package org.bb.vityok.novinar.db;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;

import java.time.Instant;

import java.util.List;
import java.util.LinkedList;

import java.util.logging.Level;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.bb.vityok.novinar.core.Channel;
import org.bb.vityok.novinar.core.NewsItem;


/** Implements database access operations for the NewsItem object.
 *
 * It is not an interface nor an abstract class but an ordinary class
 * because currently there is just one database backend (and no other
 * backends are planned) so additional abstraction is redundand.
 */
public class NewsItemDAO
{
    private Backend dbend;

    public NewsItemDAO(Backend dbend)
    {
        this.dbend = dbend;
    }

    public void insertOrUpdateItem(Channel chan, NewsItem item)
	throws Exception
    {
	Connection conn = dbend.getConnection();

        // truncate the description if it exceeds maximum possible description size
        String desc = item.getDescription();
        if (desc !=null && desc.length() >=Backend.DESCRIPTION_MAX_LENGTH) {
            item.setDescription(desc.substring(0, Backend.DESCRIPTION_MAX_LENGTH - 5 ));
        }

	// check if such item already exists in the database before
	// insertion, update the item if it is not removed otherwise
        String sqlSel = "SELECT link, news_item_id, is_removed, is_trash FROM news_item WHERE link=?";
	try (PreparedStatement cs = conn.prepareStatement(sqlSel)) {
            cs.setString(1, item.getLink());
            ResultSet rscs = cs.executeQuery();
            boolean alreadyExists = rscs.next();
            if (alreadyExists) {
                int newsItemId = rscs.getInt("news_item_id");
                boolean isRemoved = (rscs.getInt("is_removed") == 1);
                boolean isTrash = (rscs.getInt("is_trash") == 1);
                if (!(isRemoved || isTrash)) {
                    String sqlUp = "UPDATE news_item SET "
                        + " title=?, description=?, creator=?, date=?, subject=? "
                        + " WHERE news_item_id=?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlUp)) {
                        ps.setString(1, item.getTitle());
                        ps.setString(2, item.getDescription());
                        ps.setString(3, item.getCreator());
                        ps.setTimestamp(4, new Timestamp(item.getDateCalendar().toEpochMilli()));
                        ps.setString(5, item.getSubject());
                        ps.setInt(6, newsItemId);
                        ps.executeUpdate();
                        dbend.getLogger().fine("updated existing item: " + item.getTitle());
                    }
                }
            } else {
                String sqlIns = "INSERT INTO news_item(title, link, description, " +
                    " creator, date, subject, channel_id)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
                    ps.setString(1, item.getTitle());
                    ps.setString(2, item.getLink());
                    ps.setString(3, item.getDescription());
                    ps.setString(4, item.getCreator());
                    ps.setTimestamp(5, Timestamp.from(item.getDateCalendar()));
                    ps.setString(6, item.getSubject());
                    ps.setInt(7, Integer.valueOf(chan.getChannelId()));
                    ps.executeUpdate();
                    dbend.getLogger().fine("inserted a new item: " + item.getTitle());
                }
            }
        }
    } // insertOrUpdateItem


    /** Returns a list of items for the given channel.
     *
     * Returns only items that are not marked as removed.
     */
    public List<NewsItem> getNewsItemByChannel(Channel chan)
	throws Exception
    {
        List<Channel> channels = new LinkedList<Channel>();
        channels.add(chan);
        return getNewsItemByChannels(channels);
    }


    /**
     * Utility method for converting results of database queries into
     * NewsItem objects.
     */
    protected List<NewsItem> convertNewsItemsRS(ResultSet rs)
	throws Exception
    {
	List<NewsItem> newsItems = new LinkedList<NewsItem>();
	while (rs.next()) {
	    NewsItem item = new LazyNewsItem(dbend);
	    item.setNewsItemId(rs.getInt("news_item_id"));
	    item.setTitle(rs.getString("title"));
	    item.setLink(rs.getString("link"));
	    // item.setDescription(rs.getString("description"));
	    item.setCreator(rs.getString("creator"));
	    item.setDateCalendar(Instant.ofEpochMilli(rs.getTimestamp("date").getTime()));
	    item.setSubject(rs.getString("subject"));
	    item.setIsRead(rs.getInt("is_read") == 1 );
	    item.setChannelId(rs.getInt("channel_id"));
	    newsItems.add(item);
	}
	return newsItems;

    }


    /** @todo */
    public List<NewsItem> getNewsItemsInTrash()
	throws Exception
    {
	Connection conn = dbend.getConnection();
        String sql = "SELECT news_item_id, " +
            " title, link, creator, date, subject, is_read, channel_id " +
            " FROM news_item " +
            " WHERE is_trash=1 AND is_removed=0 " +
            " ORDER BY date";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
	    return convertNewsItemsRS(rs);
        }
    }


    /** Loads news items from the database except for their
     * descriptions.
     *
     * <p>Items were not removed nor moved to the trash.
     *
     * @see getNewsItemDescription
     */
    public List<NewsItem> getNewsItemByChannels(List<Channel> channels)
	throws Exception
    {
	Connection conn = dbend.getConnection();
        List<String> channelIds = new LinkedList<String>();

        for (Channel chan : channels) {
            channelIds.add(Integer.toString(chan.getChannelId()));
        }

        if (channelIds.isEmpty()) {
            return new LinkedList<NewsItem>(); // empty Items
        }

        String sql = "SELECT news_item_id, " +
            " title, link, creator, date, subject, is_read, channel_id " +
            " FROM news_item " +
            " WHERE channel_id IN " +
            " ( " + String.join(", ", channelIds) + " ) " +
            " AND is_removed=0 AND is_trash=0 " +
            " ORDER BY date";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
	    return convertNewsItemsRS(rs);
        }
    }


    /** Mark the given item as removed in the database.
     *
     * <p>The record remains in the database, but its
     * <tt>is_removed</tt> flag is set to 1 and description with title
     * are set to NULL to hopefully save space in the database files.
     *
     * <p>Periodic house-keeping by purging old and removed news items
     * from the database will be implemented in a separate method.
     */
    public void removeNewsItem(NewsItem item)
        throws Exception
    {
	Connection conn = dbend.getConnection();

        try (PreparedStatement ps = conn.prepareStatement("UPDATE news_item SET " +
                                                          " is_removed=1, description=NULL, title=NULL " +
                                                          " WHERE news_item_id=?")
             ) {
            ps.setInt(1, Integer.valueOf(item.getNewsItemId()));
            ps.executeUpdate();
            dbend.getLogger().fine("marked item as removed: " + item.getTitle());
        }
    }

    /** Marks news item as moved to trash if it wasn't before, marks
     * as removed if it was already in the trash bin. */
    public void trashNewsItem(NewsItem item)
        throws Exception
    {
	boolean isTrashed = false;
	Connection conn = dbend.getConnection();

        String queryIsTrash = "SELECT is_trash FROM news_item WHERE news_item_id=?";
        try (PreparedStatement ps = conn.prepareStatement(queryIsTrash)) {
	    ps.setInt(1, Integer.valueOf(item.getNewsItemId()));
            ResultSet rs = ps.executeQuery();
            rs.next();
	    isTrashed = (rs.getInt(1) == 1);
	}

	if (isTrashed) {
	    removeNewsItem(item);
	} else {
	    try (PreparedStatement ps = conn.prepareStatement("UPDATE news_item SET " +
							      " is_trash=1 " +
							      " WHERE news_item_id=?")
		 ) {
		ps.setInt(1, Integer.valueOf(item.getNewsItemId()));
		ps.executeUpdate();
		dbend.getLogger().fine("marked item as moved to trash: " + item.getTitle());
	    }
	}
    }


    public void markItemAsRead(NewsItem item, boolean read)
        throws Exception
    {
	Connection conn = dbend.getConnection();

        String sql = "UPDATE news_item SET " +
            " is_read=? " +
            " WHERE news_item_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.valueOf(read ? 1 : 0));
            ps.setInt(2, Integer.valueOf(item.getNewsItemId()));
            ps.executeUpdate();
            dbend.getLogger().fine("marked item as read: " + item.getTitle());
        }
    }


    private int querySingleInt(String sql) {
	Connection conn = dbend.getConnection();
        int intValue = -1;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                intValue = rs.getInt(1);
            }
        } catch (SQLException sqle) {
            dbend.getLogger().log(Level.SEVERE, "failed to query: " + sql, sqle);
        }

        return intValue;
    }


    public int getTotalNewsItemsCount()
    {
        return querySingleInt("SELECT COUNT(*) AS count FROM news_item");
    }


    public int getUnreadNewsItemsCount()
    {
        return querySingleInt("SELECT COUNT(*) AS count FROM news_item WHERE is_read=0 AND is_removed=0");
    }


    public int getRemovedNewsItemsCount()
    {
        return querySingleInt("SELECT COUNT(*) AS count FROM news_item WHERE is_removed=1");
    }



    /** Purge news items for this channel preceding the given
     * timestamp from the database.
     */
    public void cleanupChannel(Channel chan, Instant ts) {
	Connection conn = dbend.getConnection();

        String sql = "DELETE FROM news_item "
            + " WHERE is_removed=1 OR is_trash=1 " // removed items either way
            + " AND channel_id=? "
            + " AND date < ?";
	try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chan.getChannelId());
            ps.setTimestamp(2, new Timestamp(ts.toEpochMilli()));
            ps.executeUpdate();
        } catch (SQLException sqle) {
            dbend.getLogger().log(Level.SEVERE, "failed to cleanup: " + sql, sqle);
        }
    }

    /** Deletes all news items from the database for the given
     * channel.
     */
    public void removeChannelItems(Channel chan) {
	Connection conn = dbend.getConnection();

        String sql = "DELETE FROM news_item WHERE channel_id=? ";
	try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chan.getChannelId());
            ps.executeUpdate();
        } catch (SQLException sqle) {
            dbend.getLogger().log(Level.SEVERE, "failed to remove channel items: " + sql, sqle);
        }
    }
}
