package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.exception.ParseException;

import java.util.Collection;

/**
 * @since v3.13
 */
public class TestSimpleEntityMapperHandler extends ListeningTestCase
{
    @Test
    public void testParseExceptionOnId()
    {
        SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        SimpleEntityMapperHandler simpleEntityMapperHandler =
                new SimpleEntityMapperHandler(SimpleEntityMapperHandler.PRIORITY_ENTITY_NAME, mapper);
        try
        {
            simpleEntityMapperHandler.handleEntity(SimpleEntityMapperHandler.PRIORITY_ENTITY_NAME, EasyMap.build("id", "", "name", "Frederick"));
            fail("SimpleEntityMapperHandler should have thrown a parse exception.");
        }
        catch (ParseException e)
        {
            //expected
        }
    }

    @Test
    public void testParseExceptionOnName()
    {
        SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        SimpleEntityMapperHandler simpleEntityMapperHandler =
                new SimpleEntityMapperHandler(SimpleEntityMapperHandler.PRIORITY_ENTITY_NAME, mapper);
        try
        {
            simpleEntityMapperHandler.handleEntity(SimpleEntityMapperHandler.PRIORITY_ENTITY_NAME, EasyMap.build("id", "1234", "name", ""));
            fail("SimpleEntityMapperHandler should have thrown a parse exception.");
        }
        catch (ParseException e)
        {
            //expected
        }
    }

    @Test
    public void testHappyPath() throws ParseException
    {
        SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        // make the values we are going to get required
        mapper.flagValueAsRequired("12345");
        mapper.flagValueAsRequired("54321");

        SimpleEntityMapperHandler simpleEntityMapperHandler = new SimpleEntityMapperHandler("Rubbish", mapper);
        simpleEntityMapperHandler.handleEntity("Rubbish", EasyMap.build("id", "12345", "name", "John"));
        simpleEntityMapperHandler.handleEntity("Rubbish", EasyMap.build("id", "54321", "name", "Nhoj"));
        simpleEntityMapperHandler.handleEntity("BSENTITY", EasyMap.build("id", "54321", "name", "fgsdgfd"));
        Collection expected = EasyList.build("12345", "54321");
        assertEquals(2, mapper.getRequiredOldIds().size());
        assertTrue(mapper.getRequiredOldIds().containsAll(expected));
        assertEquals("John", mapper.getKey("12345"));
        assertEquals("Nhoj", mapper.getKey("54321"));
    }
}
