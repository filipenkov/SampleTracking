package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.locator.NodeLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebTable;
import org.w3c.dom.Node;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

/**
 * Used to make assertions on the Web table for the View User Directories page.
 *
 * @since v4.3
 */
public class UserDirectoryTable 
{
    private final WebTable table;
    private final Assertions assertions;

    public UserDirectoryTable(final FuncTestCase funcTestCase)
    {
        table = new TableLocator(funcTestCase.getTester(), "directory-list").getTable();
        assertions = funcTestCase.getAssertions();
        // check the Header Row
        assertions.getTableAssertions().assertTableRowEqualsCollapsed(table, 0, new String[] {"ID", "Directory Name", "Type", "Order", "Operations"});

    }

    public TableCell getTableCell(int row, int column)
    {
        return table.getTableCell(row, column);
    }

    public RowAssertions assertRow(final int rowNum)
    {
        return new RowAssertions(rowNum);
    }

    public class RowAssertions
    {
        private int rowNum;

        public RowAssertions(final int rowNum)
        {
            this.rowNum = rowNum;
        }

        public RowAssertions contains(final String id, final String name, final String type)
        {
            // Do the assertion
            assertions.getTableAssertions().assertTableRowEqualsCollapsed(table, rowNum, new String[] {id, name, type, null, null});
            // return this for fluidity
            return this;
        }

        public RowAssertions hasMoveUp(final boolean enabled)
        {
            // Find the Order cell (ie with move Up and Move Down buttons)
            final Node cellNode = table.getTableCell(rowNum, 3).getDOM();
            final String cellText = new NodeLocator(cellNode).getHTML();

            if (enabled)
            {
                assertTrue("Enabled Move Up button expected. Found '" + cellText + "'", cellText.contains("<SPAN class=\"icon icon-move-up\">Move Up</SPAN>"));
            }
            else
            {
                assertTrue("Disabled Move Up button expected. Found '" + cellText + "'", cellText.contains("<SPAN class=\"icon icon-move-up-disabled\">Move Up</SPAN>"));
            }
            // return this for fluidity
            return this;
        }

        public RowAssertions hasMoveDown(final boolean enabled)
        {
            // Find the Order cell (ie with move Up and Move Down buttons)
            final Node cellNode = table.getTableCell(rowNum, 3).getDOM();
            final String cellText = new NodeLocator(cellNode).getHTML();

            if (enabled)
            {
                assertTrue("Enabled Move Down button expected. Found '" + cellText + "'", cellText.contains("<SPAN class=\"icon icon-move-down\">Move Down</SPAN>"));
            }
            else
            {
                assertTrue("Disabled Move Down button expected. Found '" + cellText + "'", cellText.contains("<SPAN class=\"icon icon-move-down-disabled\">Move Down</SPAN>"));
            }
            // return this for fluidity
            return this;
        }

        public void hasOnlyEditOperation()
        {
            final String cellText = table.getCellAsText(rowNum, 4);
            assertTrue("Edit operation not found. Found: '" + cellText + "'.", cellText.contains("Edit"));
        }

        public void hasDisableEditOperations()
        {
            final String cellText = table.getCellAsText(rowNum, 4);
            assertTrue("Disable operation not found. Found: '" + cellText + "'.", cellText.contains("Disable"));
            assertFalse("Enable operation found but not expected.", cellText.contains("Enable"));
            assertTrue("Edit operation not found. Found: '" + cellText + "'.", cellText.contains("Edit"));
            assertFalse("Synchronise operation found but not expected.", cellText.contains("Synchronise"));
        }

        public void hasDisableEditSynchroniseOperations()
        {
            final String cellText = table.getCellAsText(rowNum, 4);
            assertTrue("Disable operation not found. Found: '" + cellText + "'.", cellText.contains("Disable"));
            assertFalse("Enable operation found but not expected.", cellText.contains("Enable"));
            assertTrue("Edit operation not found. Found: '" + cellText + "'.", cellText.contains("Edit"));
            hasSynchroniseOperation();
        }

        public void hasEnableEditRemoveSynchroniseOperations()
        {
            final String cellText = table.getCellAsText(rowNum, 4);
            assertTrue("Enable operation not found. Found: '" + cellText + "'.", cellText.contains("Enable"));
            assertFalse("Disable operation found but not expected.", cellText.contains("Disable"));
            assertTrue("Edit operation not found. Found: '" + cellText + "'.", cellText.contains("Edit"));
            assertTrue("Remove operation not found. Found: '" + cellText + "'.", cellText.contains("Remove"));
            hasSynchroniseOperation();
        }

        public void hasEnableEditRemoveOperations()
        {
            final String cellText = table.getCellAsText(rowNum, 4);
            assertTrue("Enable operation not found. Found: '" + cellText + "'.", cellText.contains("Enable"));
            assertFalse("Disable operation found but not expected.", cellText.contains("Disable"));
            assertTrue("Edit operation not found. Found: '" + cellText + "'.", cellText.contains("Edit"));
            assertTrue("Remove operation not found. Found: '" + cellText + "'.", cellText.contains("Remove"));
            assertFalse("Synchronise operation found but not expected.", cellText.contains("Synchronise"));
        }

        private void hasSynchroniseOperation()
        {
            String cellText = table.getCellAsText(rowNum, 4);
            if (cellText.contains("Synchronising for"))
            {
                // Synchronise in progress - wait a while
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                cellText = table.getCellAsText(rowNum, 4);
            }
            assertTrue("Synchronise operation not found. Found: '" + cellText + "'.", cellText.contains("Synchronise"));
        }
    }
}
