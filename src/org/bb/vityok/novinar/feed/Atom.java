package org.bb.vityok.novinar.feed;

import java.util.Calendar;
import java.util.Date;

import java.util.logging.Level;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.bb.vityok.novinar.Novinar;
import org.bb.vityok.novinar.NewsItem;
import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.OPMLManager;


/** Parse Atom feeds as defined by the RFC 4287.
 *
 * See: https://tools.ietf.org/html/rfc4287
 *
 */
public class Atom
    extends FeedParser
{
    public static final String ATOM_XMLNS = "http://www.w3.org/2005/Atom";

    /** In the wild wild web various timestamp formats can be
     * seen. Here are some that I've encountered.
     */
    public static final SimpleDateFormat TIMESTAMP_FORMAT[] = { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                                                                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
                                                                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss") };

    public Atom(Novinar novinar) { super(novinar); }

    /** Check if the given document can be parsed by this parser.
     *
     * @arg doc DOM representation of the feed contents.
     * @return true if the given document can be processed by this parser.
     * @see processFeed
     */
    public boolean accepts(Document doc) {
        String nodeName = doc.getDocumentElement().getNodeName();
        if (nodeName != null && !(nodeName.isEmpty())) {
            return nodeName.equals("feed");
        } else {
            return false;
        }
    }

    /** Extracts "alternate" link for the given entry.
     *
     * When no "altnernate" link is present returns "self", or any
     * link "href" otherwise.
     */
    public String getLink(Element entry) {
        NodeList linkElements = entry.getElementsByTagName("link");
        String hrefAlternate = null;
        String hrefSelf = null;
        String hrefAny = null;
        for (int i = 0; i < linkElements.getLength(); i++) {
            Node linkNode = linkElements.item(i);
            String rel = OPMLManager.getAttribute(linkNode, "rel", null);
            String href = OPMLManager.getAttribute(linkNode, "href", null);
            if (rel != null) {
                switch (rel) {
                case "alternate" : hrefAlternate = href; break;
                case "self" : hrefSelf = href; break;
                }
            }
            if (href != null) {
                hrefAny = href;
            }
        }
        if (hrefAlternate != null) { return hrefAlternate; }
        if (hrefSelf != null) { return hrefSelf; }
        return hrefAny;
    }


    public Calendar extractTimestamp(Element entry, String name) {
        NodeList timestamps = entry.getElementsByTagName(name);
        if (timestamps.getLength() > 0) {
            String tsStr = timestamps.item(0).getTextContent();
            Calendar ts = parseTimestamp(tsStr);
            if (ts != null) { return ts; }

            Novinar.getLogger().severe("Atom parser failed to parse timestamp: " + tsStr);
        }

        return new Calendar.Builder().setInstant(System.currentTimeMillis()).build();
    }


    public void processFeed(Channel chan, Document doc)
	throws Exception
    {
	Element docElement = doc.getDocumentElement();

	if (docElement != null && docElement.getNodeName().equals("feed")) {
	    Element feedElement = docElement ;

	    String cTitle = feedElement.getElementsByTagName("title").item(0).getTextContent();
	    String cLink = getLink(feedElement);
            String cDescription = "";
            if (feedElement.getElementsByTagName("subtitle").getLength() > 0) {
                 cDescription = feedElement.getElementsByTagName("subtitle").item(0).getTextContent();
            }
	    Novinar.getLogger().info("channel title: " + cTitle);
	    Novinar.getLogger().info("channel link: " + cLink);
	    Novinar.getLogger().info("channel description: " + cDescription);

	    NodeList entriesList = docElement.getElementsByTagName("entry");

	    Novinar.getLogger().info("got " + entriesList.getLength() + " entries");

	    for (int i = 0; i < entriesList.getLength(); i++) {
		Element entry = (Element) entriesList.item(i);
		String iTitle = entry.getElementsByTagName("title").item(0).getTextContent();
                // todo: there might be several link elements, choose the one not being rel="self"
		String iLink = getLink(entry);
		String iContent = entry.getElementsByTagName("content").item(0).getTextContent();
                Calendar ts = extractTimestamp(entry, "published");
		NewsItem newsItem = new NewsItem();
		newsItem.setTitle(iTitle);
		newsItem.setLink(iLink);
		newsItem.setDescription(iContent);
                newsItem.setDateCalendar(ts);
		novinar.insertOrUpdateItem(chan, newsItem);
	    }
	}
    }
}
