package org.bb.vityok.novinar;

import java.util.List;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** Represents an OPML Outline with the minimal set of features
 * required for handling the feed information.
 */
public class Outline
{
    private String title;
    /* DOM node that corresponds to this outline */
    private Element elt;

    /* Root outline has no parents and is null */
    private Outline parent = null;
    private List<Outline> children = null;

    /* Channel object if there is any, and there must be one if this
     * is a channel outline */
    private Channel channel = null;

    private OPMLManager oman;


    /** Binds the outline with the associated DOM node. */
    public Outline(OPMLManager oman, Node node, Outline parent) {
        this.elt = (Element) node;
        this.oman = oman;
        this.parent = parent;
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

    public Element getNode() { return elt; }
    public OPMLManager getOPMLManager() { return oman; }
    public Outline getParent() { return parent; }

    /** Channels are leafs in the OPML tree, return the associated
     * channel object if there is any.
     */
    public Channel getChannel() { return channel; }

    public final String getAttribute(String name, String defaultValue) {
        String attValue = elt.getAttribute(name);
        return (attValue == null || attValue.isEmpty()) ? defaultValue : attValue;
    }

    public void setAttribute(String namespaceURI, String name, String value) {
        oman.setAttribute(elt, namespaceURI, name, value);
    }

    public String getAttributeNS(String namespaceURI, String name, String defaultValue) {
        // todo: use Element.getAttributeNS method instead
        return oman.getAttributeNS(elt, namespaceURI, name, defaultValue);
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

    public boolean isFolder() {
        return channel == null;
    }

    public boolean isChannel() {
        return channel != null;
    }

    @Override
    public String toString() { return getTitle(); }


    /** Find child outline nodes and add them to the children list.
     */
    private void buildChildren() {
        NodeList childNodes = elt.getChildNodes();
        children = new LinkedList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if (childElement.getTagName().equals("outline")) {
                    addChildOutline(new Outline(oman, childNode, this));
                }
            }
        }
    }

    public void addChildOutline(Outline ol) {
        children.add(ol);
    }

    public void removeChildOutline(Outline ol) {
        children.remove(ol);
        elt.removeChild(ol.getNode());

    }

} // end class Outline
