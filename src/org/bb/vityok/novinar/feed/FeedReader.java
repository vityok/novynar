package org.bb.vityok.novinar.feed;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.time.Duration;
import java.time.Instant;

import java.util.List;
import java.util.LinkedList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.bb.vityok.novinar.core.Channel;
import org.bb.vityok.novinar.core.Novinar;
import org.bb.vityok.novinar.core.UpdatePeriod;

/**
 * Download news feed and send it to the appropriate parser (Atom, RSS, RSS+RDF,
 * etc).
 *
 * <p>
 * Updates channel information (last time updated, if there are any problems,
 * etc.)
 */
public class FeedReader extends Thread
{

    /**
     * Some feeds start generated feeds with the UTF8 byte-order mark
     * (BOM). It has to be manually discarded when the XML data is
     * passed as a stream of chars to the XML parser.
     */
    final public static char UTF8_BOM = '\uFEFF';

    private Novinar novinar;
    private List<FeedParser> parsers;

    private ExecutorService threadPool = Executors.newFixedThreadPool(3);

    public FeedReader(Novinar novinar) {
        super("Feeds reader thread");
        this.novinar = novinar;
        parsers = new LinkedList<>();
        parsers.add(new RDF(novinar));
        parsers.add(new RSS(novinar));
        parsers.add(new Atom(novinar));
    }

    /**
     * Opens connection to a remote resource and returns the input stream.
     */
    public InputStream openRemoteFeed(String url) throws Exception {
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
            switch (con.getResponseCode()) {
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
                location = con.getHeaderField("Location");
                location = URLDecoder.decode(location, "UTF-8");
                base = new URL(url);
                next = new URL(base, location); // Deal with relative URLs
                url = next.toExternalForm();
                continue;
            case HttpURLConnection.HTTP_OK:
                InputStream is = con.getInputStream();
                return is;
            }
        }
        return null;
    }

    /**
     * Opens input stream from the local file (for testing purposes primarily).
     */
    public InputStream openLocalFeed(String path) throws Exception {
        File file = new File(path);
        return new FileInputStream(file);
    }

    /**
     * Reads contents from the given input stream and returns a sanitized string.
     *
     * <p>
     * Some feeds contain invalid XML characters in CDATA sections. This method
     * ensures that no such characters are left and will not break the XML parser.
     */
    public String slurpAndFixInputStream(InputStream is)
        throws IOException
    {
        StringBuilder sb = new StringBuilder();

        // todo: what if the input stream is not UTF_8 encoded? which might happen if
        // the server is configured for a different encoding
        try (Reader reader = new BufferedReader(new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int codePoint = 0;
            boolean firstChar = true;
            char[] dst = new char[2];
            while ((codePoint = reader.read()) != -1) {
                boolean surrogate = false;
                // if (Character.isHighSurrogate((char)current)
                // && reader. && Character.isLowSurrogate(text.charAt(i + 1))) {
                // surrogate = true;
                // codePoint = text.codePointAt(i++);
                // } else {
                // see: https://stackoverflow.com/a/11672807
                Character.toChars(codePoint, dst, 0);
                // skip the BOM if there is any in the start of the stream
                if (firstChar && dst[0] == UTF8_BOM) {
                    firstChar = false;
                    continue;
                }
                firstChar = false;
                // }
                if ((codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD)
                    || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
                    || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                    || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {
                    sb.append(dst[0]);
                    if (surrogate) {
                        sb.append(dst[0]);
                    }
                }
            }
        }
        return sb.toString();
    }

    public Document loadFeed(Channel chan) throws Exception {
        Novinar.getLogger().info("loading items for the channel: " + chan);

        InputStream is = null;
        String url = chan.getLink();
        URL feedURL = new URL(url);

        try {
            switch (feedURL.getProtocol()) {
            case "file": {
                is = openLocalFeed(feedURL.getPath());
                break;
            }
            case "http":
            case "https": {
                is = openRemoteFeed(url);
                break;
            }
            default: {
                chan.setProblems("Invalid URL, could not detect protocol");
                return null;
            }
            }

            // Input stream for reading feed data obtained, handle it
            if (is == null) {
                Novinar.getLogger().severe("FeedReader failed to open: " + url);
                chan.touch();
                String problem = "Failed to open: " + url;
                chan.setProblems(problem);
                throw new FeedHandlingException(problem);
            } else {
                Document doc = parseFeedXml(is);

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
                        // reset existing state of the accumulated problems for this channel
                        chan.setProblems(null);
                        return doc;
                    }
                }
                is.close();

                Novinar.getLogger().severe("FeedReader doesn't know how to handle this type of feeds. Inspect: " + url);
                String problem = "FeedReader doesn't know how to handle this type of feeds.";
                chan.setProblems(problem);
                throw new FeedHandlingException(problem);
            }
        } catch (Exception e) {
            Novinar.getLogger().log(Level.SEVERE, "failed to parse feed for channel: " + chan, e);
            if (is != null) {
                is.close();
            }
            String problem = "Exception thrown while parsing feed for channel: " + e.getMessage();
            chan.setProblems(problem);
            throw new FeedHandlingException(problem);
        }
    } // end loadFeed

    public void submitLoadFeedTask(Channel chan)
        throws Exception
    {
        threadPool.execute(() -> {
                try {
                    if (!Thread.currentThread().isInterrupted()) {
                        loadFeed(chan);
                    }
                } catch (Exception e) {
                    Novinar.getLogger().severe("Problem loading channel " + chan);
                    String problem = "Problem loading channel";
                    chan.setProblems(problem);
                }
            });
    }

    /**
     * Attempt parsing the feed XML document in several different ways and check
     * which one works.
     *
     * <p>
     * The easiest way is to just pass the InputStream to the DocumentBuilder.parse
     * method. But this method doesn't offer a way to recover from invalid XML
     * characters (ie. in CDATA sections), which unfortunately might happen in the
     * wild.
     */
    private Document parseFeedXml(InputStream is)
        throws IOException, ParserConfigurationException, SAXException
    {
        String fixedStr = "n/a";
        try {
            // fixedStr = slurpAndFixInputStream(is);
            // final Reader feedReader = new StringReader(fixedStr);
            final InputSource feedReaderSource = new InputSource(is); // feedReader);
            // Novinar.getLogger().info("feed contents: " + fixedStr);

            // Parse XML data into a DOM document/tree
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(feedReaderSource);
            return doc;
        }
        catch (Exception e) {
            Novinar.getLogger().severe("failed to parse XML doc: begin[" + fixedStr + "]end");
            throw new IOException("failed to load and parse feed", e);
        }
    }

    /** Refresh/download all feeds from all known channels. */
    public void loadFeeds() throws Exception {
        novinar.setStatus(Novinar.Status.READING_FEEDS);
        List<Channel> channels = novinar.getChannels();
        Novinar.getLogger().info("loading " + channels.size() + " channels");
        for (Channel channel : channels) {
            submitLoadFeedTask(channel);
        }
        novinar.setStatus(Novinar.Status.READY);
    }

    /**
     * Entry point for the main background trhead periodically checking feeds and
     * downloading new items.
     *
     */
    // todo: the thread must hang in background, periodically checking
    // for the channels that have to be updated
    @Override
    public void run() {
        Novinar.getLogger().info("FeedReader thread is running");
        boolean firstRound = true;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Instant nextRound = Instant.now().plus(UpdatePeriod.DEFAULT_UPDATE_PERIOD.getDuration());
                Channel nextRoundChannel = null;

                List<Channel> channels = novinar.getChannels();

                for (Channel channel : channels) {
                    if (Thread.currentThread().isInterrupted()) {
                        Novinar.getLogger().info("FeedReader thread interrupted. Breaking the cycle");
                        return;
                    }
                    try {
                        Instant lastUpdate = channel.getLatestUpdate();
                        UpdatePeriod updatePeriod = channel.getUpdatePeriod();

                        // when this channel should be updated?
                        if (updatePeriod != UpdatePeriod.NEVER) {
                            Instant whenUp = lastUpdate.plus(updatePeriod.getDuration());

                            Novinar.getLogger().info("channel: " + channel + " must be updated at: " + whenUp);
                            if ((firstRound && (!channel.getIgnoreOnBoot())) || whenUp.isBefore(Instant.now())) {
                                submitLoadFeedTask(channel);
                            }

                            Instant nextUpdate = Instant.now().plus(updatePeriod.getDuration());
                            if (nextUpdate.isBefore(nextRound)) {
                                nextRound = whenUp;
                                nextRoundChannel = channel;
                            }
                        }
                    } catch (Exception e) {
                        Novinar.getLogger().log(Level.SEVERE, "failed to load channel" + channel, e);
                    }
                } // finished processing channels

                firstRound = false;
                Instant now = Instant.now();
                Duration toSleep = Duration.between(now, nextRound);
                Novinar.getLogger().info("sleeping for: " + toSleep + " (now is: " + now + " will wake up at: "
                                         + nextRound + ")" + " to update: " + nextRoundChannel);
                Thread.sleep(toSleep.toMillis());
            } catch (InterruptedException ie) {
                Novinar.getLogger().log(Level.INFO, "FeedReader thread got interrupted");
                return;
            } catch (Exception e) {
                Novinar.getLogger().log(Level.SEVERE, "failed while loading feeds", e);
            }
        }
        Novinar.getLogger().info("FeedReader thread stopped");
    }

    public void close() {
        threadPool.shutdown();
        interrupt();
    }
} // end FeedReader
