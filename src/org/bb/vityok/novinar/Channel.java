package org.bb.vityok.novinar;

import java.io.Serializable;

import java.text.ParseException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.List;

import org.w3c.dom.Node;


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
    private Instant latestUpdate;  // last time the channel has been updated
    private List<NewsItem> items;

    /** Outline that corresponds to this channel object. */
    private final Outline ol;

    public Channel(Outline ol) {
        this.ol = ol;
        this.link = ol.getAttribute("xmlUrl", null);
        OPMLManager oman = ol.getOPMLManager();
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

        // timestamp
        String tsStr = oman.getAttributeNS(ol.getNode(),
                                           OPMLManager.NOVINAR_NS,
                                           OPMLManager.A_LAST_UPDATED,
                                           null);
        latestUpdate = null;
        if (tsStr != null) {
            try {
                latestUpdate = Instant.parse(tsStr);
            } catch (DateTimeParseException pe) {
                Novinar.getLogger().severe("failed to parse Channel " + getTitle()
                                           + " timestamp: " + tsStr);
            }
        }
        if (latestUpdate == null) {
            // couldn't parse it from the XML
            latestUpdate = Instant.EPOCH;
        }
    } // end Channel

    /** primary key */
    public int getChannelId() { return channelId; }

    /** Assign a new ID for this channel. */
    public void setChannelId(int channelId) {
        OPMLManager oman = ol.getOPMLManager();
        oman.setAttribute(ol.getNode(),
                          OPMLManager.NOVINAR_NS,
                          OPMLManager.Q_CHANNEL_ID,
                          Integer.toString(channelId));
    }

    public String getTitle() { return ol.getTitle(); }
    /** @todo */
    public void setTitle(String title) { ol.setTitle(title); }

    public String getLink() { return link; }

    /** @todo */
    public void setLink(String link) {
        this.link = link;
        OPMLManager oman = ol.getOPMLManager();
        oman.setAttribute(ol.getNode(),
                          Outline.A_XML_URL,
                          link);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getLatestUpdate() {
        return latestUpdate;
    }
    public void setLatestUpdate(Instant inst) {
        this.latestUpdate = inst;
        String updateTs = latestUpdate.toString();

        OPMLManager oman = ol.getOPMLManager();
        oman.setAttribute(ol.getNode(),
                          OPMLManager.NOVINAR_NS,
                          OPMLManager.Q_LAST_UPDATED,
                          updateTs);
        Novinar.getLogger().info("updated channel: " + getTitle()
                                 + " at: " + updateTs);
    }

    /** Mark the channel as updated just now.
     *
     * Like the Unix <tt>touch</tt> program does to the files.
     */
    public void touch() {
	setLatestUpdate(Instant.now());
    }

    public boolean getIgnoreOnBoot() {
        return ol.getIgnoreOnBoot();
    }

    public UpdatePeriod getUpdatePeriod() { return ol.getUpdatePeriod(); }

    @Override
    public String toString() {
        return "channel {id=" + getChannelId()
            + " link=" + getLink()
            + " title=" + getTitle()
            + "}";
    }
}
