package org.bb.vityok.novinator;

import java.util.List;

import java.io.Serializable;

/** Representation of a news feed channel. */
public class Channel
    implements Serializable
{
    private String title;
    private String link;
    private String description;
    private List<NewsItem> items;

    public Channel () { }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<NewsItem> getItems() { return items; }
    public void setItems(List<NewsItem> items) { this.items = items; }

}
