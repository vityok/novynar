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

        if (this.url == null) {
            // feed URL is not defined, treat it as a folder
            buildChildren();
        } else {
            // feed URL is defined for Channels
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
        OPMLManager.getInstance().setAttribute(node, namespaceURI, name, value);
    }


    public String getAttributeNS(String namespaceURI, String name, String defaultValue) {
        return OPMLManager.getInstance().getAttributeNS(node, namespaceURI, name, defaultValue);
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

    public String toString() { return getTitle(); }

    /** Find child outline nodes and add them to the children list.
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
