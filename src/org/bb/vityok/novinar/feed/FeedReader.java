package org.bb.vityok.novinar.feed;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.OPMLManager;
import org.bb.vityok.novinar.db.ChannelDAO;


/** Download news feed and send it to the appropriate parser (Atom,
 * RSS, RSS+RDF, etc).
 */
public class FeedReader
{
    public static final FeedReader INSTANCE = new FeedReader();

    public URL DEFAULT_URL;

    protected FeedReader()
    {
	DEFAULT_URL = null;
	try {
	    DEFAULT_URL = new URL("http://rss.slashdot.org/Slashdot/slashdotMain");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static FeedReader getInstance() {
	return INSTANCE;
    }

    public Document loadFeed(Channel chan)
	throws Exception
    {
        System.out.println("loading items for the channel: " + chan);
        
	URL feedURL = new URL(chan.getLink());
	HttpURLConnection con = (HttpURLConnection) feedURL.openConnection();

	int responseCode = con.getResponseCode();
	System.out.println("\nSending 'GET' request to URL : " + feedURL);
	System.out.println("Response Code : " + responseCode);

	if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

	    // Map<String,List<String>> headersMap = con.getHeaderFields();

	    // con.getHeaderFields().forEach((k,v)->System.out.println("Header : " + k + " Value : " + v));

	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(con.getInputStream());
	    doc.getDocumentElement().normalize();

	    //optional, but recommended
	    //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	    System.out.println("Root element :[" + doc.getDocumentElement().getNodeName() + "]");
	    if ("rdf:RDF".equals(doc.getDocumentElement().getNodeName())) {
		System.out.println("Processing RDF feed");
		RDF.getInstance().processFeed(chan, doc);
	    }

	    return doc;
	} else {
	    return null;
	}
    }

    public void loadFeeds()
	throws Exception
    {
	ChannelDAO cdao = ChannelDAO.getInstance();
	if (cdao.getChannelsCount() == 0) {
	    // empty db, let's put our default feed
	    // cdao.createChannelFor(DEFAULT_URL.toString());
	}

	List<Channel> channels = ChannelDAO.getInstance().getAllChannels();
	for (Channel channel : channels) {
	    loadFeed(channel);
	}
    }

}
