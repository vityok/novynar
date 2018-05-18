import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.Novinar;
import org.bb.vityok.novinar.Outline;

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


    @Test
    void outlinePropertyHandling()
        throws Exception
    {
        final Outline root = novinar.getRootOutline();
        final String propA = "update-on-boot";
        final String propB = "update-frequency";

        assertNull(root.getProperty(propA));
        root.setProperty(propA, "yes");
        assertNotNull(root.getProperty(propA));
        assertTrue("yes".equals(root.getProperty(propA)));

        assertNull(root.getProperty(propB));
        assertTrue("no".equals(root.getProperty(propB, "no")));
        root.setProperty(propB, "no");
        assertTrue("no".equals(root.getProperty(propB)));
        root.setProperty(propB, "yes");
        assertTrue("yes".equals(root.getProperty(propB)));
    }
}
