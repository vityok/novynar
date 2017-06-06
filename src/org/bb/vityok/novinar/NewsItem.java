package org.bb.vityok.novinar;

import java.io.Serializable;

import java.util.Calendar;

import javafx.beans.property.SimpleStringProperty;

public class NewsItem
    implements Serializable
{
    private SimpleStringProperty title = new SimpleStringProperty();
    private SimpleStringProperty link = new SimpleStringProperty();
    private SimpleStringProperty description = new SimpleStringProperty();
    private SimpleStringProperty creator = new SimpleStringProperty();
    private SimpleStringProperty date = new SimpleStringProperty();
    private SimpleStringProperty subject = new SimpleStringProperty();

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
