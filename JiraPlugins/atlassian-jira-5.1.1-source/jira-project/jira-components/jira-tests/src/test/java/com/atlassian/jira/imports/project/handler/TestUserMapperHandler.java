package com.atlassian.jira.imports.project.handler;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.*;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.imports.project.parser.*;
import com.atlassian.jira.issue.worklog.OfBizWorklogStore;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * @since v3.13
 */
public class TestUserMapperHandler extends ListeningTestCase
{
    @Test
    public void testProjectLeadSetInEndDocument() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        project.setLead("dude");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));


        final UserMapper mapper = new UserMapper(null);
        ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/Some/path");
        projectImportOptions.setOverwriteProjectDetails(true);
        UserMapperHandler mapperHandler = new UserMapperHandler(projectImportOptions, backupProject, mapper);

        mapperHandler.endDocument();

        assertEquals(1, mapper.getRequiredOldIds().size());
        assertEquals("dude", mapper.getRequiredOldIds().iterator().next());
    }

    @Test
    public void testProjectLeadNotSetInEndDocument() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        project.setLead("dude");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));


        final UserMapper mapper = new UserMapper(null);
        ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/Some/path");
        projectImportOptions.setOverwriteProjectDetails(false);
        UserMapperHandler mapperHandler = new UserMapperHandler(projectImportOptions, backupProject, mapper);

        mapperHandler.endDocument();

        assertEquals(0, mapper.getRequiredOldIds().size());
    }

    @Test
    public void testMapperFlaggedByAttacher() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12345", "test.txt", new Date(), "dude");

        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(Collections.EMPTY_MAP);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParserControl.replay();

        final UserMapper mapper = new UserMapper(null);
        ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/Some/path");
        UserMapperHandler mapperHandler = new UserMapperHandler(projectImportOptions, backupProject, mapper)
        {
            AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        mapperHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(1, mapper.getOptionalOldIds().size());
        assertEquals("dude", mapper.getOptionalOldIds().iterator().next());
        mockAttachmentParserControl.verify();
    }

    @Test
    public void testMapperNotFlaggedByUnhandledAttacher() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "2", "test.txt", new Date(), "dude");

        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(Collections.EMPTY_MAP);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParserControl.replay();

        final UserMapper mapper = new UserMapper(null);
        ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/Some/path");
        UserMapperHandler mapperHandler = new UserMapperHandler(projectImportOptions, backupProject, mapper)
        {
            AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        mapperHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(0, mapper.getRequiredOldIds().size());
        mockAttachmentParserControl.verify();
    }

    @Test
    public void testMapperFlaggedByVoter() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setIssueId("12345");
        externalVoter.setVoter("dude");

        final Mock mockUserAssociationParser = new Mock(UserAssociationParser.class);
        mockUserAssociationParser.setStrict(true);
        mockUserAssociationParser.expectAndReturn("parseVoter", P.ANY_ARGS, externalVoter);
        mockUserAssociationParser.expectAndReturn("parseWatcher", P.ANY_ARGS, null);

        final UserMapper mapper = new UserMapper(null);
        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mapper)
        {
            UserAssociationParser getUserAssociationParser()
            {
                return (UserAssociationParser) mockUserAssociationParser.proxy();
            }
        };

        mapperHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(1, mapper.getOptionalOldIds().size());
        assertEquals("dude", mapper.getOptionalOldIds().iterator().next());
        mockUserAssociationParser.verify();
    }

    // The voter is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledVoter() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));
        final MockControl mockUserMapperControl = MockClassControl.createStrictControl(UserMapper.class);
        final UserMapper mockUserMapper = (UserMapper) mockUserMapperControl.getMock();
        mockUserMapperControl.replay();

        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setIssueId("66666");
        externalVoter.setVoter("dude");

        final Mock mockUserAssociationParser = new Mock(UserAssociationParser.class);
        mockUserAssociationParser.setStrict(true);
        mockUserAssociationParser.expectAndReturn("parseWatcher", P.ANY_ARGS, null);
        mockUserAssociationParser.expectAndReturn("parseVoter", P.ANY_ARGS, externalVoter);

        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mockUserMapper)
        {
            UserAssociationParser getUserAssociationParser()
            {
                return (UserAssociationParser) mockUserAssociationParser.proxy();
            }
        };

        mapperHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, Collections.EMPTY_MAP);
        mockUserMapperControl.verify();
    }

    @Test
    public void testMapperFlaggedByProjectRole() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalProjectRoleActor externalProjectRoleActor = new ExternalProjectRoleActor("12", "1234", "4321", UserRoleActorFactory.TYPE, "dude");

        final MockControl mockProjectRoleActorParserControl = MockControl.createStrictControl(ProjectRoleActorParser.class);
        final ProjectRoleActorParser mockProjectRoleActorParser = (ProjectRoleActorParser) mockProjectRoleActorParserControl.getMock();
        mockProjectRoleActorParser.parse(null);
        mockProjectRoleActorParserControl.setReturnValue(externalProjectRoleActor);
        mockProjectRoleActorParserControl.replay();

        final UserMapper mapper = new UserMapper(null);
        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mapper)
        {
            ProjectRoleActorParser getProjectRoleActorParser()
            {
                return mockProjectRoleActorParser;
            }
        };

        mapperHandler.handleEntity(ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME, null);

        assertEquals(1, mapper.getOptionalOldIds().size());
        assertEquals("dude", mapper.getOptionalOldIds().iterator().next());
        mockProjectRoleActorParserControl.verify();
    }

    @Test
    public void testMapperNotFlaggedByUnhandledProjectRoleNotRightProject() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalProjectRoleActor externalProjectRoleActor = new ExternalProjectRoleActor("12", null, "4321", UserRoleActorFactory.TYPE, "dude");

        final MockControl mockProjectRoleActorParserControl = MockControl.createStrictControl(ProjectRoleActorParser.class);
        final ProjectRoleActorParser mockProjectRoleActorParser = (ProjectRoleActorParser) mockProjectRoleActorParserControl.getMock();
        mockProjectRoleActorParser.parse(null);
        mockProjectRoleActorParserControl.setReturnValue(externalProjectRoleActor);
        mockProjectRoleActorParserControl.replay();

        final UserMapper mapper = new UserMapper(null);
        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mapper)
        {
            ProjectRoleActorParser getProjectRoleActorParser()
            {
                return mockProjectRoleActorParser;
            }
        };

        mapperHandler.handleEntity(ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME, null);

        assertEquals(0, mapper.getOptionalOldIds().size());
        mockProjectRoleActorParserControl.verify();
    }

    @Test
    public void testMapperNotFlaggedByUnhandledProjectRoleNotRightRoleType() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalProjectRoleActor externalProjectRoleActor = new ExternalProjectRoleActor("12", "5555", "4321", GroupRoleActorFactory.TYPE, "dude");

        final MockControl mockProjectRoleActorParserControl = MockControl.createStrictControl(ProjectRoleActorParser.class);
        final ProjectRoleActorParser mockProjectRoleActorParser = (ProjectRoleActorParser) mockProjectRoleActorParserControl.getMock();
        mockProjectRoleActorParser.parse(null);
        mockProjectRoleActorParserControl.setReturnValue(externalProjectRoleActor);
        mockProjectRoleActorParserControl.replay();

        final UserMapper mapper = new UserMapper(null);
        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mapper)
        {
            ProjectRoleActorParser getProjectRoleActorParser()
            {
                return mockProjectRoleActorParser;
            }
        };

        mapperHandler.handleEntity(ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME, null);

        assertEquals(0, mapper.getOptionalOldIds().size());
        mockProjectRoleActorParserControl.verify();
    }

    @Test
    public void testMapperFlaggedByWatcher() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setIssueId("12345");
        externalWatcher.setWatcher("dude");

        final Mock mockUserAssociationParser = new Mock(UserAssociationParser.class);
        mockUserAssociationParser.setStrict(true);
        mockUserAssociationParser.expectAndReturn("parseVoter", P.ANY_ARGS, null);
        mockUserAssociationParser.expectAndReturn("parseWatcher", P.ANY_ARGS, externalWatcher);

        final UserMapper mapper = new UserMapper(null);
        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mapper)
        {
            UserAssociationParser getUserAssociationParser()
            {
                return (UserAssociationParser) mockUserAssociationParser.proxy();
            }
        };

        mapperHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(1, mapper.getOptionalOldIds().size());
        assertEquals("dude", mapper.getOptionalOldIds().iterator().next());
        mockUserAssociationParser.verify();
    }

    // The watcher is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledWatcher() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));
        final MockControl mockUserMapperControl = MockClassControl.createStrictControl(UserMapper.class);
        final UserMapper mockUserMapper = (UserMapper) mockUserMapperControl.getMock();
        mockUserMapperControl.replay();

        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setIssueId("66666");
        externalWatcher.setWatcher("dude");

        final Mock mockUserAssociationParser = new Mock(UserAssociationParser.class);
        mockUserAssociationParser.setStrict(true);
        mockUserAssociationParser.expectAndReturn("parseVoter", P.ANY_ARGS, null);
        mockUserAssociationParser.expectAndReturn("parseWatcher", P.ANY_ARGS, externalWatcher);

        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mockUserMapper)
        {
            UserAssociationParser getUserAssociationParser()
            {
                return (UserAssociationParser) mockUserAssociationParser.proxy();
            }
        };

        mapperHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, Collections.EMPTY_MAP);
        mockUserMapperControl.verify();
    }


    @Test
    public void testMapperFlaggedByComment() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalComment externalComment = new ExternalComment();
        externalComment.setIssueId("12345");
        externalComment.setUpdateAuthor("dude");
        externalComment.setUsername("someauthor");

        final Mock mockCommentParser = new Mock(CommentParser.class);
        mockCommentParser.setStrict(true);
        mockCommentParser.expectAndReturn("parse", P.ANY_ARGS, externalComment);

        final UserMapper mapper = new UserMapper(null);
        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mapper)
        {
            CommentParser getCommentParser()
            {
                return (CommentParser) mockCommentParser.proxy();
            }
        };

        mapperHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(2, mapper.getOptionalOldIds().size());
        Collection expected = EasyList.build("dude", "someauthor");
        assertTrue(mapper.getOptionalOldIds().containsAll(expected));
        mockCommentParser.verify();
    }

    // The comment is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledComment() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));
        final MockControl mockUserMapperControl = MockClassControl.createStrictControl(UserMapper.class);
        final UserMapper mockUserMapper = (UserMapper) mockUserMapperControl.getMock();
        mockUserMapperControl.replay();

        ExternalComment externalComment = new ExternalComment();
        externalComment.setIssueId("66666");
        externalComment.setUpdateAuthor("dude");
        externalComment.setUsername("someauthor");

        final Mock mockCommentParser = new Mock(CommentParser.class);
        mockCommentParser.setStrict(true);
        mockCommentParser.expectAndReturn("parse", P.ANY_ARGS, externalComment);

        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mockUserMapper)
        {
            CommentParser getCommentParser()
            {
                return (CommentParser) mockCommentParser.proxy();
            }
        };

        mapperHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, Collections.EMPTY_MAP);
        mockUserMapperControl.verify();
    }

    @Test
    public void testMapperFlaggedByWorklog() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setIssueId("12345");
        externalWorklog.setUpdateAuthor("dude");
        externalWorklog.setAuthor("someauthor");

        final Mock mockWorklogParser = new Mock(WorklogParser.class);
        mockWorklogParser.setStrict(true);
        mockWorklogParser.expectAndReturn("parse", P.ANY_ARGS, externalWorklog);

        final UserMapper mapper = new UserMapper(null);
        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mapper)
        {
            WorklogParser getWorklogParser()
            {
                return (WorklogParser) mockWorklogParser.proxy();
            }
        };

        mapperHandler.handleEntity(OfBizWorklogStore.WORKLOG_ENTITY, Collections.EMPTY_MAP);

        assertEquals(2, mapper.getOptionalOldIds().size());
        assertTrue(mapper.getOptionalOldIds().containsAll(EasyList.build("dude", "someauthor")));
        mockWorklogParser.verify();
    }

    // The worklog is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledWorklog() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));
        final MockControl mockUserMapperControl = MockClassControl.createStrictControl(UserMapper.class);
        final UserMapper mockUserMapper = (UserMapper) mockUserMapperControl.getMock();
        mockUserMapperControl.replay();

        ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setIssueId("66666");
        externalWorklog.setUpdateAuthor("dude");
        externalWorklog.setAuthor("someauthor");

        final Mock mockWorklogParser = new Mock(WorklogParser.class);
        mockWorklogParser.setStrict(true);
        mockWorklogParser.expectAndReturn("parse", P.ANY_ARGS, externalWorklog);

        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mockUserMapper)
        {
            WorklogParser getWorklogParser()
            {
                return (WorklogParser) mockWorklogParser.proxy();
            }
        };

        mapperHandler.handleEntity(OfBizWorklogStore.WORKLOG_ENTITY, Collections.EMPTY_MAP);
        mockUserMapperControl.verify();
    }

    @Test
    public void testMapperFlaggedByChangeGroup() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalChangeGroup externalChangeGroup = new ExternalChangeGroup();
        externalChangeGroup.setIssueId("12345");
        externalChangeGroup.setAuthor("dude");

        final Mock mockChangeGroupParser = new Mock(ChangeGroupParser.class);
        mockChangeGroupParser.setStrict(true);
        mockChangeGroupParser.expectAndReturn("parse", P.ANY_ARGS, externalChangeGroup);

        final UserMapper mapper = new UserMapper(null);
        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mapper)
        {
            ChangeGroupParser getChangeGroupParser()
            {
                return (ChangeGroupParser) mockChangeGroupParser.proxy();
            }
        };

        mapperHandler.handleEntity(ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(1, mapper.getOptionalOldIds().size());
        assertEquals("dude", mapper.getOptionalOldIds().iterator().next());
        mockChangeGroupParser.verify();

    }

    // The changeGroup is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledChangeGroup() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));
        final MockControl mockUserMapperControl = MockClassControl.createStrictControl(UserMapper.class);
        final UserMapper mockUserMapper = (UserMapper) mockUserMapperControl.getMock();
        mockUserMapperControl.replay();

        ExternalChangeGroup externalChangegroup = new ExternalChangeGroup();
        externalChangegroup.setIssueId("66666");
        externalChangegroup.setAuthor("dude");

        final Mock mockChangegroupParser = new Mock(ChangeGroupParser.class);
        mockChangegroupParser.setStrict(true);
        mockChangegroupParser.expectAndReturn("parse", P.ANY_ARGS, externalChangegroup);

        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mockUserMapper)
        {
            ChangeGroupParser getChangeGroupParser()
            {
                return (ChangeGroupParser) mockChangegroupParser.proxy();
            }
        };

        mapperHandler.handleEntity(ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME, Collections.EMPTY_MAP);
        mockUserMapperControl.verify();
    }

    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledEntity() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));
        final MockControl mockUserMapperControl = MockClassControl.createStrictControl(UserMapper.class);
        final UserMapper mockUserMapper = (UserMapper) mockUserMapperControl.getMock();
        mockUserMapperControl.replay();

        UserMapperHandler mapperHandler = new UserMapperHandler(null, backupProject, mockUserMapper);

        mapperHandler.handleEntity("SomeEntity", Collections.EMPTY_MAP);

        mockUserMapperControl.verify();
    }

}
