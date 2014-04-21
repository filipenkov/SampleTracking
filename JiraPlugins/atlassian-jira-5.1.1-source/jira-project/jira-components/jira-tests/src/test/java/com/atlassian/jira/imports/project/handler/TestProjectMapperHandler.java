package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.imports.project.mapper.IdKeyPair;
import com.atlassian.jira.imports.project.parser.ProjectParser;
import com.atlassian.core.util.map.EasyMap;

import java.util.Collections;

/**
 * @since v3.13
 */
public class TestProjectMapperHandler extends ListeningTestCase
{
    
    @Test
    public void testProject() throws ParseException
    {
        SimpleProjectImportIdMapper projectMapper = new SimpleProjectImportIdMapperImpl();

        ProjectMapperHandler projectMapperHandler = new ProjectMapperHandler(projectMapper);
        projectMapperHandler.handleEntity(ProjectParser.PROJECT_ENTITY_NAME, EasyMap.build("id", "123", "key", "TST"));
        projectMapperHandler.handleEntity(ProjectParser.PROJECT_ENTITY_NAME, EasyMap.build("id", "321", "key", "ANA"));
        assertEquals(2, projectMapper.getValuesFromImport().size());
        assertTrue(projectMapper.getValuesFromImport().contains(new IdKeyPair("123", "TST")));
        assertTrue(projectMapper.getValuesFromImport().contains(new IdKeyPair("321", "ANA")));
    }

    @Test
    public void testProjectWrongEntityType() throws ParseException
    {
        SimpleProjectImportIdMapper projectMapper = new SimpleProjectImportIdMapperImpl();

        ProjectMapperHandler projectMapperHandler = new ProjectMapperHandler(projectMapper);
        projectMapperHandler.handleEntity("BSENTITY", Collections.EMPTY_MAP);
        assertEquals(0, projectMapper.getValuesFromImport().size());
    }

}
