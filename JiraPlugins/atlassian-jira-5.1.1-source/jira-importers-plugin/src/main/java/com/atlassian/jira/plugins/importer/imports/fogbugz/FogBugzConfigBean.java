/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz;

import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelperImpl;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.FogBugzValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.ProjectTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FogBugzConfigBean extends AbstractDatabaseConfigBean {
	private static final String COMPUTER_FIELD = "sComputer";
	private static final String FULL_NAME_FIELD = "sFullName";

	public static final String DUPLICATES_LINK_NAME = "Duplicates";

	public static final String SEE_ALSO_LINK_NAME = "See also";

	public static final String SUBCASE_LINK_NAME = "Parent / Subcase";

	public static final String UNUSED_USERS_GROUP = "fogbugz-import-unused-users";

    private final ExternalUtils utils;

	public FogBugzConfigBean(JdbcConnection jdbcConnection, ExternalUtils utils) {
		super(jdbcConnection, utils);
		this.utils = utils;

	}

    @Override
    public void initializeValueMappingHelper() {
		final FogBugzValueMappingDefinitionsFactory mappingDefinitionsFactory =
				new FogBugzValueMappingDefinitionsFactory(this, utils.getAuthenticationContext(),
						utils.getConstantsManager(), utils.getFieldManager());

		valueMappingHelper = new ValueMappingHelperImpl(utils.getWorkflowSchemeManager(),
				utils.getWorkflowManager(), mappingDefinitionsFactory, utils.getConstantsManager());
    }

	public List<String> getExternalProjectNames() {
		return getJdbcConnection().queryDb(new ResultSetTransformer<String>() {
			public String getSqlQuery() {
				return ProjectTransformer.PROJECT_QUERY_SQL;
			}

			public String transform(ResultSet rs) throws SQLException {
				return rs.getString("sProject");
			}
		});
	}

	// -------------------------------------------------------------------------------------------------- Field Mappings

	public List<ExternalCustomField> getCustomFields() {
		List<ExternalCustomField> fields = Lists.newArrayList(
				new ExternalCustomField("sCustomerEmail", "Customer Email",
						CustomFieldConstants.TEXT_FIELD_TYPE, CustomFieldConstants.TEXT_FIELD_SEARCHER),
				new ExternalCustomField(COMPUTER_FIELD, "Computer",
						CustomFieldConstants.TEXT_FIELD_TYPE, CustomFieldConstants.TEXT_FIELD_SEARCHER));
		return fields;
	}

	public String getUsernameForFullName(String sFullName) {
		String translatedUserName = getValueMappingHelper().getValueMappingForImport(FULL_NAME_FIELD, sFullName);
		if (sFullName.equals(translatedUserName)) {
			return extractUsername(sFullName);
		} else {
			return translatedUserName;
		}
	}

	protected static String extractUsername(String fullName) {
		String username;
		if (StringUtils.contains(fullName, " ")) {
			username = StringUtils.replaceChars(fullName, "-'()", "");
			username = StringUtils.substring(username, 0, 1) + StringUtils.substringAfter(username, " ");
			username = StringUtils.replaceChars(username, " ", "");
		} else {
			username = fullName;
		}
		return StringUtils.lowerCase(username);
	}

	public List<String> getLinkNamesFromDb() {
        return ImmutableList.of(DUPLICATES_LINK_NAME, SEE_ALSO_LINK_NAME, SUBCASE_LINK_NAME);
	}

	public String getUnusedUsersGroup() {
		return UNUSED_USERS_GROUP;
	}
}

