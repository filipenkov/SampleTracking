package com.atlassian.crowd.model.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InternalGroupTest
{
    private static final String SHORT_STRING = StringUtils.repeat("X", 255);
    private static final String LONG_STRING = StringUtils.repeat("X", 300);
    private static final String TRUNCATED_STRING = StringUtils.repeat("X", 252).concat("...");

    private static final Directory DIRECTORY = new DirectoryImpl(new InternalEntityTemplate(-1L, "directory", true, null, null));

    @Test
    public void testInternalGroup_ShortDescription()
    {
        final GroupTemplate groupTemplate = new GroupTemplate("group");
        groupTemplate.setDescription(SHORT_STRING);
        final InternalGroup group = new InternalGroup(groupTemplate, DIRECTORY);

        assertEquals(SHORT_STRING, group.getDescription());
    }

    @Test
    public void testInternalGroup_LongDescription()
    {
        final GroupTemplate groupTemplate = new GroupTemplate("group");
        groupTemplate.setDescription(LONG_STRING);
        final InternalGroup group = new InternalGroup(groupTemplate, DIRECTORY);

        assertEquals(TRUNCATED_STRING, group.getDescription());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInternalGroup_LongName()
    {
        final GroupTemplate groupTemplate = new GroupTemplate(LONG_STRING);
        new InternalGroup(groupTemplate, DIRECTORY);
    }
}
