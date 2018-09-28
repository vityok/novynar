package org.bb.vityok.novinar.core;

import java.io.Serializable;

import java.time.Instant;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/** Representation of the news item from a news feed. */
// todo: make it possible to get channel based on news item
public class NewsItem
    implements Serializable
{
	private static final long serialVersionUID = -8251368066387551468L;
	
	private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleStringProperty title = new SimpleStringProperty();
    private SimpleStringProperty link = new SimpleStringProperty();
    private SimpleStringProperty description = new SimpleStringProperty();
    private SimpleStringProperty creator = new SimpleStringProperty();
    private SimpleStringProperty date = new SimpleStringProperty();
    private SimpleStringProperty subject = new SimpleStringProperty();
    private SimpleBooleanProperty isRead = new SimpleBooleanProperty();
    private Instant cDate;
    private int channelId;

    public NewsItem () {
        cDate = Instant.now();
    }

    /** Every news item has a unique ID automatically generated by a
     * database sequence.
     */
    public int getNewsItemId() { return id.get(); }
    public void setNewsItemId(int id) { this.id.set(id); }

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    public String getLink() { return link.get(); }
    public void setLink(String link) { this.link.set(link); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public String getCreator() { return creator.get(); }
    public void setCreator(String creator) { this.creator.set(creator); }

    public String getDate() { return cDate.toString(); }
    public void setDate(String date) { this.date.set(date); }

    public int getChannelId() { return channelId; }
    public void setChannelId(int channelId) { this.channelId = channelId; }

    public Instant getDateCalendar() {
	return cDate;
    }

    public void setDateCalendar(Instant cDate) {
        this.cDate = cDate;
    }

    public String getSubject() { return subject.get(); }
    public void setSubject(String subject) { this.subject.set(subject); }

    public boolean getIsRead() { return isRead.get(); }
    public void setIsRead(boolean isRead) {
        this.isRead.set(isRead);
    }

    public SimpleBooleanProperty isReadProperty() { return isRead; }
}