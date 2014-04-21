package com.atlassian.jira.util.system.patch;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Set;

/**
 */
public class TestAppliedPatches extends ListeningTestCase
{
    @Test
    public void testIsNeverNull()
    {
        final Set<AppliedPatchInfo> patches = AppliedPatches.getAppliedPatches();
        assertNotNull(patches);
        //
        // this will be empty unless we are producing a patched version of JIRA
        assertEquals(0, patches.size());
    }
}
