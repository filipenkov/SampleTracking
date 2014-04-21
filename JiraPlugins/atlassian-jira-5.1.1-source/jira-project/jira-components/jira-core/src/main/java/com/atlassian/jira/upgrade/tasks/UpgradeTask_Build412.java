package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Adds system project avatars.
 *
 * @since v4.0
 */
public class UpgradeTask_Build412 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build412.class);

    private AvatarManager avatarManager;
    private ProjectManager projectManager;
    private ApplicationProperties applicationProperties;

    public UpgradeTask_Build412(AvatarManager avatarManager, ProjectManager projectManager, ApplicationProperties applicationProperties)
    {
        super(false);
        this.avatarManager = avatarManager;
        this.projectManager = projectManager;
        this.applicationProperties = applicationProperties;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // first check if there are any existing system avatars. if so they are from a pre-JIRA 4.0 beta deployment
        for (Avatar avatar : avatarManager.getAllSystemAvatars(Avatar.Type.PROJECT))
        {
            log.info("Deleting existing system avatar");
            avatarManager.delete(avatar.getId());
        }

        // for each system avatar file, add the system avatar
        createAvatar("codegeist.png");
        createAvatar("eamesbird.png");
        createAvatar("jm_black.png");
        createAvatar("jm_brown.png");
        createAvatar("jm_orange.png");
        createAvatar("jm_red.png");
        createAvatar("jm_white.png");
        createAvatar("jm_yellow.png");
        createAvatar("monster.png");
        createAvatar("rainbow.png");
        createAvatar("kangaroo.png");
        Avatar a = createAvatar("rocket.png");
        // for each project set the default
        final List<Project> projects = projectManager.getProjectObjects();
        for (Project project : projects)
        {
            try
            {
                projectManager.updateProject(project, project.getName(), project.getDescription(), project.getLeadUserName(), project.getUrl(), project.getAssigneeType(), a.getId());
            }
            catch (Exception e)
            {
                log.error("Problem adding avatar to project " + project.getName());
            }
        }
        applicationProperties.setString(APKeys.JIRA_DEFAULT_AVATAR_ID, a.getId().toString());
    }

    private Avatar createAvatar(final String fileName)
    {
        log.info("Creating system project avatar " + fileName);
        return avatarManager.create(AvatarImpl.createSystemAvatar(fileName, "image/png", Avatar.Type.PROJECT));
    }

    @Override
    public String getBuildNumber()
    {
        return "412";
    }
}
