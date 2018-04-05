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
}
