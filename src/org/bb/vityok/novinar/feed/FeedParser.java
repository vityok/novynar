package org.bb.vityok.novinar.feed;

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
}
