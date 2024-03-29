package org.bb.vityok.novinar.feed;

import java.text.ParseException;
import java.text.ParsePosition;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.bb.vityok.novinar.core.Channel;
import org.bb.vityok.novinar.core.Novinar;

/** Base class for concrete feed parsers. */
// ./gradlew test --tests FeedReaderTest
public abstract class FeedParser
{
    // Dublin core xmlns
    public static final String DC_NS = "http://purl.org/dc/elements/1.1/";
    public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RSS_RDF_NS = "http://purl.org/rss/1.0/";
    public static final String SYN_NS = "http://purl.org/rss/1.0/modules/syndication/";
    public static final String CONTENT_NS = "http://purl.org/rss/1.0/modules/content/";
    public static final String FEEDBURNER_NS = "http://rssnamespace.org/feedburner/ext/1.0";

    protected Novinar novinar;

    public FeedParser(Novinar novinar) {
        this.novinar = novinar;
    }

    /** Check if the given document can be parsed by this parser.
     *
     * @arg doc DOM representation of the feed contents.
     * @return true if the given document can be processed by this parser.
     * @see processFeed
     */
    public abstract boolean accepts(Document doc) throws Exception;
    public abstract void processFeed(Channel chan, Document doc) throws Exception;

    /** Set of timestamp parser patterns.
     *
     * <p>Malformed timestamps happen in the real world feeds all the time.
     *
     * <p>Patterns with manually-specified time-zones had to be added
     * to the list. Since precise time-stamp time-zone is not as
     * important as the fixed time-stamp itself, UTC has been chosen
     * as default.
     */
    public static final DateTimeFormatter TIMESTAMP_FORMATS[] = {
    		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
    		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
    		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC),
    		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz"), // 2018-04-30T12:00:00+00:00
    		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC), // fails: 2018-10-29 15:58:00
    		// RFC_1123 date-time with a zone name other than GMT
    		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z"),
    		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z"),
    		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy H:m:s z"), // Tue, 23 Oct 2018 9:21:17 CEST
    		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm Z"),
    		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm z"),
    		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm").withZone(ZoneOffset.UTC),
    		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy").withZone(ZoneOffset.UTC),
    		DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a").withZone(ZoneOffset.UTC),
    		// there are web-sites that specify AM-PM part of day,
    		// but the hour value is actually in the 24 hours
    		// range, like: 04/25/2018 13:42 PM. Following
    		// pattern tries to fix this
    		DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm a").withZone(ZoneOffset.UTC),
    		DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    		DateTimeFormatter.ISO_OFFSET_DATE_TIME,
    		DateTimeFormatter.ISO_ZONED_DATE_TIME,
    		DateTimeFormatter.ISO_DATE_TIME,
    		DateTimeFormatter.ISO_INSTANT,
    		DateTimeFormatter.RFC_1123_DATE_TIME
    };

    private final static String ALL_DAY = " (All day)";

    public static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");


    /** Attempts to parse the given timestamp using TIMESTAMP_FORMATS.
     *
     * @return a Instant object upon a success, null otherwise.
     */
    public static Instant parseTimestamp(String timestamp) {
	String ts = timestamp.trim();

	if (timestamp.endsWith(ALL_DAY) && ts.length() > ALL_DAY.length()) {
	    // if the date-time is incomplete and only date is present
	    try {
		ts = ts.substring(0, ts.length() - ALL_DAY.length());
		LocalDate ld = LocalDate.parse(ts, DAY_FORMAT);
		Instant iTs = ld.atStartOfDay().toInstant(ZoneOffset.UTC);
		return iTs;
	    } catch (DateTimeParseException pe) {
		pe.printStackTrace();
	    }
	} else {
	    for (DateTimeFormatter tsFormat : TIMESTAMP_FORMATS) {
		try {
		    Instant iTs = tsFormat.parse(ts, Instant::from);
		    return iTs;
		} catch (DateTimeParseException pe) {
		    continue;
		}
	    }
	}
	return null;
    }

    /**
     * Attempt extraction of creator/author information from the channel item.
     *
     * @param item
     * @return
     */
    public String extractCreator(Element item) {
	NodeList authors = item.getElementsByTagName("author");
        if (authors.getLength() > 0) {
            return authors.item(0).getTextContent();
        }

        NodeList creators = item.getElementsByTagName("creator");
        if (creators.getLength() > 0) {
            return creators.item(0).getTextContent();
        }

        NodeList dcCreators = item.getElementsByTagName("dc:creator");
        if (dcCreators.getLength() > 0) {
            return dcCreators.item(0).getTextContent();
        }

        NodeList nsCreators = item.getElementsByTagNameNS(DC_NS, "creator");
        if (nsCreators.getLength() > 0) {
            return nsCreators.item(0).getTextContent();
        }

        return null;
    }
}
