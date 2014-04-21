package com.atlassian.crowd.util.build;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Basic tests for the generated class {@link BuildUtils} to ensure that it
 * has been included in the build.
 *
 */
public class BuildUtilsTest
{
    @Test
    public void fieldsAreNotNull()
    {
        assertNotNull(BuildUtils.BUILD_VERSION);
        assertNotNull(BuildUtils.BUILD_NUMBER);
        assertNotNull(BuildUtils.BUILD_DATE);
    }
    
    @Test
    public void methodsGiveNonNullResults()
    {
        assertNotNull(BuildUtils.getVersion());
        assertNotNull(BuildUtils.getCurrentBuildDate());
    }
}
