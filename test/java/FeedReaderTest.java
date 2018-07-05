import java.io.File;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.Novinar;
import org.bb.vityok.novinar.NewsItem;

import org.bb.vityok.novinar.feed.RSS;

// To run only this test:
//
// ./gradlew test --tests *FeedReaderTest

@DisplayName("Test the FeedReader")
class FeedReaderTest
    extends BaseTest
{
    @BeforeEach
    void setup() {
        configure();
    }

    /*    @Test */
    void updateFeeds()
        throws Exception
    {
        novinar.loadFeeds();
        for (Channel chan : novinar.getChannels()) {
            System.out.println("channel: " + chan);
            List<NewsItem> items = novinar.getNewsItemsFor(chan);
            assertTrue(items.size() > 0);
            // make sure that at least each item has a non-null and
            // not empty mandatory attributes
            items.forEach((item) -> {
                    assertNotNull(item.getTitle());
                    assertFalse(item.getTitle().isEmpty());
                    assertNotNull(item.getLink());
                    assertFalse(item.getLink().isEmpty());
                    assertNotNull(item.getDescription());
                    assertFalse(item.getDescription().isEmpty());
                });
        }
    }

    @Test
    void parseTimeStamps()
    {
        assertNotNull(RSS.parseTimestamp("Fri, 25 May 2018 10:20:32 PDT"));
        assertNotNull(RSS.parseTimestamp("Wed, 30 May 2018 01:00:25 PDT"));
        assertNotNull(RSS.parseTimestamp("Tue, 28 Nov 2017 03:00 EST"));
        assertNotNull(RSS.parseTimestamp("Wed, 09 May 2018 (All day)"));
	assertNotNull(RSS.parseTimestamp("Tue, 03 Jul 2018 8:14:20 CEST"));
	assertNotNull(RSS.parseTimestamp("2018-07-02 10:52:00"));
        assertNotNull(RSS.parseTimestamp("05/30/2018 20:41 PM"));
	assertNotNull(RSS.parseTimestamp("06/29/2018 12:57 PM"));
    }
}
