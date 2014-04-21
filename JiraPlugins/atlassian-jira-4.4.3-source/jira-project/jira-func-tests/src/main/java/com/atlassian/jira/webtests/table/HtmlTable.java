package com.atlassian.jira.webtests.table;

import com.atlassian.jira.functest.framework.util.text.TextKit;
import com.atlassian.jira.util.dbc.Assertions;
import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;

/**
 * Represents a HTML table found on a page in a test.
 *
 * This is basically a wrapper around the low-level WebTable.
 *
 * @since v4.0
 * @see WebTable
 */
public class HtmlTable
{
    private WebTable webTable;

    public HtmlTable(final WebTable webTable)
    {
        Assertions.notNull("webTable", webTable);
        this.webTable = webTable;
    }

    public static HtmlTable newHtmlTable(final WebTester tester, final String tableID)
    {
        WebTable webTable = tester.getDialog().getWebTableBySummaryOrId(tableID);
        return new HtmlTable(webTable);
    }

    public Row getRow(final int rowIndex)
    {
        return new Row(webTable, rowIndex);
    }

    /**
     * Finds the first row in this HtmlTable where the text in the given column equals {@code cellValue}.
     *
     * <p> Note that we trim the whitespace from the beginning and end of the cell value.
     *
     * @param columnIndex Index of the column that we are looking in for the given text.
     * @param cellValue The text we expect the cell to contain.
     * @return the first such row found, or null if none found.
     */
    public Row findRowWhereCellEquals(final int columnIndex, final String cellValue)
    {
        // find the row with the desired cellValue
        for (int rowNum = 0; rowNum < webTable.getRowCount(); rowNum ++)
        {
            String value = webTable.getCellAsText(rowNum, columnIndex);
            if (value != null && value.trim().equals(cellValue))
            {
                return getRow(rowNum);
            }
        }
        // No such row found.
        return null;
    }

    /**
     * Finds the first row in this HtmlTable where the text in the given column starts with {@code prefix}.
     *
     * <p> Note that we trim the whitespace from the beginning and end of the cell value. 
     *
     * @param columnIndex Index of the column that we are looking in for the given prefix.
     * @param prefix The prefix.
     * @return the first such row found, or null if none found.
     */
    public Row findRowWhereCellStartsWith(final int columnIndex, final String prefix)
    {
        // find the row with the desired cellValue
        for (int rowNum = 0; rowNum < webTable.getRowCount(); rowNum ++)
        {
            String value = webTable.getCellAsText(rowNum, columnIndex);
            if (value != null && value.trim().startsWith(prefix))
            {
                return getRow(rowNum);
            }
        }
        // No such row found.
        return null;
    }

    /**
     * Method that checks if a particular table cell contains the text specified.
     *
     * @param row the row index
     * @param col the column index
     * @param text the text to check for
     * @return Returns true if the text is contained in the table cell specified.
     */
    public boolean doesCellHaveText(int row, int col, String text)
    {
        final String cellContent = webTable.getCellAsText(row, col);
        return cellContent.contains(text);
    }

    public int getRowCount()
    {
        return webTable.getRowCount();
    }

    public static class Row
    {
        private final WebTable webTable;
        private final int rowIndex;

        Row(final WebTable webTable, final int rowIndex)
        {
            this.webTable = webTable;
            this.rowIndex = rowIndex;
        }

        public String getCellAsText(final int cellIndex)
        {
            return TextKit.collapseWhitespace(webTable.getCellAsText(rowIndex, cellIndex));
        }

        public int getRowIndex()
        {
            return rowIndex;
        }
    }
}
