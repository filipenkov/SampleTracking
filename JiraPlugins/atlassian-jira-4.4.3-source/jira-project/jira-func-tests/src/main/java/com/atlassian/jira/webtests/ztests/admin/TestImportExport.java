package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Test to see that importing an xml file into JIRA and then exporting from it results in an XML file
 * Author: christo
 * $Id: TestImportExport.java,v 1.4 2006/10/17 05:56:56 cmountford Exp $
 *
 * Uses the following xml files:
 * TestImportExport.xml
 * TestImportExport2.xml
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.IMPORT_EXPORT })
public class TestImportExport extends JIRAWebTest
{

    public TestImportExport(String name)
    {
        super(name);
    }

    public void testXmlImportFromNonImportDirectory() throws Exception
    {
        File data = new File(getEnvironmentData().getXMLDataLocation(), "EmptyJira.xml");

        // write new data to temp file
        File newData = File.createTempFile("testXmlImportFromNonImportDirectory", ".xml"); //This will be created in the /tmp directory
        try
        {
            FileUtils.copyFile(data, newData);

            tester.gotoPage("secure/admin/XmlRestore!default.jspa");
            tester.setWorkingForm("jiraform");
            tester.setFormElement("filename", newData.getAbsolutePath());
            tester.submit();

            tester.assertTextPresent("Could not find file at this location");
        }
        finally
        {
            newData.delete();
        }
    }

    public void testXmlImportWithInvalidIndexDirectory() throws Exception
    {
        //By creating a file for the index path, we'll force the failure of the index path directory creation
        File indexPath = File.createTempFile("testXmlImportWithInvalidIndexDirectory", null);
        indexPath.createNewFile();
        indexPath.deleteOnExit();

        try {
            administration.restoreDataWithReplacedTokens("TestSetupInvalidIndexPath.xml", EasyMap.build("@@INDEX_PATH@@", indexPath.getAbsolutePath()));
        }
        catch(AssertionError e) {
            //We're expecting an AssertionError here to inform us that the import was not successful
        }
        tester.assertTextPresent("Cannot write to index directory. Check that the application server and JIRA have permissions to write to: " + indexPath.getAbsolutePath());
    }

    public void testXmlImportWithInvalidAttachmentsDirectory() throws Exception
    {
        //By creating a file for the index path, we'll force the failure of the index path directory creation
        File attachmentPath = File.createTempFile("testXmlImportWithInvalidAttachmentsDirectory", null);
        attachmentPath.createNewFile();
        attachmentPath.deleteOnExit();

        try {
            administration.restoreDataWithReplacedTokens("TestSetupInvalidAttachmentPath.xml", EasyMap.build("@@ATTACHMENT_PATH@@", attachmentPath.getAbsolutePath()));
        }
        catch(AssertionError e) {
            //We're expecting an AssertionError here to inform us that the import was not successful
        }
        tester.assertTextPresent("Cannot write to attachment directory. Check that the application server and JIRA have permissions to write to: " + attachmentPath.getAbsolutePath());
    }

    public void testXmlImportFromFuture()
    {
        File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/" + "TestXmlImportFromFuture.xml");
        copyFileToJiraImportDirectory(file);
        gotoPage("secure/admin/XmlRestore!default.jspa");
        setWorkingForm("jiraform");
        setFormElement("filename", file.getName());
        setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        submit();
        administration.waitForRestore();
        assertTextNotPresent("Your project has been successfully imported");
        assertTextPresent("The xml data you are trying to import seems to be from a newer version of JIRA. This will not work.");
    }

    public void testXmlImportWithAV1LicenseInIt() throws Exception
    {
        File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/" + "oldlicense.xml");
        copyFileToJiraImportDirectory(file);
        gotoPage("secure/admin/XmlRestore!default.jspa");
        setWorkingForm("jiraform");
        setFormElement("filename", file.getName());
        submit();
        administration.waitForRestore();
        text.assertTextPresent(new WebPageLocator(tester), "Please upgrade your license or generate an evaluation license.");
    }

    private void runTestImportExport(String fileBaseName) throws Exception
    {
        String importFilename = fileBaseName + ".xml";
        String exportFilename = fileBaseName + "_out.xml";

        // if we update the build number the test will break unless we first
        // import it and re-export it to the import file name.
        restoreData(importFilename);
        getAdministration().exportDataToFile(importFilename);
        // now we can import that data knowing it has the current build number
        restoreData(importFilename);
        // and export it to a different filename to see that export recreates the imported file
        getAdministration().exportDataToFile(exportFilename);

        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.transform.TransformerFactory",
                "org.apache.xalan.processor.TransformerFactoryImpl");

        DifferenceListener acceptable = new DifferenceListener()
        {

            public int differenceFound(Difference difference)
            {
                // ignore differences in sequence number ids
                Node control = difference.getControlNodeDetail().getNode();
                if (attrMatch(control, "id", "ListenerConfig"))
                {
                    return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }

                boolean listenerConfigSequenceValueItem = attrMatch(control, "seqId", "SequenceValueItem")
                        && attrHasSibling(control, "seqName", "ListenerConfig");
                if (listenerConfigSequenceValueItem)
                {
                    return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }

                // ignore license differences
                boolean osPropertyString = attrMatch(control, "value", "OSPropertyString");

                if (osPropertyString && (attrHasSibling(control, "id", "10012")
                            || attrHasSibling(control, "id", "10013")))
                {
                    return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }

                return RETURN_ACCEPT_DIFFERENCE;
            }

            public void skippedComparison(Node control, Node test)
            {
                // don't care
            }

        };

        Reader imported = new FileReader(new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath(), importFilename));
        Reader exported = new FileReader(new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath(), exportFilename));
        try
        {
            DetailedDiff diff = new DetailedDiff(new Diff(imported, exported));
            diff.overrideDifferenceListener(acceptable);
            diff.overrideElementQualifier(new ElementNameAndTextQualifier());
            assertTrue(diff.toString(), diff.similar());

        }
        finally
        {
            closeReader(imported);
            closeReader(exported);
        }
    }

    private void closeReader(Reader r)
    {
        try
        {
            r.close();
        }
        catch(IOException ioe)
        {
            log(ioe.getLocalizedMessage());
        }
    }

    /**
     * Returns true iff node is attr and owner has attr with given name and value.
     */
    private boolean attrHasSibling(Node node, String attrName, String attrValue)
    {
        if (node.getNodeType() != Node.ATTRIBUTE_NODE) {
            return false;
        }

        Attr attr = ((Attr)node).getOwnerElement().getAttributeNode(attrName);
        return (attr != null) && attr.getNodeValue().equals(attrValue);
    }

    /**
     * Returns true iff n has name, is attr and has owner name as specified.
     */
    private boolean attrMatch(Node n, String name, String ownerElementName)
    {
        return n.getNodeName().equals(name)
                && n.getNodeType() == Node.ATTRIBUTE_NODE
                && ((Attr)n).getOwnerElement().getNodeName().equals(ownerElementName);
    }
}
