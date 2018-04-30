package org.bb.vityok.novinar.feed;

import java.util.Calendar;

import java.util.logging.Level;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.bb.vityok.novinar.Novinar;
import org.bb.vityok.novinar.NewsItem;
import org.bb.vityok.novinar.Channel;


/** Parse RDF/XML news feeds.
 *
 * Format specification is available at: http://web.resource.org/rss/1.0/spec
 */
public class RDF
    extends FeedParser
{
    // Dublin core xmlns
    public static final String DC_NS = "http://purl.org/dc/elements/1.1/";
    public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RSS_RDF_NS = "http://purl.org/rss/1.0/";
    public static final String SYN_NS = "http://purl.org/rss/1.0/modules/syndication/";

    public RDF(Novinar novinar) { super(novinar); }

    /** Check if the given document can be parsed by this parser.
     *
     * @arg doc DOM representation of the feed contents.
     * @return true if the given document can be processed by this parser.
     * @see processFeed
     */
    public boolean accepts(Document doc) {
        String nodeName = doc.getDocumentElement().getNodeName();
        if (nodeName != null && !(nodeName.isEmpty())) {
            return nodeName.equals("rdf:RDF");
        } else {
            return false;
        }
    }


    /** Tries to extract entry timestamp as specified in a Dublic Core
     * date element.
     */
    public Calendar extractTimestamp(Element entry) {
        NodeList timestamps = entry.getElementsByTagName("dc:date");
        if (timestamps.getLength() > 0) {
            String tsStr = timestamps.item(0).getTextContent();
            Calendar ts = parseTimestamp(tsStr);
            if (ts != null) {
                return ts;
            }

            Novinar.getLogger().log(Level.SEVERE, "failed to parse timestamp: " + tsStr);
        } else {
            Novinar.getLogger().log(Level.SEVERE, "failed to extract timestamp: " + entry.getTagName());
        }
        return new Calendar.Builder().setInstant(System.currentTimeMillis()).build();
    }


    public void processFeed(Channel chan, Document doc)
	throws Exception
    {
	Element docElement = doc.getDocumentElement();
	Node channelNode = docElement.getElementsByTagName("channel").item(0);

	Novinar.getLogger().finer("Channel node is: " + channelNode.getNodeName());

	if (channelNode.getNodeType() == Node.ELEMENT_NODE) {
	    Element channelElement = (Element) channelNode;

	    String cTitle = channelElement.getElementsByTagName("title").item(0).getTextContent();
	    String cLink = channelElement.getElementsByTagName("link").item(0).getTextContent();
	    String cDescription = channelElement.getElementsByTagName("description").item(0).getTextContent();
	    Novinar.getLogger().info("channel title: " + cTitle);
	    Novinar.getLogger().info("channel link: " + cLink);
	    Novinar.getLogger().info("channel description: " + cDescription);

	    NodeList itemsList = docElement.getElementsByTagName("item");

	    Novinar.getLogger().info("got " + itemsList.getLength() + " items in description");

            Calendar oldestTimestamp = null;

	    for (int i = 0; i < itemsList.getLength(); i++) {
		Element item = (Element) itemsList.item(i);
		String iTitle = item.getElementsByTagName("title").item(0).getTextContent();
		String iLink = item.getElementsByTagName("link").item(0).getTextContent();
		String iDescription = item.getElementsByTagName("description").item(0).getTextContent();
                Calendar iTs = extractTimestamp(item);
		NewsItem newsItem = new NewsItem();
		newsItem.setTitle(iTitle);
		newsItem.setLink(iLink);
		newsItem.setDescription(iDescription);
                newsItem.setDateCalendar(iTs);

                if (oldestTimestamp == null
                    || iTs.before(oldestTimestamp)) {
                    oldestTimestamp = iTs;
                }

                NodeList creators = item.getElementsByTagName("dc:creator");
                if (creators.getLength() > 0) {
                    String iCreator = creators.item(0).getTextContent();
                    newsItem.setCreator(iCreator);
                }
		novinar.insertOrUpdateItem(chan, newsItem);
	    }

            novinar.cleanupChannel(chan, oldestTimestamp);
	}
    }
}
