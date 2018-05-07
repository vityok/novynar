package org.bb.vityok.novinar.db;

import java.io.Serializable;

import java.util.Calendar;

import java.util.logging.Level;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import org.bb.vityok.novinar.NewsItem;
import org.bb.vityok.novinar.Novinar;


/** This is the NewsItem bean that stores its data in the database.
 *
 * It does not load all its data at once, but holds sufficient
 * information to load required data on demand. Therefore it is
 * "lazy".
*/
public class LazyNewsItem extends NewsItem

{
    private NewsItemDAO niDAO;

    public LazyNewsItem (NewsItemDAO niDAO) {
        super();
        this.niDAO = niDAO;
    }

    /** Loads description from the database.
     *
     * Doesn't hold any references to the retrieved data to allow
     * garbage collection once it is no longer used.
     */
    public String getDescription() {
        try {
            return niDAO.getNewsItemDescription(this);
        } catch (Exception e) {
            Novinar.getLogger().log(Level.SEVERE, "failed to load description for NewsItem: " + this, e);
            return  "";
        }
    }
}
