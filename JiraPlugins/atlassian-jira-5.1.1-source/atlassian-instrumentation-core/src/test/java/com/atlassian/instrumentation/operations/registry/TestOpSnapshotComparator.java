package com.atlassian.instrumentation.operations.registry;

import com.atlassian.instrumentation.operations.OpSnapshot;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestOpSnapshotComparator extends TestCase
{

    OpSnapshot snapshot1 = new OpSnapshot("nameABC", 10, 20, 130, 0);
    OpSnapshot snapshot2 = new OpSnapshot("nameABC", 10, 40, 250, 0);
    OpSnapshot snapshot3 = new OpSnapshot("nameXYZ", 10, 40, 350, 0);
    OpSnapshot snapshot4 = new OpSnapshot("nameXYZ", 10, 40, 470, 0);
    OpSnapshot snapshot5 = new OpSnapshot("nameXYZ", 10, 50, 550, 0);
    OpSnapshot snapshot6 = new OpSnapshot("nameKLM", 20, 5, 67, 0);

    public void testNameSorting()
    {
        List<OpSnapshot> snapshotList = Arrays.asList(snapshot1, snapshot2, snapshot3, snapshot4, snapshot5, snapshot6);
        Collections.sort(snapshotList, OpSnapshotComparator.BY_NAME);

        List<OpSnapshot> expectedList = Arrays.asList(snapshot1, snapshot2, snapshot6, snapshot3, snapshot4, snapshot5);
        assertEquals(expectedList, snapshotList);
    }

    public void testHighestTimeSorting()
    {
        List<OpSnapshot> snapshotList = Arrays.asList(snapshot1, snapshot2, snapshot3, snapshot4, snapshot5, snapshot6);
        Collections.sort(snapshotList, OpSnapshotComparator.BY_TIME_TAKEN);

        List<OpSnapshot> expectedList = Arrays.asList(snapshot5, snapshot4, snapshot3, snapshot2, snapshot1, snapshot6);
        assertEquals(expectedList, snapshotList);
    }

    public void testHighestInvocationCountSorting()
    {
        List<OpSnapshot> snapshotList = Arrays.asList(snapshot1, snapshot2, snapshot3, snapshot4, snapshot5, snapshot6);
        Collections.sort(snapshotList, OpSnapshotComparator.BY_INVOCATION_COUNT);

        List<OpSnapshot> expectedList = Arrays.asList(snapshot6, snapshot5, snapshot4, snapshot3, snapshot2, snapshot1);
        assertEquals(expectedList, snapshotList);
    }

    public void testHighestResultSetSizeSorting()
    {
        List<OpSnapshot> snapshotList = Arrays.asList(snapshot1, snapshot2, snapshot3, snapshot4, snapshot5, snapshot6);
        Collections.sort(snapshotList, OpSnapshotComparator.BY_RESULT_SET_SIZE);

        List<OpSnapshot> expectedList = Arrays.asList(snapshot5, snapshot4, snapshot3, snapshot2, snapshot1, snapshot6);
        assertEquals(expectedList, snapshotList);
    }

    public void testDefautSorting()
    {
        List<OpSnapshot> snapshotList = Arrays.asList(snapshot1, snapshot2, snapshot3, snapshot4, snapshot5, snapshot6);
        Collections.sort(snapshotList, OpSnapshotComparator.BY_DEFAULT);

        // default is highest time taken
        List<OpSnapshot> expectedList = Arrays.asList(snapshot5, snapshot4, snapshot3, snapshot2, snapshot1, snapshot6);
        assertEquals(expectedList, snapshotList);
    }

}