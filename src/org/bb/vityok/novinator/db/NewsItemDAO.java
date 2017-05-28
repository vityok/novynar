package org.bb.vityok.novinator.db;

import java.sql.PreparedStatement;

import org.bb.vityok.novinator.NewsItem;

/** Implements database access operations for the NewsItem object.
 *
 * It is not an interface nor an abstract class but an ordinary class
 * because currently there is just one database backend (and no other
 * backends are planned) so additional abstraction is redundand.
 */
public class NewsItemDAO
{
    public NewsItemDAO()
    {
    }

    public void insertOrUpdateItem(String channelId, NewsItem item)
	throws Exception
    {
	Backend be = Backend.getInstance();
	PreparedStatement psInsert = be.getConnection().prepareStatement("insert into location values (?, ?)");
    }
}
