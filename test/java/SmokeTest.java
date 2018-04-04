import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bb.vityok.novinar.Novinar;

@DisplayName("Smoke Test: check if everything works")
class SmokeTest {

    public static final File opmlFile = new File("test/resources/opml-file.opml");
    Novinar novinar = null;

    @BeforeEach
    void setup() {
        novinar = new Novinar(opmlFile, "testDB");
    }

    @Test
    void loadOPMLData() {
        assertNotNull(novinar.getRootOutline());
        assertNotNull(novinar.getChannels());
    }
}
