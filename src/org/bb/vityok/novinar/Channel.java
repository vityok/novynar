package org.bb.vityok.novinar;

import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Node;

import java.io.Serializable;

import java.util.Calendar;


/** Representation of a news feed channel.
 *
 * It is an Outline with the feed URL attribute defined. Also it is a
 * leaf in the OPML tree and therefore has no children. However, it
 * might have child elements in the DOM tree that define some
 * properties, like description, that are not fit to be stored in the
 * Attributes.
 */
public class Channel
    implements Serializable
{
    private int channelId;
    private String title;
    private String link;
    private String description;
    private Calendar latestUpdate;  // last time the channel has been updated
    private List<NewsItem> items;

    private Outline ol;

    public Channel(Outline ol) {
        this.ol = ol;

        OPMLManager oman = OPMLManager.getInstance();
        String idStr = oman.getAttributeNS(ol.getNode(),
                                           OPMLManager.NOVINAR_NS,
                                           OPMLManager.A_CHANNEL_ID,
                                           null);
        if (idStr == null) {
            // this is an item that we didn't process yet
            this.channelId = oman.genChannelId();
            setChannelId(channelId);
        } else {
            this.channelId = Integer.valueOf(idStr);
        }
    } // end Channel

    /** primary key */
    public int getChannelId() { return channelId; }

    /** Assign a new ID for this channel. */
    public void setChannelId(int channelId) {
        OPMLManager oman = OPMLManager.getInstance();
        oman.setAttribute(ol.getNode(),
                          OPMLManager.NOVINAR_NS,
                          OPMLManager.A_CHANNEL_ID,
                          Integer.toString(channelId));
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { /* todo */ }

    public String getLink() { return ol.getUrl(); }
    public void setLink(String link) { /* todo */ }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Calendar getLatestUpdate() { return latestUpdate; }
    public void setLatestUpdate(Calendar cal) { this.latestUpdate = cal; }

    /** Mark the channel as updated just now. */
    public void updatedNow() {
	setLatestUpdate(new Calendar.Builder()
                             .setInstant(System.currentTimeMillis())
                             .build());
    }

    // public List<NewsItem> getItems() { return items; }
    // public void setItems(List<NewsItem> items) { this.items = items; }

    public String toString() {
        return "channel {id=" + getChannelId()
            + " link=" + getLink()
            + " title=" + getTitle()
            + "}";
    }
}
