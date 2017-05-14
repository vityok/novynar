package org.bb.vityok.novinator.feed;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.bb.vityok.novinator.NewsItem;

import org.bb.vityok.novinator.db.Backend;

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
	Element docElement = doc.getDocumentElement();
	Node channelNode = docElement.getElementsByTagName("channel").item(0);

	System.out.println("Channel node is: " + channelNode.getNodeName());

	if (channelNode.getNodeType() == Node.ELEMENT_NODE) {
	    Element channelElement = (Element) channelNode;

	    System.out.println("channel title: " + channelElement.getElementsByTagName("title").item(0).getTextContent());
	    System.out.println("channel link: " + channelElement.getElementsByTagName("link").item(0).getTextContent());
	    System.out.println("channel description: " + channelElement.getElementsByTagName("description").item(0).getTextContent());

	    NodeList itemsList = docElement.getElementsByTagName("item");

	    System.out.println("got " + itemsList.getLength() + " items in description");

	    for (int i = 0; i < itemsList.getLength(); i++) {
		Element item = (Element) itemsList.item(i);
		System.out.println("item title: " + item.getElementsByTagName("title").item(0).getTextContent());
		System.out.println("item link: " + item.getElementsByTagName("link").item(0).getTextContent());
		// System.out.println("item description: " + item.getElementsByTagName("description").item(0).getTextContent());
		NewsItem newsItem = new NewsItem();
	    }
	}

	/*
	  NodeList topNodes = doc.getChildNodes();
	*/
    }
}
