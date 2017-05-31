package org.bb.vityok.novinator.feed;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.bb.vityok.novinator.NewsItem;

import org.bb.vityok.novinator.db.Backend;
import org.bb.vityok.novinator.db.NewsItemDAO;


/** Parse RDF/XML news feeds.
 *
 * Format specification is available at: http://web.resource.org/rss/1.0/spec
 */
public class RDF
{
    private static final RDF INSTANCE = new RDF();

    protected RDF() { }

    public static RDF getInstance() { return INSTANCE; }

    public void processFeed(Document doc)
	throws Exception
    {
	NewsItemDAO dao = new NewsItemDAO();
	Element docElement = doc.getDocumentElement();
	Node channelNode = docElement.getElementsByTagName("channel").item(0);

	System.out.println("Channel node is: " + channelNode.getNodeName());

	if (channelNode.getNodeType() == Node.ELEMENT_NODE) {
	    Element channelElement = (Element) channelNode;

	    String cTitle = channelElement.getElementsByTagName("title").item(0).getTextContent();
	    String cLink = channelElement.getElementsByTagName("link").item(0).getTextContent();
	    String cDescription = channelElement.getElementsByTagName("description").item(0).getTextContent();
	    System.out.println("channel title: " + cTitle);
	    System.out.println("channel link: " + cLink);
	    System.out.println("channel description: " + cDescription);

	    NodeList itemsList = docElement.getElementsByTagName("item");

	    System.out.println("got " + itemsList.getLength() + " items in description");

	    for (int i = 0; i < itemsList.getLength(); i++) {
		Element item = (Element) itemsList.item(i);
		String iTitle = item.getElementsByTagName("title").item(0).getTextContent();
		String iLink = item.getElementsByTagName("link").item(0).getTextContent();
		String iDescription = item.getElementsByTagName("description").item(0).getTextContent();
		// System.out.println("item title: " + iTitle);
		// System.out.println("item link: " + iLink);
		NewsItem newsItem = new NewsItem();
		newsItem.setTitle(iTitle);
		newsItem.setLink(iLink);
		newsItem.setDescription(iDescription);
		dao.insertOrUpdateItem(cTitle, newsItem);
	    }
	}
    }
}
