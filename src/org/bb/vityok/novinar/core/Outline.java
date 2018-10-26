package org.bb.vityok.novinar.core;

import java.util.List;
import java.util.LinkedList;

import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMException;


/** Represents an OPML Outline with the minimal set of features
 * required for handling the feed information.
 */
public class Outline
{
    public static final String A_TEXT = "text";
    public static final String A_XML_URL = "xmlUrl";

    public static final String A_PROPERTY = "property";
    public static final String Q_PROPERTY = OPMLManager.Q_NOVINAR + A_PROPERTY;

    public static final String A_KEY = "key";
    public static final String Q_KEY = OPMLManager.Q_NOVINAR + A_KEY;

    public static final String P_IGNORE_ON_BOOT = "ignore-on-boot";
    public static final String P_UPDATE_PERIOD = "update-period";


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
        this.title = getAttribute(A_TEXT, "N/A");
        String url = getAttribute(A_XML_URL, null);

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

    /** Root outline doesn't have a parent.
     *
     * @return true if this is a root outline.
     */
    public boolean isRoot() {
	return getParent() == null;
    }

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
        return OPMLManager.getAttributeNS(elt, namespaceURI, name, defaultValue);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        oman.setAttribute(elt, A_TEXT, title);
    }

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

    private Node getPropertyNode(String key) {
        // we expect that the list of properties will be "flat" ie. we
        // can query all descendant property elements ignoring more
        // complex hierarchy
        try {
            NodeList properties = getNode().getElementsByTagNameNS(OPMLManager.NOVINAR_NS,
                                                                   A_PROPERTY);
            for (int i = 0; i < properties.getLength(); i++) {
                Node propNode = properties.item(i);
                String propKey = OPMLManager.getAttributeNS(propNode, OPMLManager.NOVINAR_NS, A_KEY, "");
                if (propKey.equals(key)) {
                    return propNode;
                }
            }
        } catch (DOMException de) {
            Novinar.getLogger().log(Level.SEVERE, "failure in the OPML structure", de);
        }
        return null;
    }

    /** Properties are stored as child elements under the given
     * outline.
     *
     * Element has tag name "property" and namespace defined by
     * OPMLManager.NOVINAR_NS constant.
     *
     * For example, a property can define whether the given channel
     * should be checked during startup, or how frequently it should
     * be checked, maybe some other useful information.
     *
     * @todo
     */
    public String getProperty(String key) {
        Node propNode = getPropertyNode(key);
        if (propNode != null) {
            return propNode.getTextContent();
        } else {
            return null;
        }
    }


    /** Returns value of the given property or default value if it is
     * not present.
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value == null) ? defaultValue : value;
    }


    /** @todo */
    public void setProperty(String key, String value) {
        Node propNode = getPropertyNode(key);
        if (propNode != null) {
            // property with the same key already exists
            propNode.setTextContent(value);
        } else {
            // create a new child node to store this property value
            Node newPropNode = elt.getOwnerDocument().createElementNS(OPMLManager.NOVINAR_NS, Q_PROPERTY);
            oman.setAttribute(newPropNode, OPMLManager.NOVINAR_NS, Q_KEY, key);
            newPropNode.setTextContent(value);
            elt.appendChild(newPropNode);
        }
    }

    public boolean getIgnoreOnBoot() {
        Boolean b = Boolean.valueOf(getProperty(Outline.P_IGNORE_ON_BOOT, "false"));
        return b.booleanValue();
    }

    public void setIgnoreOnBoot(boolean flag) {
        setProperty(Outline.P_IGNORE_ON_BOOT, flag ? "true" : "false");
    }

    public UpdatePeriod getUpdatePeriod() {
        String str = getProperty(Outline.P_UPDATE_PERIOD, null);
        if (str == null) {
            return UpdatePeriod.DEFAULT_UPDATE_PERIOD;
        }
        return UpdatePeriod.fromString(str);
    }

    public void setUpdatePeriod(UpdatePeriod p) {
        setProperty(Outline.P_UPDATE_PERIOD, p.getCode());
    }
} // end class Outline
