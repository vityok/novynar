package org.bb.vityok.novinar;

import java.util.Calendar;
import java.util.List;

import java.io.Serializable;

/** Representation of a news feed channel. */
public class Channel
    implements Serializable
{
    private int channelId;
    private String title;
    private String link;
    private String description;
    private Calendar latestUpdate;  // last time the channel has been updated
    private List<NewsItem> items;

    public Channel () { }

    /** primary key */
    public int getChannelId() { return channelId; }
    public void setChannelId(int channelId) { this.channelId = channelId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Calendar getLatestUpdate() { return latestUpdate; }
    public void setLatestUpdate() { this.latestUpdate = latestUpdate; }

    public List<NewsItem> getItems() { return items; }
    public void setItems(List<NewsItem> items) { this.items = items; }
}
