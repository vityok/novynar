package org.bb.vityok.novinator.db;

import java.util.List;
import java.util.LinkedList;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

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
	PreparedStatement ps = be.getConnection().prepareStatement("INSERT INTO news_item(title, link, description, creator, date, subject)"
								   + " VALUES (?, ?, ?, ?, ?, ?)");
	ps.setString(1, item.getTitle());
	ps.setString(2, item.getLink());
	ps.setString(3, item.getDescription());
	ps.setString(4, item.getCreator());
	ps.setTimestamp(5, new Timestamp(item.getDateCalendar().getTimeInMillis()));
	ps.setString(6, item.getSubject());
	ps.executeUpdate();
	System.out.println("inserted a new item: " + item.getTitle());
    }


    public List<NewsItem> getNewsItemByChannel(String channel)
	throws Exception
    {
	// /*
	//   We select the rows and verify the results.
	// */
	// rs = s.executeQuery(
	// 		    "SELECT num, addr FROM location ORDER BY num");

	// /* we expect the first returned column to be an integer (num),
	//  * and second to be a String (addr). Rows are sorted by street
	//  * number (num).
	//  *
	//  * Normally, it is best to use a pattern of
	//  *  while(rs.next()) {
	//  *    // do something with the result set
	//  *  }
	//  * to process all returned rows, but we are only expecting two rows
	//  * this time, and want the verification code to be easy to
	//  * comprehend, so we use a different pattern.
	//  */

	// int number; // street number retrieved from the database
	// boolean failure = false;
	// if (!rs.next())
	//     {
	// 	failure = true;
	// 	reportFailure("No rows in ResultSet");
	//     }

	// if ((number = rs.getInt(1)) != 300)
	//     {
	// 	failure = true;
	// 	reportFailure(
	// 		      "Wrong row returned, expected num=300, got " + number);
	//     }

	// if (!rs.next())
	//     {
	// 	failure = true;
	// 	reportFailure("Too few rows");
	//     }

	// if ((number = rs.getInt(1)) != 1910)
	//     {
	// 	failure = true;
	// 	reportFailure(
	// 		      "Wrong row returned, expected num=1910, got " + number);
	//     }

	// if (rs.next())
	//     {
	// 	failure = true;
	// 	reportFailure("Too many rows");
	//     }

	// if (!failure) {
	//     System.out.println("Verified the rows");
	// }

	// // delete the table
	// s.execute("drop table location");
	// System.out.println("Dropped table location");

	return null;
    }
}
