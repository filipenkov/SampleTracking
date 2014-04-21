package com.atlassian.jira.util;

import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.jaxen.JaxenException;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * This rough and ready parser can take a mvn2 POM file and extract information from it
 * in Java object form.
 */
public class PomParser {

    public PomModel parse(File pomFile) {
        try {
            Document document = readDom(pomFile);
            return convertToPomModel(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PomModel convertToPomModel(Document document) throws JaxenException {
        final List<PomModel.Dependency> dependencyList = new ArrayList<PomModel.Dependency>();

        final Map<String, String> pomProperties = readPomProperties(document);

        Element project = document.getRootElement();
        final String projectGroupId = getPlainText(project, pomProperties, "groupId");
        final String projectArtifactId = getPlainText(project, pomProperties, "artifactId");
        final String projectVersion = getPlainText(project, pomProperties, "version");
        final String projectName = getPlainText(project, pomProperties, "name");
        final String projectPackaging = getPlainText(project, pomProperties, "packaging");

        List<Element> elements = selectNodes(document, "//dependencies/dependency");
        for (Element e : elements) {

            final String groupId = getPlainText(e, pomProperties, "groupId");
            final String artifactId = getPlainText(e, pomProperties, "artifactId");
            final String version = getPlainText(e, pomProperties, "version");
            final Map<String, String> commentProperties = getCommentProperties(e);

            dependencyList.add(new PomModel.Dependency() {
                public String getGroupId() {
                    return groupId;
                }

                public String getArtifactId() {
                    return artifactId;
                }

                public String getVersion() {
                    return version;
                }

                public String getCommentProperty(String commentKey) {
                    return commentProperties.get(commentKey);
                }

                public Map<String, String> getCommentProperties() {
                    return commentProperties;
                }

                @Override
                public String toString() {
                    return new StringBuilder("groupId:").append(groupId).append(" artifactId:").append(artifactId)
                            .append(" version:").append(version).append(" comment properties:").append(commentProperties).toString();
                }
            });
        }
        return new PomModelImpl(dependencyList,pomProperties,projectGroupId,projectArtifactId,projectVersion,projectName,projectPackaging);
    }

    private String getPlainText(Element e, Map<String, String> pomProperties, String xpath) {
        Node node = e.selectSingleNode(xpath);
        if (node == null) {
            return null;
        }
        String text = node.getText().trim();
        for (String key : pomProperties.keySet()) {
            String variableName = "${" + key + "}";
            if (text.indexOf(variableName) != -1) {
                text = text.replace(variableName, pomProperties.get(key));
            }
        }
        return text;
    }

    private Map<String, String> readPomProperties(Document document) {
        Map<String, String> properties = new HashMap<String, String>();
        List<Element> elements = selectNodes(document,"//properties");
        for (Element element : elements) {
            @SuppressWarnings({"unchecked"})
            List<Element> namedElements = element.elements();
            for (Element namedE : namedElements) {
                properties.put(namedE.getName(), namedE.getTextTrim());
            }
        }
        return properties;
    }

    private Map<String, String> getCommentProperties(Element e) {
        @SuppressWarnings({"unchecked"})
        Iterator<Node> iterator = e.nodeIterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node instanceof Comment) {
                sb.append(node.getText());
            }
        }
        Properties p = new Properties();
        try {
            // parse as java properties
            p.load(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
        } catch (IOException e1) {
            // never gonna happen for UTF-8 and Strings
        }
        Map<String, String> commentMap = new HashMap<String, String>();
        for (Object key : p.keySet()) {
            commentMap.put(String.valueOf(key), String.valueOf(p.get(key)));
        }
        return commentMap;
    }

    @SuppressWarnings({"unchecked"})
    private List<Element> selectNodes(Node node, String xpath) {
        return node.selectNodes(xpath);
    }

    private Document readDom(File pomFile) throws SAXException, IOException, ParserConfigurationException {
        final org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomFile);
        final DOMReader reader = new DOMReader();
        return reader.read(doc);
    }

    private static class PomModelImpl implements PomModel
    {
        private final List<Dependency> dependencyList;
        private final Map<String, String> pomProperties;
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String name;
        private final String packaging;

        private PomModelImpl(final List<Dependency> dependencyList, final Map<String, String> pomProperties, final String groupId, final String artifactId, final String version, final String name, final String packaging)
        {
            this.dependencyList = dependencyList;
            this.pomProperties = pomProperties;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.name = name;
            this.packaging = packaging;
        }

        public List<PomModel.Dependency> getDependencies() {
            return dependencyList;
        }

        public Dependency getDependencyViaArtifactName(final String artifactName)
        {
            for (Dependency dependency : dependencyList)
            {
                if (artifactName.equals(dependency.getArtifactId()))
                {
                    return dependency;
                }
            }
            return null;
        }

        public Map<String, String> getProperties() {
            return pomProperties;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public String getName() {
            return name;
        }

        public String getPackaging() {
            return packaging;
        }
    }
}
