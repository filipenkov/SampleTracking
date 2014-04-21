package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.searchers.util.WorkRatioSearcherConfig;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class UpgradeTask_Build92 extends AbstractReindexUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build92.class);

    private static final String DATE_AFTER_SUFFIX = ":after";
    private static final String DATE_BEFORE_SUFFIX = ":before";
    private static final String DATE_PREVIOUS_SUFFIX = ":previous";
    private static final String DATE_NEXT_SUFFIX = ":next";

    private static final List<Upgrade92Helper> conversions = Arrays.asList(new Upgrade92Helper("/searchrequest/parameter/workratiomin", DocumentConstants.ISSUE_WORKRATIO, DocumentConstants.ISSUE_WORKRATIO + WorkRatioSearcherConfig.MIN_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/workratiomax", DocumentConstants.ISSUE_WORKRATIO, DocumentConstants.ISSUE_WORKRATIO + WorkRatioSearcherConfig.MAX_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/createdAfter", DocumentConstants.ISSUE_CREATED, DocumentConstants.ISSUE_CREATED + DATE_AFTER_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/createdBefore", DocumentConstants.ISSUE_CREATED, DocumentConstants.ISSUE_CREATED + DATE_BEFORE_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/createdPrevious", DocumentConstants.ISSUE_CREATED, DocumentConstants.ISSUE_CREATED + DATE_PREVIOUS_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/updatedAfter", DocumentConstants.ISSUE_UPDATED, DocumentConstants.ISSUE_UPDATED + DATE_AFTER_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/updatedBefore", DocumentConstants.ISSUE_UPDATED, DocumentConstants.ISSUE_UPDATED + DATE_BEFORE_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/updatedPrevious", DocumentConstants.ISSUE_UPDATED, DocumentConstants.ISSUE_UPDATED + DATE_PREVIOUS_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/duedateAfter", DocumentConstants.ISSUE_DUEDATE, DocumentConstants.ISSUE_DUEDATE + DATE_AFTER_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/duedateBefore", DocumentConstants.ISSUE_DUEDATE, DocumentConstants.ISSUE_DUEDATE + DATE_BEFORE_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/duedatePrevious", DocumentConstants.ISSUE_DUEDATE, DocumentConstants.ISSUE_DUEDATE + DATE_PREVIOUS_SUFFIX),
                                                           new Upgrade92Helper("/searchrequest/parameter/duedateNext", DocumentConstants.ISSUE_DUEDATE, DocumentConstants.ISSUE_DUEDATE + DATE_NEXT_SUFFIX));

    private final GenericDelegator delegator;

    public UpgradeTask_Build92(ApplicationProperties applicationProperties, IssueIndexManager indexManager, GenericDelegator genericDelegator)
    {
        super(applicationProperties, indexManager);
        this.delegator = genericDelegator;
    }

    public String getBuildNumber()
    {
        return "92";
    }

    public String getShortDescription()
    {
        return "Upgrades the search requests to use the new format for dates and work ratios. Also upgrade projectparameter to be multi-valued";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        List requests = delegator.findAll("SearchRequest");
        SAXReader xmlReader = new SAXReader();

        XPath projectIdxPath = DocumentHelper.createXPath("/searchrequest/parameter/projid");

        for (Iterator iterator1 = requests.iterator(); iterator1.hasNext();)
        {
            GenericValue searchRequestGv = (GenericValue) iterator1.next();
            String xml = searchRequestGv.getString("request");
            Reader stringReader = new StringReader(xml);
            Document doc = xmlReader.read(stringReader);

            // Upgrade the field name changes
            for (Upgrade92Helper conversion : conversions)
            {
                XPath xpathSelector = DocumentHelper.createXPath(conversion.getxPath());
                List results = xpathSelector.selectNodes(doc);
                for (Iterator iter = results.iterator(); iter.hasNext();)
                {
                    Element element = (Element) iter.next();
                    element.setName(conversion.getNewXmlName());
                    element.addAttribute("name", conversion.getNewName());
                }
            }

            // Upgrade the project param changes
            List projectIdNodes = projectIdxPath.selectNodes(doc);
            for (Iterator iter = projectIdNodes.iterator(); iter.hasNext();)
            {
                Element projectIdNode = (Element) iter.next();
                if (projectIdNode.element("value") == null)
                {
                    Element value = projectIdNode.addElement("value");
                    value.setText(projectIdNode.attribute("value").getText());

                    projectIdNode.addAttribute("andQuery", "false");
                    projectIdNode.addAttribute("name", null);
                    projectIdNode.addAttribute("value", null);
                }
            }


            searchRequestGv.setString("request", doc.asXML());
            searchRequestGv.store();
            log.info("Upgraded search request " + searchRequestGv.getString("name"));
        }

    }

    public static final class Upgrade92Helper
    {
        final String xPath;
        final String newXmlName;
        final String newName;

        public Upgrade92Helper(String xPath, String newXmlName, String newName)
        {
            this.xPath = xPath;
            this.newXmlName = newXmlName;
            this.newName = newName;
        }

        public String getxPath()
        {
            return xPath;
        }

        public String getNewXmlName()
        {
            return newXmlName;
        }

        public String getNewName()
        {
            return newName;
        }
    }
}


