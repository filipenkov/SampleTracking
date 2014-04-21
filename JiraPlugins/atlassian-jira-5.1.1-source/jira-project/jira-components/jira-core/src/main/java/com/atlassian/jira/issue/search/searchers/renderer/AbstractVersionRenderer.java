package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.issue.fields.option.VersionOption;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The base renderer for Version searcher renderers
 *
 * @since v4.0
 */
abstract class AbstractVersionRenderer extends AbstractProjectConstantsRenderer implements SearchRenderer
{
    private static final Logger log = Logger.getLogger(AbstractVersionRenderer.class);

    private final ProjectManager projectManager;
    private final VersionManager versionManager;
    private final boolean unreleasedOptionsFirst;
    private final SimpleFieldSearchConstantsWithEmpty constants;

    AbstractVersionRenderer(SimpleFieldSearchConstantsWithEmpty constants, String searcherNameKey, ProjectManager projectManager, VersionManager versionManager,
            VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, final FieldVisibilityManager fieldVisibilityManager,
            boolean unreleasedOptionsFirst)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager, constants, searcherNameKey);
        this.constants = constants;
        this.projectManager = projectManager;
        this.versionManager = versionManager;
        this.unreleasedOptionsFirst = unreleasedOptionsFirst;
    }

    List<Option> getSelectListOptions(final User searcher, SearchContext searchContext)
    {
        if (searchContext.isSingleProjectContext())
        {
            Long projectId = searchContext.getProjectIds().iterator().next();
            Project project = projectManager.getProjectObj(projectId);
            if (project != null)
            {
                List<Option> unreleasedOptions = new LinkedList<Option>();
                Collection<Version> unreleasedVersions = versionManager.getVersionsUnreleased(project.getId(), false);
                if (!unreleasedVersions.isEmpty())
                {
                    unreleasedOptions.addAll(CollectionUtil.transform(unreleasedVersions, VersionOption.FUNCTION));
                    unreleasedOptions.add(0, new TextOption(VersionManager.ALL_UNRELEASED_VERSIONS, getI18n(searcher).getText("common.filters.unreleasedversions"), "sectionHeaderOption"));
                }

                // reverse the order of the released versions.
                List<Option> releasedOptions  = new LinkedList<Option>();
                List<Version> releasedVersions = new ArrayList<Version>(versionManager.getVersionsReleased(project.getId(), false));
                if (!releasedVersions.isEmpty())
                {
                    releasedOptions.addAll(CollectionUtil.transform(releasedVersions, VersionOption.FUNCTION));
                    Collections.reverse(releasedOptions);
                    releasedOptions.add(0, new TextOption(VersionManager.ALL_RELEASED_VERSIONS, getI18n(searcher).getText("common.filters.releasedversions"), "sectionHeaderOption"));
                }

                List<Option> versions = new ArrayList<Option>();
                if (unreleasedOptionsFirst)
                {
                    versions.addAll(unreleasedOptions);
                    versions.addAll(releasedOptions);
                }
                else
                {
                    versions.addAll(releasedOptions);
                    versions.addAll(unreleasedOptions);
                }

                return versions;
            }
            else
            {
                log.warn("Project for search context " + searchContext + " is invalid");
            }
        }

        return Collections.emptyList();
    }

    class VersionLabelFunction implements Function<String, GenericProjectConstantsLabel>
    {
        private final User searcher;
        private final boolean createBrowseUrl;

        /**
         * Constructor specifying whether the version label should link to the browse fix for version page
         *
         * @param searcher The User doing the search
         * @param createBrowseUrl Whether or not a link should be created
         * @since v3.10.2
         */
        VersionLabelFunction(final User searcher, boolean createBrowseUrl)
        {
            this.searcher = searcher;
            this.createBrowseUrl = createBrowseUrl;
        }

        public GenericProjectConstantsLabel get(String id)
        {
            if (VersionManager.NO_VERSIONS.equals(id))
            {
                return new GenericProjectConstantsLabel(getI18n(searcher).getText("navigator.hidden.search.request.summary.no.versions"));
            }
            else if (VersionManager.ALL_UNRELEASED_VERSIONS.equals(id))
            {
                return new GenericProjectConstantsLabel(getI18n(searcher).getText("navigator.hidden.search.request.summary.all.unreleased.versions"));
            }
            else if (VersionManager.ALL_RELEASED_VERSIONS.equals(id))
            {
                return new GenericProjectConstantsLabel(getI18n(searcher).getText("navigator.hidden.search.request.summary.all.released.versions"));
            }
            else
            {
                Version version = versionManager.getVersion(new Long(id));
                if (version != null)
                {
                    if (createBrowseUrl)
                    {
                        return new GenericProjectConstantsLabel(version.getName(), "/browse/" + version.getProjectObject().getKey() + "/fixforversion/" + version.getId());
                    }
                    else
                    {
                        return new GenericProjectConstantsLabel(version.getName());
                    }
                }
                else
                {
                    log.warn("Unknown " + constants.getSearcherId() + " selected. Value: " + id);
                    return null;
                }
            }
        }
    }
}
