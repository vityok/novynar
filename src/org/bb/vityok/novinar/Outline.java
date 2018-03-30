package org.bb.vityok.novinar;

import java.util.List;
import java.util.LinkedList;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** Represents an OPML Outline with the minimal set of features
 * required for handling the feed information.
 */
public class Outline
{
    private String url;
    private String title;
    private int id;
    private Node node;
    private List<Outline> children = null;
    private Channel channel = null;


    /** Binds the outline with the associated DOM node. */
    public Outline(Node node) {
        this.node = node;
        this.title = getAttribute("text", "N/A");
        this.url = getAttribute("xmlUrl", null);
        this.id = 0;

        String idStr = getAttributeNS(OPMLManager.NOVINAR_NS, "channelId", null);
        if (this.url == null) {
            // feed URL is not defined, treat it as a folder
            buildChildren();
        } else {
            // feed URL is defined, construct a channel object based
            // on this outline
            if (idStr == null) {
                // this is an item that we didn't process yet

                // todo: instead of maintaining counter in the database,
                // place it as the attribute of the root node in the OPML
                // document. Make OPMLManager increment the counter
                // whenever needed

                // todo: refactor this.id = ChannelDAO.getInstance().createChannelFor(url);
                setAttribute(OPMLManager.NOVINAR_NS,
                             "channelId",
                             Integer.toString(OPMLManager.getInstance().genChannelId()));
            } else {
                this.id = Integer.valueOf(idStr);
            }

            this.channel = new Channel(this);
            OPMLManager.getInstance().addChannel(channel);
        }
    }

    public Node getNode() { return node; }

    /** Channels are leafs in the OPML tree, return the associated
     * channel object if there is any.
     */
    public Channel getChannel() { return channel; }

    public String getAttribute(String name, String defaultValue) {
        NamedNodeMap atts = node.getAttributes();
        Node att = atts.getNamedItem(name);
        return (att == null) ? defaultValue : att.getNodeValue();
    }


    public void setAttribute(String namespaceURI, String name, String value) {
        NamedNodeMap atts = node.getAttributes();
        Attr attr = OPMLManager.getInstance()
            .getDocument()
            .createAttributeNS(namespaceURI, name);
        attr.setValue(value);
        atts.setNamedItemNS(attr);
    }


    public String getAttributeNS(String namespaceURI, String name, String defaultValue) {
        NamedNodeMap atts = node.getAttributes();
        Node att = atts.getNamedItemNS(namespaceURI, name);
        return (att == null) ? defaultValue : att.getNodeValue();
    }


    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public int getId() { return id; }

    public List<Outline> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return (children == null) ?
            false :
            ( children.size() > 0 );
    }

    public String toString() { return "outline: " + getTitle(); }

    /** Find child outline nodes and add them to the children
     * list.
     */
    private void buildChildren() {
        NodeList childNodes = node.getChildNodes();
        children = new LinkedList<Outline>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().equals("outline")) {
                children.add(new Outline(childNode));
            }
        }
    }
} // end class Outline
