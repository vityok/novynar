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
import org.bb.vityok.novinar.Novinar;

import org.bb.vityok.novinar.db.NewsItemDAO;


/** Download news feed and send it to the appropriate parser (Atom,
 * RSS, RSS+RDF, etc).
 */
public class FeedReader
{

    private Novinar novinar;

    public FeedReader(Novinar novinar)
    {
        this.novinar = novinar;
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

	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(con.getInputStream());
	    doc.getDocumentElement().normalize();

	    //optional, but recommended
	    //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	    System.out.println("Root element :[" + doc.getDocumentElement().getNodeName() + "]");
	    if ("rdf:RDF".equals(doc.getDocumentElement().getNodeName())) {
		System.out.println("Processing RDF feed");
		RDF.getInstance().processFeed(chan, doc, novinar);
	    }
            chan.updatedNow();
	    return doc;
	} else {
	    return null;
	}
    }

    /** Refresh/download all feeds from all known channels. */
    public void loadFeeds()
	throws Exception
    {
        List<Channel> channels = novinar.getChannels();
        System.out.println("loading " + channels.size() + " channels");
        for (Channel channel : channels) {
	    loadFeed(channel);
        }
    }
}
