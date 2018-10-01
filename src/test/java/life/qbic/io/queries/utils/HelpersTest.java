package life.qbic.io.queries.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class HelpersTest {

    @Test
    public void isNotOmicsRun() {
        assertEquals(false, Helpers.isOmicsRun("Q_WF_NGS_BLUB_RUN"));
        assertEquals(false, Helpers.isOmicsRun("Q_NGS_BLUB"));

    }

    @Test
    public void isOmicsRun() {
        assertEquals(true, Helpers.isOmicsRun("Q_MA_BLUB_RUN"));
        assertEquals(true, Helpers.isOmicsRun("Q_NGS_BLUB_RUN"));

    }
}