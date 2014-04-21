/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import junit.framework.Assert;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;

public abstract class AbstractTestWorkflowMigration extends JIRAWebTest
{
    protected static final String WORKFLOW_FIELD_ID = "Workflow";
    protected static final String STATUS_FIELD_ID = "Status";
    protected static final String CUSTOM_STATUS_1 = "Custom Status 1";
    protected static final String CUSTOM_STATUS_2 = "Custom Status 2";
    protected static final String CUSTOM_STATUS_3 = "Custom Status 3";
    protected static final String CUSTOM_STATUS_4 = "Custom Status 4";
    protected static final String RESOLVED_STATUS_NAME = "Resolved";
    protected static final String CLOSED_STATUS_NAME = "Closed";
    protected static final String IN_PROGRESS_STATUS_NAME = "In Progress";
    private Assertions assertions;

    public AbstractTestWorkflowMigration(String name)
    {
        super(name);        
    }

    /** Ensure the issue is in the expected status and has the correct workflow actions available */
    protected void assertIssuesWorkflowState(String issueKey, String statusName, Collection availableWorkflowActionNames)
            throws SAXException
    {
        gotoIssueChangeHistory(issueKey);
        assertIssueStatus(statusName);
        assertAvailableWorkflowActions(availableWorkflowActionNames);
    }

    private void gotoIssueChangeHistory(String issueKey)
    {
        if (issueKey == null)
        {
            throw new IllegalArgumentException("IssueKey should not be null.");
        }

        beginAt("/browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel");
    }

    private void assertAvailableWorkflowActions(Collection names) throws SAXException
    {
        WebTable table = getDialog().getResponse().getTableWithID("available_workflow_actions");

        if (names != null && !names.isEmpty())
        {
            List actions = new ArrayList();
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@class='ops-cont']//a");
            final Node[] nodes = xPathLocator.getNodes();
            for (Node node : nodes)
            {
                final Node id = node.getAttributes().getNamedItem("id");
                if (id != null)
                {
                    final String nodeText = DomKit.getCollapsedText(id).trim();
                    if (nodeText.startsWith("action_id"))
                    {
                        actions.add(DomKit.getCollapsedText(node));
                    }
                }
            }

            assertEquals(new HashSet(names), new HashSet(actions));
        }
        else
        {
            // Ensure that the table does not exist if there should be no available workflow actions
            assertNull(table);
        }
    }

    private void assertIssueStatus(String statusName)
    {
        text.assertTextPresent(new IdLocator(tester, "status-val"), statusName);

    }


    protected void assertLastChangeHistoryRecords(String issueKey, ExpectedChangeHistoryRecord
            expectedChangeHistoryRecord) throws SAXException
    {
        getAssertions().assertLastChangeHistoryRecords(issueKey, expectedChangeHistoryRecord);
    }

    protected void assertLastChangeHistoryRecords(String issueKey, List expectedChangeHistoryRecords)
            throws SAXException
    {
        getAssertions().assertLastChangeHistoryRecords(issueKey, expectedChangeHistoryRecords);
    }

    protected Assertions getAssertions()
    {
        //need to initialize the assertions framework lazily as it checks if jira is setup and performs a login.
        if(assertions == null)
        {
            FuncTestHelperFactory funcTestHelperFactory = new FuncTestHelperFactory(this, getEnvironmentData());
            assertions = funcTestHelperFactory.getAssertions();
        }
        return assertions;
    }

    protected void assertTwoDimentionalPortlet(Long portletId, String portletTitle, String xAxisHeading, String yAxisHeading, String[] xAxisHeadings, String[][] data, boolean totalsShown)
            throws SAXException
    {
        String url = getDialog().getResponse().getURL().toString();
        // If we are not on the dashboard already, go there
        if (!url.endsWith("/secure/Dashboard.jspa"))
        {
            gotoDashboard();
        }

        WebTable table = getDialog().getResponse().getTableWithID("portlet_" + portletId);

        assertNotNull("Could not find a 2D gadget with id '" + portletId + "'.", table);

        // Ensure title is correct
        String title = table.getCellAsText(0, 0);
        assertNotNull(title);
        title = title.trim();
        assertTrue(title.startsWith("Statistics Table:"));
        assertTrue(title.indexOf(portletTitle) > -1);

        // Ensure the axes headings are correct
        String axes = table.getCellAsText(1, 0);
        assertNotNull(axes);
        axes = axes.trim();
        assertTrue(axes.startsWith(xAxisHeading));
        assertTrue(axes.indexOf(yAxisHeading) > -1);

        // Assert The X Axis headings are there

        Collection xAxisHeadingsList = new ArrayList(Arrays.asList(xAxisHeadings));

        if (totalsShown)
        {
            // If we have totals enabled then add the totals heading
            xAxisHeadingsList.add("T:");
        }

        // Start at cell in column 1 (not 0) as we the (0,0) cell contains Axis headings
        int i = 1;
        for (Iterator iterator = xAxisHeadingsList.iterator(); iterator.hasNext();)
        {
            String xHeading = (String) iterator.next();
            String cellText = table.getCellAsText(1, i);
            assertNotNull(cellText);
            assertEquals(xHeading, cellText.trim());
            i++;
        }

        int columnNumber = -1;
        for (int j = 0; j < data.length; j++)
        {
            String[] rowData = data[j];
            columnNumber = rowData.length;

            String yHeading = rowData[0];

            for (int columnIndex = 0; columnIndex < rowData.length; columnIndex++)
            {
                String xHeading;
                if (columnIndex == 0)
                {
                    xHeading = "Y-Axis Headings";
                }
                else if (columnIndex < (rowData.length - 1))
                {
                    xHeading = xAxisHeadings[columnIndex - 1];
                }
                else
                {
                    xHeading = "Totals Column";
                }

                String expectedCellText = rowData[columnIndex];
                int rowIndex = j + 2;
                String cellText = table.getCellAsText(rowIndex, columnIndex);

                // Check if we are in the heading of the totals row
                boolean lastRow = (j == (data.length - 1));
                if (totalsShown && lastRow && (columnIndex == 0))
                {
                    // If so just ensure that we start with the same string.
                    // This ensures that we do not care if ":" is part of the string or not
                    Assert.assertTrue("Totals row heading in cell with co-ordinates (" + rowIndex + ", " + columnIndex + ") for (" + yHeading + ", " + xHeading + ") does not start with '" + expectedCellText + "'",
                            cellText.trim().startsWith(expectedCellText));
                }
                else
                {
                    // Find out the x and y axis heading's of the expected cell
                    Assert.assertEquals("Cell text in cell with co-ordinates (" + rowIndex + ", " + columnIndex + ") for (" + yHeading + ", " + xHeading + ") is not equal to '" + expectedCellText + "'", expectedCellText, cellText.trim());
                }
            }
        }

        // Assert that the number of rows is correct
        int rowNumber = data.length + 2;
        Assert.assertEquals("Ensure the 2D statistics gadget has " + rowNumber + " rows", rowNumber, table.getRowCount());
        // Assert that the number of columns is correct
        Assert.assertEquals("Ensure the 2D statistics gadget has " + columnNumber + " columns", columnNumber, table.getColumnCount());
    }
}
