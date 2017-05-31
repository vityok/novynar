package org.bb.vityok.novinator;

import java.io.Serializable;

import java.util.Calendar;

import javafx.beans.property.SimpleStringProperty;

public class NewsItem
    implements Serializable
{
    private SimpleStringProperty title;
    private SimpleStringProperty link;
    private SimpleStringProperty description;
    private SimpleStringProperty creator;
    private SimpleStringProperty date;
    private SimpleStringProperty subject;

    public NewsItem () { }

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    public String getLink() { return link.get(); }
    public void setLink(String link) { this.link.set(link); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public String getCreator() { return creator.get(); }
    public void setCreator(String creator) { this.creator.set(creator); }

    public String getDate() { return date.get(); }
    public void setDate(String date) { this.date.set(date); }

    public Calendar getDateCalendar() {
	return new Calendar.Builder().setInstant(System.currentTimeMillis()).build();
    }

    public String getSubject() { return subject.get(); }
    public void setSubject(String subject) { this.subject.set(subject); }
}
