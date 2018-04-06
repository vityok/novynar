package org.bb.vityok.novinar.feed;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.bb.vityok.novinar.Novinar;
import org.bb.vityok.novinar.NewsItem;
import org.bb.vityok.novinar.Channel;


/** Parse Atom feeds as defined by the RFC 4287.
 *
 * See: https://tools.ietf.org/html/rfc4287
 *
 */
public class Atom
    extends FeedParser
{
    public static final String ATOM_XMLNS = "http://www.w3.org/2005/Atom";
    
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

    public void processFeed(Channel chan, Document doc)
	throws Exception
    {
	Element docElement = doc.getDocumentElement();
	Node feedNode = docElement.getElementsByTagName("feed").item(0);

    }
}
