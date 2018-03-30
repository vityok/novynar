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

    /** List of feed channels defined in the OPML file */
    private List<Channel> channels = new LinkedList<Channel>();


    protected OPMLManager() { }
    public static OPMLManager getInstance() { return instance; }

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
            File configFile = new File(DEFAULT_CONFIG_FILE);
	    doc = db.parse(configFile);
	    Node bodyNode = doc.getElementsByTagName("body").item(0);
	    Node rootOutlineNode = getChildByName(bodyNode, "outline");
	    rootOutline = new Outline(rootOutlineNode);
	    System.out.println("Loaded OPML config from " + configFile.getPath());
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

    public Document getDocument() { return doc; }


    public List<Channel> getChannels () {
        return channels;
    }

    public void addChannel(Channel chan) {
        channels.add(chan);
    }

    /** Generates next channel id. The corresponding counter is stored
     * in an attribute in the root outline node. */
    public int genChannelId() {
        String cntrStr = rootOutline.getAttributeNS(NOVINAR_NS, "channelCounter", "0");
        int cntr = Integer.valueOf(cntrStr);
        cntr++;
        rootOutline.setAttribute(OPMLManager.NOVINAR_NS, "channelCounter", Integer.toString(cntr));
        return cntr;
    }
}
