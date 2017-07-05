package org.bb.vityok.novinar;

import java.io.File;

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
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.bb.vityok.novinator.db.ChannelDAO;


/** Managing the OPML file that defines the subscribed feeds and their
 * parameters.
 *
 * It essentially a DAO of the feeds tree and their configuration,
 * interfacing the application with both the XML/OPML file defining
 * the tree and the database backend containing everything else.
 */
public class OPMLManager
{
    private final static OPMLManager instance = new OPMLManager();

    public static final String DEFAULT_CONFIG_FILE = "backup.opml";

    public static final String NOVINAR_NS = "https://bitbucket.org/vityok/novinar";

    private Document doc;
    private Outline rootOutline;

    protected OPMLManager() { }
    public static OPMLManager getInstance() { return instance; }

    /** Represents an OPML Outline with the minimal set of
     * features.
     */
    public class Outline {
	private String url;
	private String title;
	private int id;
	private Node node;
	private List<Outline> children = null;
	private Channel channel;

	/** Binds the outline with the associated DOM node. */
	public Outline(Node node) {
	    this.node = node;
	    this.title = getAttribute("text", "N/A");
	    this.url = getAttribute("xmlUrl", null);
	    this.id = 0;
	    buildChildren();

	    if (hasChildren()) {
		String idStr = getAttributeNS(NOVINAR_NS, "channelId", null);
		if (idStr == null) {
		    this.id = ChannelDAO.getInstance().createChannelFor(url);
		    setAttribute(NOVINAR_NS, "channelId", Integer.toString(id));
		} else {
		    this.id = Integer.valueOf(idStr);
		}
	    }
	}

	private String getAttribute(String name, String defaultValue) {
	    NamedNodeMap atts = node.getAttributes();
	    Node att = atts.getNamedItem(name);
	    return (att == null) ? defaultValue : att.getNodeValue();
	}

	private void setAttribute(String namespaceURI, String name, String value) {
	    NamedNodeMap atts = node.getAttributes();
	    Attr attr = doc.createAttributeNS(namespaceURI, name);
	    attr.setValue(value);
	    atts.setNamedItemNS(attr);
	}


	private String getAttributeNS(String namespaceURI, String name, String defaultValue) {
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
		children.size() > 0;
	}

	public String toString() { return getTitle(); }

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


    /** Returns first child node having specified name. */
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
	try {
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    doc = db.parse(new File(DEFAULT_CONFIG_FILE));
	    Node bodyNode = doc.getElementsByTagName("body").item(0);
	    Node rootOutlineNode = getChildByName(bodyNode, "outline");
	    rootOutline = new Outline(rootOutlineNode);
	    System.out.println("Loaded OPML config from " + DEFAULT_CONFIG_FILE);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    public void storeConfig() {
	try {
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    Result output = new StreamResult(new File(DEFAULT_CONFIG_FILE));
	    Source input = new DOMSource(doc);

	    transformer.transform(input, output);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public Outline getRootOutline() { return rootOutline; }
}
