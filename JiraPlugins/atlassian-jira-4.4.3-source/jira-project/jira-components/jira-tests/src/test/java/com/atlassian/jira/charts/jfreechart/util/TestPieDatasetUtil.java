package com.atlassian.jira.charts.jfreechart.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import org.jfree.data.general.PieDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.apache.commons.lang.ArrayUtils;

/**
 * @since v4.0
 */
public class TestPieDatasetUtil extends ListeningTestCase
{
    private PieDataset getDataset()
    {
        final DefaultPieDataset cakeDistributionDataset = new DefaultPieDataset();

        cakeDistributionDataset.setValue("John Doe", new Double(20));
        cakeDistributionDataset.setValue("Steve Smith", new Double(40));
        cakeDistributionDataset.setValue("Jane Doe", new Double(20));
        cakeDistributionDataset.setValue("Cowboy Neal", new Double(10));
        cakeDistributionDataset.setValue("Crazy Jeeves", new Double(10));

        return cakeDistributionDataset;
    }

    @Test
    public void testCreateConsolidatedSortedPieDataset()
    {
        final PieDataset pieDataset = getDataset();
        final PieDataset sortedConsolidatedPieDataset =
                PieDatasetUtil.createConsolidatedSortedPieDataset(
                        PieDatasetUtil.createSortedPieDataset(pieDataset), "Small eaters", false, 0.15d, 3);


        PieDataset consolidatedSet = PieDatasetUtil.createConsolidatedSortedPieDataset(
                pieDataset, "Small eaters", false, 0.15d, 3);
        assertEquals(4, consolidatedSet.getKeys().size());
        assertTrue(consolidatedSet.getKeys().contains("Small eaters"));
        assertTrue(consolidatedSet.getValue("Small eaters").doubleValue() == 20d);

        consolidatedSet = PieDatasetUtil.createConsolidatedSortedPieDataset(
                pieDataset, "Small eaters", false, 0.15d, 4);
        assertEquals(5, consolidatedSet.getKeys().size());
        assertTrue(consolidatedSet.getKeys().contains("Small eaters"));
        assertTrue(consolidatedSet.getValue("Small eaters").doubleValue() == 10d);


        assertFalse(ArrayUtils.isEquals(
                sortedConsolidatedPieDataset,
                PieDatasetUtil.createConsolidatedSortedPieDataset(pieDataset, "Small eaters", false, 0.15d, 3)));
        assertTrue(ArrayUtils.isEquals(
                sortedConsolidatedPieDataset,
                PieDatasetUtil.createConsolidatedSortedPieDataset(pieDataset, "Small eaters", true, 0.15d, 3)));
    }
}
