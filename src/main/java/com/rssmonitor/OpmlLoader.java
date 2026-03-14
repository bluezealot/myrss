package com.rssmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OpmlLoader {
    private static final Logger logger = LoggerFactory.getLogger(OpmlLoader.class);

    public List<String> loadFeeds(String opmlPath) {
        List<String> feedUrls = new ArrayList<>();
        
        try {
            File opmlFile = new File(opmlPath);
            if (!opmlFile.exists()) {
                logger.error("OPML file not found: {}", opmlPath);
                return feedUrls;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(opmlFile);
            doc.getDocumentElement().normalize();

            NodeList outlineNodes = doc.getElementsByTagName("outline");
            
            for (int i = 0; i < outlineNodes.getLength(); i++) {
                Element outline = (Element) outlineNodes.item(i);
                String xmlUrl = outline.getAttribute("xmlUrl");
                
                if (xmlUrl != null && !xmlUrl.isEmpty()) {
                    feedUrls.add(xmlUrl);
                    logger.debug("Found feed URL: {}", xmlUrl);
                }
            }
            
            logger.info("Loaded {} feed URLs from OPML file", feedUrls.size());
            
        } catch (Exception e) {
            logger.error("Error parsing OPML file: {}", e.getMessage(), e);
        }
        
        return feedUrls;
    }
}
