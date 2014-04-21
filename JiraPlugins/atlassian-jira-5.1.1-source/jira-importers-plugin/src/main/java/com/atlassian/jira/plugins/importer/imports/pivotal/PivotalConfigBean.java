/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelperImpl;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.plugins.importer.imports.pivotal.config.LoginNameValueMapper;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PivotalConfigBean extends AbstractConfigBean2 implements UserNameMapper {

	private final PivotalClient pivotalClient;
	private final ExternalUtils utils;
	private final String defaultResolutionId;
	private final SiteConfiguration credentials;
	private final PivotalImporterController importerController;

	private boolean showUserMappingPage; // not persisted!

	public PivotalConfigBean(SiteConfiguration credentials, ExternalUtils utils, PivotalImporterController importerController) {
		super(utils);
		this.credentials = credentials;
		this.importerController = importerController;
		this.pivotalClient = new CachingPivotalClient(this);
		this.utils = utils;
		defaultResolutionId = utils.getApplicationProperties().getString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION);
	}

    @Override
    public void initializeValueMappingHelper() {
        final ValueMappingDefinitionsFactory mappingDefinitionFactory = new ValueMappingDefinitionsFactory() {
			@Override
			public List<ValueMappingDefinition> createMappingDefinitions(ValueMappingHelper valueMappingHelper) {
				if (showUserMappingPage) {
					// using Supplier so the actual code is not invoked until ValueMappingHelper.initDistinctValuesCache()
					Supplier<Set<String>> usernameProvider = new Supplier<Set<String>>() {
						@Override
						public Set<String> get() {
							final PivotalDataBean dataBean = importerController.createDataBean(false);
							final Set<ExternalUser> allUsers = dataBean.getAllUsers(ConsoleImportLogger.INSTANCE);
							return ImmutableSet.copyOf(Iterables.transform(allUsers, new Function<ExternalUser, String>() {
								@Override
								public String apply(ExternalUser input) {
									return input.getFullname(); // PivotalMembershipParser stores the original name as fullname
								}
							}));

						}
					};
					return Collections.<ValueMappingDefinition>singletonList(new LoginNameValueMapper(usernameProvider, utils.getAuthenticationContext()));
				} else {
					return Collections.emptyList();
				}
			}
		};
		valueMappingHelper = new ValueMappingHelperImpl(utils.getWorkflowSchemeManager(),
				utils.getWorkflowManager(), mappingDefinitionFactory, utils.getConstantsManager());
    }

	public PivotalClient getPivotalClient() {
		return pivotalClient;
	}

	@Override
	public List<String> getExternalProjectNames() {
		try {
			final PivotalClient pivotalClient = getPivotalClient(); // so we can override in tests
			if (!pivotalClient.isLoggedIn()) {
				pivotalClient.login(credentials.getUsername(), credentials.getPassword());
			}
			return Lists.newArrayList(pivotalClient.getAllProjectNames());
		} catch (PivotalRemoteException e) {
			throw new RuntimeException(e); // @todo wseliga
		}
	}


	@Override
    public Map<String, Map<String, String>> getAvailableFieldMappings(ExternalCustomField customField, final Set<ExternalProject> projects) {
        return Maps.newLinkedHashMap();
    }

    @Override
	public List<ExternalCustomField> getCustomFields() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getLinkNamesFromDb() {
		return Collections.emptyList();
	}

	@Override
	public String getUsernameForLoginName(String loginName) {
		if (StringUtils.isBlank(getValueMappingHelper().getValueMapping(LoginNameValueMapper.FIELD, loginName))) {
			return loginName;
		} else {
			return getValueMappingHelper().getValueMappingForImport(LoginNameValueMapper.FIELD, loginName);
		}
	}

	public boolean isShowUserMappingPage() {
		return showUserMappingPage;
	}

	public void setShowUserMappingPage(boolean showUserMappingPage) {
		this.showUserMappingPage = showUserMappingPage;
	}

	public String getDefaultResolutionId() {
		return defaultResolutionId;
	}

	public SiteConfiguration getCredentials() {
		return credentials;
	}
}

