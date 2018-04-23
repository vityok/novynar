import java.io.File;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.Novinar;
import org.bb.vityok.novinar.NewsItem;

@DisplayName("Test user interaction with the Novinar core")
class ActionsTest
    extends BaseTest
{
    @BeforeEach
    void setup() {
        configure();
    }

    /** Test that an item marked as "removed" is removed from the
     * default listings of news items for a channel. */
    @Test
    void removeItem()
        throws Exception
    {
        novinar.loadFeeds();
        for (Channel chan : novinar.getChannels()) {
            List<NewsItem> items = novinar.getNewsItemsFor(chan);
            int begin_size = items.size();
            novinar.removeNewsItem(items.get(0));
            int new_size = novinar.getNewsItemsFor(chan).size();
            assertEquals(begin_size, (new_size + 1));
        }
    }
}
