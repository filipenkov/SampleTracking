package com.atlassian.jira.imports.project.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalGroup;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.HashMap;

/**
 * @since v3.13
 */
public class TestGroupParserImpl extends ListeningTestCase
{
    @Test
    public void testParseIllegalArgumentException() throws ParseException {
        GroupParserImpl groupParser = new GroupParserImpl();
        try {
            groupParser.parse(null);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
    }

    @Test
    public void testParseParseException() {
        GroupParserImpl groupParser = new GroupParserImpl();
        try {
            groupParser.parse(new HashMap());
            fail("Should throw ParseException.");
        } catch (ParseException e) {
            // Expected.
            assertEquals("A Group in the backup file is missing the groupName parameter.", e.getMessage());
        }
    }

    @Test
    public void testParseHappy() throws ParseException {
        GroupParserImpl groupParser = new GroupParserImpl();
        ExternalGroup externalGroup = groupParser.parse(EasyMap.build("groupName", "Kyle"));
        assertEquals("Kyle", externalGroup.getName());
    }
}
