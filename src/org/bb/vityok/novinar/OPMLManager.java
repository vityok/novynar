package org.bb.vityok.novinar;

import java.io.File;

import java.text.SimpleDateFormat;

import java.util.List;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** Managing the OPML file that defines the subscribed feeds and their
 * parameters.
 *
 * It essentially a DAO of the feeds tree and their configuration,
 * interfacing the application with the XML/OPML file.
 */
public class OPMLManager
{
    public static final String DEFAULT_OPML_FILE_NAME = "backup.opml";

    public static final String NOVINAR_NS = "https://bitbucket.org/vityok/novinar";
    public static final String A_CHANNEL_ID = "channelId";
    public static final String Q_CHANNEL_ID = "novinar:" + A_CHANNEL_ID;

    public static final String A_CHANNEL_COUNTER = "channelCounter";
    public static final String Q_CHANNEL_COUNTER = "novinar:" + A_CHANNEL_COUNTER;

    public static final String A_LAST_UPDATED = "lastUpdated";
    public static final String Q_LAST_UPDATED = "novinar:" + A_LAST_UPDATED;

    public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private Document doc;
    private Node rootOutlineNode;
    private Outline rootOutline;
    private File opmlFile;

    /** List of feed channels defined in the OPML file.
     *
     * Instead of traversing the OPML tree in search of Channel data
     * maintain this list.
     */
    private List<Channel> channels = new LinkedList<>();


    public OPMLManager() {
        opmlFile = new File(DEFAULT_OPML_FILE_NAME);
        loadConfig();
    }
    public OPMLManager(String configFile) {
        opmlFile = new File(configFile);
        loadConfig();
    }

    /** Returns first child node of the given node with the specified
     * name.
     */
    public Node getChildByName(Node node, String name) {
	NodeList children = node.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if (child.getNodeName().equals(name)) {
		return child;
	    }
	}
	return null;
    }


    /** Parse the configuration XML-OPML file and reconstruct the
     * Outline-Channels tree with relevant configuration from the
     * DOM.
     */
     public void loadConfig() {
         loadConfig(opmlFile);
     }

    public void loadConfig(File configFile) {
        channels = new LinkedList<>();
	try {
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    doc = db.parse(configFile);

            Element rootNode = doc.getDocumentElement();
            rootNode.setAttributeNS("http://www.w3.org/2000/xmlns/",
                                    "xmlns:novinar", NOVINAR_NS);

	    Node bodyNode = doc.getElementsByTagName("body").item(0);
	    rootOutlineNode = getChildByName(bodyNode, "outline");
	    rootOutline = new Outline(this, rootOutlineNode, null);
	    Novinar.getLogger().info("Loaded OPML config from " + configFile.getPath());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /** Store OPML data to the DEFAULT_OPML_FILE. */
    public void storeConfig() {
        storeConfig(opmlFile);
    }


    /** Store OPML data to the given configFile. */
    public void storeConfig(File configFile) {
	try {
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    Result output = new StreamResult(configFile);
	    Source input = new DOMSource(doc);

	    transformer.transform(input, output);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public Outline getRootOutline() { return rootOutline; }

    public Document getDocument() { return doc; }

    public void setAttribute(Node node, String name, String value) {
        NamedNodeMap atts = node.getAttributes();
        Attr attr = getDocument().createAttribute(name);
        attr.setValue(value);
        atts.setNamedItemNS(attr);
    }

    public void setAttribute(Node node, String namespaceURI, String name, String value) {
        NamedNodeMap atts = node.getAttributes();
        Attr attr = getDocument()
            .createAttributeNS(namespaceURI, name);
        attr.setValue(value);
        atts.setNamedItemNS(attr);
    }

    /** Given a Node and an attribute name, return its contents or
     * default value.
     */
    public static String getAttribute(Node node, String name, String defaultValue) {
        NamedNodeMap atts = node.getAttributes();
        Node att = atts.getNamedItem(name);
        return (att == null) ? defaultValue : att.getNodeValue();
    }

    public static String getAttributeNS(Node node, String namespaceURI, String name, String defaultValue) {
        NamedNodeMap atts = node.getAttributes();
        Node att = atts.getNamedItemNS(namespaceURI, name);
        return (att == null) ? defaultValue : att.getNodeValue();
    }

    public List<Channel> getChannels () {
        return channels;
    }

    public void addChannel(Channel chan) {
        channels.add(chan);
    }

    /** Adds a new channel under the given Outline. */
    public Outline appendChannel(Outline ol, String url, String title) {
        Node newChannelNode = doc.createElement("outline");

        setAttribute(newChannelNode, "xmlUrl", url);
        setAttribute(newChannelNode, "text", title);
        ol.getNode().appendChild(newChannelNode);

        // channelId will be generated in the Channel constructor,
        // that will be called from the Outline when Outline realises
        // it is a channel node
        Outline newOl = new Outline(this, newChannelNode, ol);
        ol.addChildOutline(newOl);
        return newOl;
    }

    /** Adds a new folder under the given Outline. */
    public Outline appendFolder(Outline ol, String name) {
        Node newFolderNode = doc.createElement("outline");

        setAttribute(newFolderNode, "text", name);
        ol.getNode().appendChild(newFolderNode);

        Outline newOl = new Outline(this, newFolderNode, ol);
        ol.addChildOutline(newOl);
        return newOl;
    }

    /** Returns the current value of the channels counter. */
    public int getChannelCounter () {
        String cntrStr = getAttributeNS(rootOutlineNode, NOVINAR_NS, A_CHANNEL_COUNTER, "0");
        return Integer.valueOf(cntrStr);
    }

    /** Generates and stores next channel id.
     *
     * The corresponding counter is stored as attribute in the root
     * outline node.
     */
    public int genChannelId() {
        int cntr = getChannelCounter() + 1;
        setAttribute(rootOutlineNode,
                     NOVINAR_NS,
                     Q_CHANNEL_COUNTER,
                     Integer.toString(cntr));
        return cntr;
    }

    /** Removes the given entry from the OPML tree. */
    public void removeEntry(Outline ol) {
        Outline parent = ol.getParent();
        parent.removeChildOutline(ol);
    }
}
