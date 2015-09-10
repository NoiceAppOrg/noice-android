package com.noice.noice.util;


import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class URIHelperTest {

    @Test
    public void testExtractYoutubeId() throws Exception {
        String id = URIHelper.extractYoutubeId("https://www.youtube.com/watch?v=jaPn5pXtkuI");
        assertEquals("Id should extract correctly", "jaPn5pXtkuI",
                id);
    }
}
