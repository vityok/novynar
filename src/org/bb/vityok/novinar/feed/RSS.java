package org.bb.vityok.novinar.feed;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.bb.vityok.novinar.Novinar;
import org.bb.vityok.novinar.NewsItem;
import org.bb.vityok.novinar.Channel;

/** Parse RSS feeds.
 *
 * https://en.wikipedia.org/wiki/RSS
 */
public class RSS
    extends FeedParser
{

    public RSS(Novinar novinar) { super(novinar); }

    /** Check if the given document can be parsed by this parser.
     *
     * @arg doc DOM representation of the feed contents.
     * @return true if the given document can be processed by this parser.
     * @see processFeed
     */
    @Override
    public boolean accepts(Document doc) {
        String nodeName = doc.getDocumentElement().getNodeName();
        if (nodeName != null && !(nodeName.isEmpty())) {
            return nodeName.equals("rss");
        } else {
            return false;
        }
    }

    @Override
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

	    for (int i = 0; i < itemsList.getLength(); i++) {
		Element item = (Element) itemsList.item(i);
		String iTitle = item.getElementsByTagName("title").item(0).getTextContent();
		String iLink = item.getElementsByTagName("link").item(0).getTextContent();
		String iDescription = item.getElementsByTagName("description").item(0).getTextContent();

		NewsItem newsItem = new NewsItem();
		newsItem.setTitle(iTitle);
		newsItem.setLink(iLink);
		newsItem.setDescription(iDescription);
		novinar.insertOrUpdateItem(chan, newsItem);
	    }
	}
    }
}
