import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.Novinar;

@DisplayName("Test the OPML manager")
class OmanTest
    extends BaseTest
{
    @BeforeEach
    void setup() {
        configure();
    }

    @Test
    void loadOPMLData() {
        assertNotNull(novinar.getRootOutline());
        assertNotNull(novinar.getChannels());
    }

    @Test
    void channelIdGeneration() {
        for (Channel chan : novinar.getChannels()) {
            System.out.println("channel: " + chan);
        }
        assertEquals(novinar.getChannelCounter(),
                     novinar.getChannels().size());
    }

    @Test
    void treeTraversal()
        throws Exception
    {
        // calculate total number of items in a "linear way"
        int linear_items_count = 0;
        for (Channel chan : novinar.getChannels()) {
            linear_items_count += novinar.getNewsItemsFor(chan).size();
        }

        // now get the number of items for the root outline
        int traverse_items_count = novinar.getNewsItemsFor(novinar.getRootOutline()).size();

        // they must match
        assertEquals(linear_items_count, traverse_items_count);
    }
}
