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

@DisplayName("Test the FeedReader")
class FeedReaderTest
    extends BaseTest
{
    @BeforeEach
    void setup() {
        configure();
    }

    @Test
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
}
