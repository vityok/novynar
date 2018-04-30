package org.bb.vityok.novinar.feed;

import java.util.Calendar;
import java.util.Date;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;

import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.Novinar;

/** Base class for concrete feed parsers. */
public abstract class FeedParser
{
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

    public static final SimpleDateFormat TIMESTAMP_FORMATS[] = { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                                                                 new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
                                                                 new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
                                                                 new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzzzz"), // 2018-04-30T12:00:00+00:00
                                                                 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                                                                 new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z"),
                                                                 new SimpleDateFormat("MM/dd/yyyy hh:mm aa") // 04/25/2018 13:42 PM

    };

    /** Attempts to parse the given timestamp using TIMESTAMP_FORMATS.
     *
     * @return a Calendar object upon a success, null otherwise.
     */
    public Calendar parseTimestamp(String timestamp) {
        for (int i = 0; i < TIMESTAMP_FORMATS.length; i++) {
            ParsePosition pp = new ParsePosition(0);
            TIMESTAMP_FORMATS[i].setLenient(true);
            Date date = TIMESTAMP_FORMATS[i].parse(timestamp, pp);
            if (date != null && pp.getErrorIndex() < 0) {
                Calendar ts= new Calendar.Builder()
                    .setInstant(date)
                    .build();
                return ts;
            }
        }
        return null;
    }
}
