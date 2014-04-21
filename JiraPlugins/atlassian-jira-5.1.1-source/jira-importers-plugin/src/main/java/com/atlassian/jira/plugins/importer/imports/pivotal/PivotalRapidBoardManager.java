/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.greenhopper.api.rapid.configuration.LabsConfigurationService;
import com.atlassian.greenhopper.api.rapid.view.Column;
import com.atlassian.greenhopper.api.rapid.view.QuickFilter;
import com.atlassian.greenhopper.api.rapid.view.RapidViewCreationService;
import com.atlassian.greenhopper.api.rapid.view.Swimlane;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginState;
import com.atlassian.query.order.SortOrder;
import com.google.common.collect.Lists;
import com.pyxis.greenhopper.jira.license.GreenHopperLicenseManager;

import javax.annotation.Nullable;
import java.util.List;

public class PivotalRapidBoardManager {

	private final SearchRequestService searchRequestService;
	private final JiraAuthenticationContext jiraAuthenticationContext;
	private final ConstantsManager constantsManager;
	private final PluginAccessor pluginAccessor;
	public static final String GH_KEY = "com.pyxis.greenhopper.jira";

	public PivotalRapidBoardManager(SearchRequestService searchRequestService,
			JiraAuthenticationContext jiraAuthenticationContext,
			ConstantsManager constantsManager,
			PluginAccessor pluginAccessor) {
		this.searchRequestService = searchRequestService;
		this.jiraAuthenticationContext = jiraAuthenticationContext;
		this.constantsManager = constantsManager;
		this.pluginAccessor = pluginAccessor;
	}

	public boolean isGreenHopperInstalledAndEnabled() {
		final Plugin plugin = pluginAccessor.getPlugin(GH_KEY);
		return plugin != null && plugin.getPluginState().equals(PluginState.ENABLED);
	}

	public boolean isGreenHooperFeaturesEnabled() {
		if (!isGreenHopperInstalledAndEnabled()) {
			return false;
		}

        try {
            GreenHopperLicenseManager licenseManager = ComponentManager.getOSGiComponentInstanceOfType(
                    GreenHopperLicenseManager.class);
            return licenseManager != null && licenseManager.getLicense() != null;
        } catch(NoClassDefFoundError e) {
            return false;
        }
	}

	@Nullable
	public Pair<Long, String> createRapidBoard(Project project) throws Exception {
		RapidViewCreationService rapidViewCreationService = ComponentManager
				.getOSGiComponentInstanceOfType(RapidViewCreationService.class);

		LabsConfigurationService labsConfigurationService =
				ComponentManager.getOSGiComponentInstanceOfType(LabsConfigurationService.class);

		if (rapidViewCreationService == null || labsConfigurationService == null) {
			return null; // no GreenHopper
		}

		if (!labsConfigurationService.isRapidBoardEnabled()) {
			labsConfigurationService.setRapidBoardEnabled(true);
		}

		JiraServiceContext context = new JiraServiceContextImpl(jiraAuthenticationContext.getLoggedInUser());

		SearchRequest filter = new SearchRequest();
		String rapidBoardName = String.format("%s Rapid Board", project.getKey());
		filter.setName(rapidBoardName);
		filter.setQuery(JqlQueryBuilder.newBuilder().where().project(project.getKey())
				.and().sub().fixVersionIsEmpty().or().fixVersion().inFunc("unreleasedVersions").endsub()
				.endWhere().orderBy().addSortForFieldName(
						jiraAuthenticationContext.getI18nHelper().getText("gh.rank.global.name"),
						SortOrder.ASC, true).buildQuery());
		filter.setPermissions(SharedEntity.SharePermissions.GLOBAL);
		filter.setOwnerUserName(context.getLoggedInUser().getName());
		filter = searchRequestService.createFilter(context, filter);

		SimpleErrorCollection errors = new SimpleErrorCollection();

		final Long viewId = rapidViewCreationService.createNewRapidView(context.getLoggedInUser(),
				rapidBoardName, filter.getId(), errors);
		if (errors.hasAnyErrors()) {
			throw new Exception(errors.toString());
		}

		rapidViewCreationService.setStatusMappings(context.getLoggedInUser(), viewId, getRapidBoardColumns(), errors);
		if (errors.hasAnyErrors()) {
			throw new Exception(errors.toString());
		}

		rapidViewCreationService.setSwimlanes(context.getLoggedInUser(), viewId, Lists.<Swimlane>newArrayList(), errors);
		if (errors.hasAnyErrors()) {
			throw new Exception(errors.toString());
		}

		List<QuickFilter> quickFilters = Lists.newArrayList(
				new QuickFilter("Only My Issues", "assignee = currentUser()"),
				new QuickFilter("Recently Updated", "updatedDate >= -1d"));
		rapidViewCreationService.setQuickFilters(context.getLoggedInUser(), viewId, quickFilters, errors);
        if (errors.hasAnyErrors()) {
			throw new Exception(errors.toString());
		}

		return Pair.of(viewId, project.getName());
	}

	public List<Column> getRapidBoardColumns() {
		final List<Column> columns = Lists.newArrayList(
				new Column("Icebox", Lists.<Status>newArrayList(constantsManager.getStatusByName("IceBox"))),
				new Column("Backlog", Lists.<Status>newArrayList(constantsManager.getStatusByName("Not Yet Started"))),
				new Column("Current", Lists.<Status>newArrayList(
						constantsManager.getStatusByName("Started"),
						constantsManager.getStatusByName("Rejected"))),
				new Column("Finished", Lists.<Status>newArrayList(constantsManager.getStatusByName("Finished"))),
				new Column("Delivered", Lists.<Status>newArrayList(constantsManager.getStatusByName("Delivered"))),
				new Column("Done", Lists.<Status>newArrayList(constantsManager.getStatusByName("Accepted"))));
		return columns;
	}
}
