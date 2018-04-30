package org.bb.vityok.novinar.db;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;

import java.util.Calendar;
import java.util.List;
import java.util.LinkedList;

import java.util.logging.Level;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.NewsItem;
import org.bb.vityok.novinar.OPMLManager;
import org.bb.vityok.novinar.Channel;


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
	// check if such item already exists in the database before
	// insert
        String sqlSel = "SELECT link FROM news_item WHERE link=?";
	try (PreparedStatement cs = conn.prepareStatement(sqlSel)) {
            cs.setString(1, item.getLink());
            ResultSet rscs = cs.executeQuery();
            if (!rscs.next()) {
                String sqlIns = "INSERT INTO news_item(title, link, description, " +
                    " creator, date, subject, channel_id)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
                    ps.setString(1, item.getTitle());
                    ps.setString(2, item.getLink());
                    ps.setString(3, item.getDescription());
                    ps.setString(4, item.getCreator());
                    ps.setTimestamp(5, new Timestamp(item.getDateCalendar().getTimeInMillis()));
                    ps.setString(6, item.getSubject());
                    ps.setInt(7, Integer.valueOf(chan.getChannelId()));
                    ps.executeUpdate();
                    dbend.getLogger().fine("inserted a new item: " + item.getTitle());
                }
            }
        }
    }


    public String readStringFromAsciiString(InputStream is)
        throws Exception
    {
        // see https://stackoverflow.com/a/35446009
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }


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

    public List<NewsItem> getNewsItemByChannels(List<Channel> channels)
	throws Exception
    {
	Connection conn = dbend.getConnection();
        List<String> channelIds = new LinkedList<String>();
        for (Channel chan : channels) {
            channelIds.add(Integer.toString(chan.getChannelId()));
        }

        String sql = "SELECT news_item_id, " +
            " title, link, description, creator, date, subject, is_read " +
            " FROM news_item " +
            " WHERE channel_id IN " +
            " ( " + String.join(", ", channelIds) + " ) " +
            " AND is_removed=0 " +
            " ORDER BY date";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // ps.setArray(1, conn.createArrayOf("int", channelIds));

            List<NewsItem> list = new LinkedList<NewsItem>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NewsItem item = new NewsItem();
                item.setNewsItemId(rs.getInt("news_item_id"));
                item.setTitle(rs.getString("title"));
                item.setLink(rs.getString("link"));
                item.setDescription(rs.getString("description"));
                item.setCreator(rs.getString("creator"));
                item.setDate(rs.getString("date"));
                item.setSubject(rs.getString("subject"));
                item.setIsRead(rs.getInt("is_read") == 1 );
                list.add(item);
            }
            return list;
        }


    }

    /** Mark the given item as removed in the database.
     *
     * The record remains in the database, but its <tt>is_removed</tt>
     * flag is set to 1 and description with title are set to NULL to
     * hopefully save space in the database files.
     *
     * Periodic house-keeping by purging old and removed news items
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
        int count = -1;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException sqle) {
            dbend.getLogger().log(Level.SEVERE, "failed to query: " + sql, sqle);
        }

        return count;
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
    public void cleanupChannel(Channel chan, Calendar ts) {
	Connection conn = dbend.getConnection();

        String sql = "DELETE FROM news_item "
            + " WHERE is_removed=1 " // user doesn't see these items
            + " AND channel_id=? "
            + " AND date < ?";
	try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chan.getChannelId());
            ps.setTimestamp(2, new Timestamp(ts.getTimeInMillis()));
            ps.executeUpdate();
        } catch (SQLException sqle) {
            dbend.getLogger().log(Level.SEVERE, "failed to cleanup: " + sql, sqle);
        }
    }

}
