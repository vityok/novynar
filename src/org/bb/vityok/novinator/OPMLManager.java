package org.bb.vityok.novinator;

import java.io.File;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Managing the OPML file that defines the subscribed feeds and their
 * parameters.
 */
public class OPMLManager
{
    private final static OPMLManager instance = new OPMLManager();

    public static final String DEFAULT_CONFIG_FILE = "backup.opml";

    private Document doc;

    protected OPMLManager() { }
    public static OPMLManager getInstance() { return instance; }

    /** Represents an OPML Outline with the minimal set of
     * features.
     */
    public class Outline {
	private String url;
	private String title;
	private int id;
	List<Outline> getChildren() { return null; }
    }

    
    public void loadConfig() {
	try {
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    doc = db.parse(new File(DEFAULT_CONFIG_FILE));
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
}
