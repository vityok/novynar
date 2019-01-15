package org.bb.vityok.novinar.feed;

import java.time.Instant;

import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.bb.vityok.novinar.core.Channel;
import org.bb.vityok.novinar.core.NewsItem;
import org.bb.vityok.novinar.core.Novinar;


/** Parse RDF/XML news feeds.
 *
 * Format specification is available at: http://web.resource.org/rss/1.0/spec
 */
public class RDF
    extends FeedParser
{

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
    public Instant extractTimestamp(Element entry) {
        NodeList timestamps = entry.getElementsByTagName("dc:date");
        if (timestamps.getLength() > 0) {
            String tsStr = timestamps.item(0).getTextContent();
            Instant ts = parseTimestamp(tsStr);
            if (ts != null) {
                return ts;
            }

            Novinar.getLogger().log(Level.SEVERE, "failed to parse timestamp: " + tsStr);
        } else {
            Novinar.getLogger().log(Level.SEVERE, "failed to extract timestamp: " + entry.getTagName());
        }
        return Instant.now();
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

            Instant oldestTimestamp = null;

            for (int i = 0; i < itemsList.getLength(); i++) {
                Element item = (Element) itemsList.item(i);
                String iTitle = item.getElementsByTagName("title").item(0).getTextContent();
                String iLink = item.getElementsByTagName("link").item(0).getTextContent();
                String iDescription = "";
                if (item.getElementsByTagName("description").getLength() > 0) {
                    iDescription = item.getElementsByTagName("description").item(0).getTextContent();
                } else if (item.getElementsByTagNameNS(CONTENT_NS, "encoded").getLength() > 0) {
                    iDescription = item.getElementsByTagNameNS(CONTENT_NS, "encoded").item(0).getTextContent();
                } else {
                    Novinar.getLogger().severe("failed to extract description: " + item);
                }

                Instant iTs = extractTimestamp(item);
                NewsItem newsItem = new NewsItem();
                newsItem.setTitle(iTitle);
                newsItem.setLink(iLink);
                newsItem.setDescription(iDescription);
                newsItem.setDateCalendar(iTs);

                if (oldestTimestamp == null
                    || iTs.isBefore(oldestTimestamp)) {
                    oldestTimestamp = iTs;
                }

                String iCreator = extractCreator(item);
                newsItem.setCreator(iCreator);
                novinar.insertOrUpdateItem(chan, newsItem);
            }

            novinar.cleanupChannel(chan, oldestTimestamp);
        }
    }
}
