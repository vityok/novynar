package org.bb.vityok.novinar.db;

import java.io.Serializable;

import java.util.Calendar;

import java.util.logging.Level;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import org.bb.vityok.novinar.core.Channel;
import org.bb.vityok.novinar.core.NewsItem;

/** This is the NewsItem bean that stores its data in the database.
 *
 * It does not load all its data at once, but holds sufficient
 * information to load required data on demand. Therefore it is
 * "lazy".
 *
 * At the moment only item's description is loaded in a lazy way,
 * which leaves some space for improvement.
 */
public class LazyNewsItem extends NewsItem

{
    private Backend dbend;
    private int channelId;


    public LazyNewsItem (Backend dbend) {
        super();
        this.dbend = dbend;
    }

    /** Loads description from the database.
     *
     * Doesn't hold any references to the retrieved data to allow
     * garbage collection once it is no longer used.
     */
    public String getDescription() {
	Connection conn = dbend.getConnection();

        String sql = "SELECT description FROM news_item WHERE news_item_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.valueOf(getNewsItemId()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("description");
            }

        } catch (SQLException e) {
            dbend.getLogger().log(Level.SEVERE, "failed to load description for NewsItem: " + this, e);
        }
        return "";
    }

    public void setDescription() {
       dbend.getLogger().log(Level.SEVERE, "LazyNewsItem can not set description");
    }
}
