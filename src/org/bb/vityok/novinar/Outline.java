package org.bb.vityok.novinar;

import java.util.List;
import java.util.LinkedList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** Represents an OPML Outline with the minimal set of features
 * required for handling the feed information.
 */
public class Outline
{
    private String title;
    private Node node; // DOM node that corresponds to this outline

    private List<Outline> children = null;

    /* Channel object if there is any, and there must be one if this
     * is a channel outline */
    private Channel channel = null;

    private OPMLManager oman;


    /** Binds the outline with the associated DOM node. */
    public Outline(OPMLManager oman, Node node) {
        this.oman = oman;
        this.node = node;
        this.title = getAttribute("text", "N/A");
        String url = getAttribute("xmlUrl", null);

        if (url == null || url.isEmpty()) {
            // feed URL is not defined, treat it as a folder
            buildChildren();
        } else {
            // feed URL is defined for Channels
            this.channel = new Channel(this);
            oman.addChannel(channel);
        }
    }

    public Node getNode() { return node; }
    public OPMLManager getOPMLManager() { return oman; }

    /** Channels are leafs in the OPML tree, return the associated
     * channel object if there is any.
     */
    public Channel getChannel() { return channel; }

    public final String getAttribute(String name, String defaultValue) {
        NamedNodeMap atts = node.getAttributes();
        Node att = atts.getNamedItem(name);
        return (att == null) ? defaultValue : att.getNodeValue();
    }


    public void setAttribute(String namespaceURI, String name, String value) {
        oman.setAttribute(node, namespaceURI, name, value);
    }


    public String getAttributeNS(String namespaceURI, String name, String defaultValue) {
        return oman.getAttributeNS(node, namespaceURI, name, defaultValue);
    }

    public String getTitle() { return title; }

    public List<Outline> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return (children == null) ?
            false :
            ( children.size() > 0 );
    }

    @Override
    public String toString() { return getTitle(); }

    /** Find child outline nodes and add them to the children list.
     */
    private void buildChildren() {
        NodeList childNodes = node.getChildNodes();
        children = new LinkedList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().equals("outline")) {
                children.add(new Outline(oman, childNode));
            }
        }
    }
} // end class Outline
