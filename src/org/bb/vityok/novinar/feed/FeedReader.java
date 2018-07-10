package org.bb.vityok.novinar.feed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import java.util.List;
import java.util.LinkedList;

import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.Novinar;
import org.bb.vityok.novinar.UpdatePeriod;

import org.bb.vityok.novinar.db.NewsItemDAO;


/** Download news feed and send it to the appropriate parser (Atom,
 * RSS, RSS+RDF, etc).
 */
public class FeedReader
    extends Thread
{

    private Novinar novinar;
    private List<FeedParser> parsers;

    public FeedReader(Novinar novinar)
    {
        super("Feeds reader thread");
        this.novinar = novinar;
        parsers = new LinkedList<>();
        parsers.add(new RDF(novinar));
        parsers.add(new RSS(novinar));
        parsers.add(new Atom(novinar));
    }

    /** Opens connection to a remote resource and returns the input
     * stream.
     */
    public InputStream openRemoteFeed(String url)
        throws Exception
    {
        String location = url;
        URL base, next;
        int attempts = 0;
        while (attempts++ < 5) {
            URL feedURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) feedURL.openConnection();

            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            con.setRequestProperty("User-Agent", "Novinar RSS feed reader");
            con.setInstanceFollowRedirects(true);

            int responseCode = con.getResponseCode();
            Novinar.getLogger().info("\nSending 'GET' request to URL : " + feedURL);
            Novinar.getLogger().info("Response Code : " + responseCode);

            // this approach in combination with
            // con.setInstanceFollowRedirects(true) allows to follow
            // redirects from HTTP to HTTPS locations. See:
            // https://stackoverflow.com/questions/1884230/urlconnection-doesnt-follow-redirect
            switch (con.getResponseCode())
                {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    location = con.getHeaderField("Location");
                    location = URLDecoder.decode(location, "UTF-8");
                    base     = new URL(url);
                    next     = new URL(base, location);  // Deal with relative URLs
                    url      = next.toExternalForm();
                    continue;
                case HttpURLConnection.HTTP_OK:
                    InputStream is = con.getInputStream();
                    return is;
                }
        }
        return null;
    }

    /** Opens input stream from the local file (for testing purposes
     * primarily).
     */
    public InputStream openLocalFeed(String path)
        throws Exception
    {
        File file = new File(path);
        return new FileInputStream(file);
    }

    public Document loadFeed(Channel chan)
	throws Exception
    {
        Novinar.getLogger().info("loading items for the channel: " + chan);
        InputStream is = null;
        String url = chan.getLink();
        URL feedURL = new URL(url);

        try {
            switch (feedURL.getProtocol())
                {
                case "file":
                    {
                        is = openLocalFeed(feedURL.getPath());
                        break;
                    }
                case "http":
                case "https":
                    {
                        is = openRemoteFeed(url);
                        break;
                    }
                }

            // Input stream for reading feed data obtained, handle it
            if (is == null) {
                Novinar.getLogger().severe("FeedReader failed to open: " + url);
                chan.touch();
                return null;
            } else {
                // Parse XML data into a DOM document/tree
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setNamespaceAware(true);
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(is);

                // optional, but recommended read this:
                // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                doc.getDocumentElement().normalize();

                Novinar.getLogger().fine("Root element :[" + doc.getDocumentElement().getNodeName() + "]");
                for (FeedParser parser : parsers) {
                    if (parser.accepts(doc)) {
                        Novinar.getLogger().info("Processing feed with " + parser);
                        parser.processFeed(chan, doc);
                        is.close();
                        chan.touch();
                        return doc;
                    }
                }

                Novinar.getLogger().severe("FeedReader doesn't know how to handle this type of feeds. Inspect: " + url);
                is.close();
            }
        } catch (Exception e) {
            Novinar.getLogger().log(Level.SEVERE, "failed to parse feed for channel: " + chan, e);
            if (is != null) {
                is.close();
            }
        }
        chan.touch();
        return null;
    } // end loadFeed


    /** Refresh/download all feeds from all known channels. */
    public void loadFeeds()
	throws Exception
    {
        novinar.setStatus(Novinar.Status.READING_FEEDS);
        List<Channel> channels = novinar.getChannels();
        Novinar.getLogger().info("loading " + channels.size() + " channels");
        for (Channel channel : channels) {
	    loadFeed(channel);
        }
        novinar.setStatus(Novinar.Status.READY);
    }

    // todo: the thread must hang in background, periodically checking
    // for the channels that have to be updated
    public void run() {
        Novinar.getLogger().info("FeedReader thread is running");
        boolean firstRound = true;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Instant nextRound = Instant.now().plus(UpdatePeriod.DEFAULT_UPDATE_PERIOD.getDuration());
                Channel nextRoundChannel = null;

                List<Channel> channels = novinar.getChannels();

                for (Channel channel : channels) {
                    Instant lastUpdate = channel.getLatestUpdate();
                    UpdatePeriod updatePeriod = channel.getUpdatePeriod();

                    // when this channel should be updated?
		    if (updatePeriod != UpdatePeriod.NEVER) {
			Instant whenUp = lastUpdate.plus(updatePeriod.getDuration());

			Novinar.getLogger().info("channel: " + channel + " must be updated at: " + whenUp);
			if ( (firstRound && (! channel.getIgnoreOnBoot()))
			     || whenUp.isBefore(Instant.now())) {
			    loadFeed(channel);
			}

			Instant nextUpdate = Instant.now().plus(updatePeriod.getDuration());
			if (nextUpdate.isBefore(nextRound)) {
			    nextRound = whenUp;
			    nextRoundChannel = channel;
			}
		    }
                } // finished processing channels

                firstRound = false;
                Instant now = Instant.now();
                Duration toSleep = Duration.between(now, nextRound);
                Novinar.getLogger().info("sleeping for: " + toSleep + " (now is: " + now
                                         + " will wake up at: " + nextRound + ")"
                                         + " to update: " + nextRoundChannel);
                Thread.currentThread().sleep(toSleep.toMillis());
            } catch (InterruptedException ie) {
                Novinar.getLogger().log(Level.INFO, "FeedReader thread got interrupted");
                return;
            } catch (Exception e) {
                Novinar.getLogger().log(Level.SEVERE, "failed while loading feeds", e);
            }
        }
        Novinar.getLogger().info("FeedReader thread stopped");
    }
}
