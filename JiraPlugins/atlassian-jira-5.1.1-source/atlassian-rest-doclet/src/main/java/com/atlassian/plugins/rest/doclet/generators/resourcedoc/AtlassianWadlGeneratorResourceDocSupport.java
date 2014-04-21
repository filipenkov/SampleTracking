package com.atlassian.plugins.rest.doclet.generators.resourcedoc;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.generators.resourcedoc.WadlGeneratorResourceDocSupport;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ResourceDocType;
import com.sun.research.ws.wadl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This class generates the WADL description of rest resources and considers the rest plugin module descriptors
 * configured inside the atlassian-plugin.xml file when generating the resource path.
 *
 * It builds up a map that contains a mapping of a package name to a resource path.
 * The full resource path is concatenated of the following strings:
 * 1) path as configured for rest plugin module descriptor: e.g. api
 * 2) version as configured for rest plugin module descriptor e.g. 2.0.alpha1
 * 3) path of the rest end point e.g. worklog
 * 
 * e.g. /api/2.0.alpha1/worklog/
 */
public class AtlassianWadlGeneratorResourceDocSupport extends WadlGeneratorResourceDocSupport
{
    private HashMap<String, ResourcePathInformation> resourcePathInformation;

    private static final Logger LOG = LoggerFactory.getLogger(AtlassianWadlGeneratorResourceDocSupport.class);
    private static final String ATLASSIAN_PLUGIN_XML = "atlassian-plugin.xml";

    public AtlassianWadlGeneratorResourceDocSupport()
    {
        super();
    }

    public AtlassianWadlGeneratorResourceDocSupport(WadlGenerator wadlGenerator, ResourceDocType resourceDoc)
    {
        super(wadlGenerator, resourceDoc);
    }

    @Override
    public void init() throws Exception
    {
        super.init();
        parseAtlassianPluginXML();
    }

    private void parseAtlassianPluginXML()
    {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);

        try
        {
            final URL resource = getClass().getClassLoader().getResource(ATLASSIAN_PLUGIN_XML);
            if (resource == null) return;
            LOG.info("Found " + ATLASSIAN_PLUGIN_XML + " file! Looking for rest plugin module descriptors...");

            DocumentBuilder db = dbf.newDocumentBuilder();

            resourcePathInformation = new HashMap<String, ResourcePathInformation>();

            final Document document = db.parse(resource.toExternalForm());
            final NodeList restPluginModuleDescriptors = document.getElementsByTagName("rest");
            final int numPluginModuleDescriptors = restPluginModuleDescriptors.getLength();
            LOG.info("Found " + numPluginModuleDescriptors + " rest plugin module descriptors.");

            for (int i = 0; i < numPluginModuleDescriptors; i++)
            {
                final Node node = restPluginModuleDescriptors.item(i);

                final NamedNodeMap attributes = node.getAttributes();
                final Node pathItem = attributes.getNamedItem("path");
                final Node versionItem = attributes.getNamedItem("version");
                if (pathItem == null || versionItem == null)
                    continue;

                String resourcePath = pathItem.getNodeValue();
                String version = versionItem.getNodeValue();

                LOG.info("Found rest end point with path '" + resourcePath + "' and version '" + version + "'");

                //Remove leading slash
                if (resourcePath.indexOf("/") != -1)
                {
                    resourcePath = resourcePath.substring(resourcePath.indexOf("/") + 1);
                }

                final NodeList list = node.getChildNodes();
                for (int j = 0; j < list.getLength(); j++)
                {
                    final Node child = list.item(j);
                    if (child.getNodeName().equals("package"))
                    {
                        final String packageName = child.getFirstChild().getNodeValue();
                        LOG.info("Map package '" + packageName + "' to resource path '" + resourcePath + "' and version '" + version + "'");
                        resourcePathInformation.put(packageName, new ResourcePathInformation(resourcePath, version));
                    }
                }
            }
        }
        catch (Exception ex)
        {
            LOG.error("Failed to read " + ATLASSIAN_PLUGIN_XML + " and parse rest plugin module descriptor information. Reason", ex);
        }
    }

    @Override
    public Resource createResource(AbstractResource r, String path)
    {
        final Resource result = super.createResource(r, path);
        boolean resourcePathChanged = false;
        for (String packageName : resourcePathInformation.keySet())
        {
            if (r.getResourceClass().getPackage().getName().startsWith(packageName))
            {
                final ResourcePathInformation pathInformation = resourcePathInformation.get(packageName);
                final String newPath = pathInformation.getPath() + "/" + pathInformation.getVersion() + "/" + result.getPath();
                result.setPath(newPath);
                resourcePathChanged = true;
                LOG.info("Setting resource path of rest end point '" + r.getResourceClass().getCanonicalName() + "' to '" + newPath + "'");
                break;
            }
        }
        if (!resourcePathChanged)
        {
            LOG.info("Resource path of rest end point '" + r.getResourceClass().getCanonicalName() + "' unchanged no mapping to rest plugin module descriptor found.");
        }

        return result;
    }

    public class ResourcePathInformation
    {
        private final String path;
        private final String version;

        public ResourcePathInformation(String path, String version)
        {
            this.path = path;
            this.version = version;
        }

        public String getVersion()
        {
            return version;
        }

        public String getPath()
        {
            return path;
        }
    }

}
