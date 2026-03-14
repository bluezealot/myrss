package com.rssmonitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpmlLoaderTest {

    @Test
    void testLoadFeedsFromValidOpml(@TempDir Path tempDir) throws Exception {
        // Create a test OPML file
        File opmlFile = tempDir.resolve("test.opml").toFile();
        try (FileWriter writer = new FileWriter(opmlFile)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<opml version=\"2.0\">\n");
            writer.write("<head><title>Test</title></head>\n");
            writer.write("<body>\n");
            writer.write("<outline text=\"Feed1\" xmlUrl=\"http://example.com/feed1.xml\" />\n");
            writer.write("<outline text=\"Feed2\" xmlUrl=\"http://example.com/feed2.xml\" />\n");
            writer.write("</body>\n");
            writer.write("</opml>\n");
        }

        OpmlLoader loader = new OpmlLoader();
        List<String> feeds = loader.loadFeeds(opmlFile.getAbsolutePath());

        assertEquals(2, feeds.size());
        assertTrue(feeds.contains("http://example.com/feed1.xml"));
        assertTrue(feeds.contains("http://example.com/feed2.xml"));
    }

    @Test
    void testLoadFeedsFromNonExistentFile() {
        OpmlLoader loader = new OpmlLoader();
        List<String> feeds = loader.loadFeeds("/nonexistent/path/file.opml");
        assertTrue(feeds.isEmpty());
    }
}
