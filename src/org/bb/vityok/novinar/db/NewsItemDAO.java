package org.bb.vityok.novinar.db;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.LinkedList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
	PreparedStatement cs = conn.prepareStatement("SELECT link FROM news_item WHERE link=?");
	cs.setString(1, item.getLink());
	ResultSet rscs = cs.executeQuery();
	if (!rscs.next()) {
	    PreparedStatement ps = conn.prepareStatement("INSERT INTO news_item(title, link, description, creator, date, subject, channel_id, is_read)"
							 + " VALUES (?, ?, ?, ?, ?, ?, ?, 0)");
	    ps.setString(1, item.getTitle());
	    ps.setString(2, item.getLink());
	    ps.setString(3, item.getDescription());
	    ps.setString(4, item.getCreator());
	    ps.setTimestamp(5, new Timestamp(item.getDateCalendar().getTimeInMillis()));
	    ps.setString(6, item.getSubject());
	    ps.setInt(7, Integer.valueOf(chan.getChannelId()));
	    ps.executeUpdate();
	    System.out.println("inserted a new item: " + item.getTitle());
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


    /** Returns a list of items for the given channel or all items if
     * the given channel is null.
     */
    public List<NewsItem> getNewsItemByChannel(Channel chan)
	throws Exception
    {
	List<NewsItem> list = new LinkedList<NewsItem>();
	Connection conn = dbend.getConnection();
	PreparedStatement ps;
        if (chan != null) {
            ps = conn.prepareStatement("SELECT title, link, description, creator, date, subject FROM news_item WHERE channel_id=?");
            ps.setInt(1, chan.getChannelId());
        } else {
            ps = conn.prepareStatement("SELECT title, link, description, creator, date, subject FROM news_item");
        }

	ResultSet rs = ps.executeQuery();
	while (rs.next()) {
	    NewsItem item = new NewsItem();
	    item.setTitle(rs.getString("title"));
	    item.setLink(rs.getString("link"));
	    item.setDescription(rs.getString("description"));
	    item.setCreator(rs.getString("creator"));
	    item.setDate(rs.getString("date"));
	    item.setSubject(rs.getString("subject"));
	    list.add(item);
	}
	return list;
    }

    public List<NewsItem> getNewsItemByChannels(List<Channel> chans)
	throws Exception
    {
        return new LinkedList<>();
    }
}
