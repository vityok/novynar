package org.bb.vityok.novinar.core;

import java.io.Serializable;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/** Representation of a news feed channel.
 *
 * <p>It is an Outline with the feed URL attribute defined. Also it is a
 * leaf in the OPML tree and therefore has no children. However, it
 * might have child elements in the DOM tree that define some
 * properties, like description, that are not fit to be stored in the
 * Attributes.
 */
public class Channel
    implements Serializable
{
    private static final long serialVersionUID = -5268402804904404881L;
	
    private int channelId;
    private String link;
    private String description;
    private Instant latestUpdate;  // last time the channel has been updated
    private String problems;
    // utilize JavaFX beans extensions to avoid messing with the 
    // property change listeners
    private BooleanProperty propHasProblems = new SimpleBooleanProperty(false);
    
    /** Outline that corresponds to this channel object. */
    private final Outline ol;

    public Channel(Outline ol) {
        this.ol = ol;
        this.link = ol.getAttribute("xmlUrl", null);
        OPMLManager oman = ol.getOPMLManager();
        String idStr = OPMLManager.getAttributeNS(ol.getNode(),
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
        String tsStr = OPMLManager.getAttributeNS(ol.getNode(),
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
    public int getChannelId() {
    	return channelId;
    }

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

    public String getDescription() {
	return description;
    }
    
    public void setDescription(String description) {
	this.description = description;
    }

    public Instant getLatestUpdate() {
        return latestUpdate;
    }

    public void setLatestUpdate(Instant inst) {
	this.latestUpdate = inst;
	String updateTs = latestUpdate.toString();

	OPMLManager oman = ol.getOPMLManager();
	oman.setAttribute(ol.getNode(), OPMLManager.NOVINAR_NS, OPMLManager.Q_LAST_UPDATED, updateTs);
	Novinar.getLogger().info("updated channel: " + getTitle() + " at: " + updateTs);
    }

    /** Mark the channel as updated just now.
     *
     * <p>
     * Like the Unix <tt>touch</tt> program does to the files.
     */
    public void touch() {
	setLatestUpdate(Instant.now());
    }

    public boolean getIgnoreOnBoot() {
        return ol.getIgnoreOnBoot();
    }

    public UpdatePeriod getUpdatePeriod() {
    	return ol.getUpdatePeriod();
    }

    public String getProblems() {
    	return problems;
    }
    
    public void setProblems(String problems) {
    	this.problems = problems;
    	propHasProblems.set(hasProblems());
    	System.out.println("___van: " + hasProblems() + " - "+ problems);
    }
    
    public boolean hasProblems() {
    	return !(problems == null || problems.isEmpty());
    }
    
    public BooleanProperty hasProblemsProperty() {
	return propHasProblems;
    }
    
    @Override
    public String toString() {
	return "channel {id=" + getChannelId() + " link=" + getLink() + " title=" + getTitle() + "}";
    }
}
