package org.bb.vityok.novinator;

import java.io.Serializable;

public class NewsItem
    implements Serializable
{
    private String title;
    private String link;
    private String description;
    private String creator;
    private String date;
    private String subject;

    public NewsItem () { }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
 
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}
