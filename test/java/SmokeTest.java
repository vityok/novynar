import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bb.vityok.novinar.OPMLManager;

@DisplayName("Smoke Test: check if everything works")
class SmokeTest {

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
}
