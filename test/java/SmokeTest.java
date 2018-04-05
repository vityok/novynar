import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bb.vityok.novinar.Novinar;

@DisplayName("Smoke Test: check if everything works")
class SmokeTest
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
}
