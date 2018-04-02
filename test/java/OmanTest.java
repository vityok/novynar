import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bb.vityok.novinar.Channel;
import org.bb.vityok.novinar.OPMLManager;

@DisplayName("Test the OPML manager")
class OmanTest {

    public static final File opmlFile = new File("test/resources/opml-file.opml");

    @BeforeEach
    void setup() {
        OPMLManager oman = OPMLManager.getInstance();
        oman.loadConfig(opmlFile);
    }

    @Test
    void loadOPMLData() {
        OPMLManager oman = OPMLManager.getInstance();
        assertNotNull(oman.getDocument());
        assertNotNull(oman.getRootOutline());
        assertNotNull(oman.getChannels());
    }

    @Test
    void channelIdGeneration() {
        OPMLManager oman = OPMLManager.getInstance();
        for (Channel chan : oman.getChannels()) {
            System.out.println("channel: " + chan);
        }
            
        // there is only one channel in the OPML file
        assertEquals(oman.getChannels().size(), 1);
        assertEquals(oman.getChannelCounter(), oman.getChannels().size());
    }
}
