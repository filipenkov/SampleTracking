/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalCommentMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalUserMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.FullNameUserMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.TimeEstimateConverter;
import com.atlassian.jira.project.ProjectManager;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.discovery.tools.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CsvConfiguration {

	private static final Logger log = Logger.getLogger(CsvConfiguration.class);

	public static final String ASSIGNEE_FIELD = IssueFieldConstants.ASSIGNEE;
	public static final String REPORTER_FIELD = IssueFieldConstants.REPORTER;
	public static final String PROJECT_LEAD_FIELD = "project.lead";

	private static final String MAPPER_PREFIX = "settings.advanced.mapper.";

	private CsvConfigBean configBean;
	private final ProjectManager projectManager;

	public CsvConfiguration(CsvConfigBean configBean, ProjectManager projectManager) throws ConfigurationException {
		this.configBean = configBean;
		this.projectManager = projectManager;
	}

	public boolean areProjectsMappedFromCsv() {
		return configBean.getStringValue("mapfromcsv") != null
				? Boolean.valueOf(configBean.getStringValue("mapfromcsv")) : false;
	}

	public boolean getImportSingleProject() {
		final String key = configBean.getProjectKey();
		if (key == null) {
			return false;
		}
		return projectManager.getProjectObjByKey(key) == null;
	}

	public boolean getImportExistingProject() {
		final String key = configBean.getProjectKey();
		if (key == null) {
			return false;
		}
		return projectManager.getProjectObjByKey(key) != null;
	}

	public boolean isSingleProjectCsv() {
		return getImportSingleProject() || getImportExistingProject();
	}

	public ExternalProject getSingleProjectBean() throws Exception {
		final ExternalProject externalProject = new ExternalProject();

		if (getImportSingleProject()) {
			for(String key : configBean.getConfig().keySet()) {
				if (key.startsWith("project.")) {
					externalProject.setField(key.replaceFirst("project.", ""), configBean.getStringValue(key));
				}
			}
		} else if (getImportExistingProject()) {
			externalProject.setKey(configBean.getProjectKey());
		}

		return externalProject;
	}

	public List<ExternalUserMapper> getCustomUserMappers() throws ImportException {
		List<ExternalUserMapper> mappers;
		try {
			String defaultEmail = configBean.getStringValue("user.email.suffix");
			String className = configBean.getStringValue(MAPPER_PREFIX + "user");
			String[] extraUserFields = StringUtils.split(configBean.getStringValue(CsvConfigBean.EXTRA_USER_FIELDS), ",");

			List<String> userFields = Lists.newArrayList(ASSIGNEE_FIELD, REPORTER_FIELD);
			if (areProjectsMappedFromCsv()) {
				userFields.add(PROJECT_LEAD_FIELD);
			}
			if (extraUserFields != null && extraUserFields.length > 0) {
				userFields.addAll(Lists.newArrayList(extraUserFields));
			}

			// If there is a class name
			if (StringUtils.isNotEmpty(className)) {
				Class mapperClass = ClassLoaderUtils.loadClass(className, this.getClass());
				mappers = new ArrayList<ExternalUserMapper>();

				//@TODO WMC make USER_FIEDS dynamic
				for (int i = 0; i < userFields.size(); i++) {
					String userField = userFields.get(i);
					ExternalUserMapper mapper = (ExternalUserMapper) ClassUtils.newInstance(mapperClass,
							new Class[]{String.class, String.class},
							new String[]{userField, defaultEmail});
					mappers.add(mapper);
				}
			}
			// if there is only an e-mail address
			else {
				mappers = new ArrayList<ExternalUserMapper>();
				for (int i = 0; i < userFields.size(); i++) {
					String userField = userFields.get(i);
					ExternalUserMapper mapper = new FullNameUserMapper(userField, defaultEmail);
					mappers.add(mapper);
				}
			}
		}
		catch (Exception e) {
			log.warn("Loading custom user mapper classes failed", e);
			throw new ImportException(e);
		}

		return mappers;
	}

	@Nullable
	public ExternalCommentMapper getCustomCommentMapper() throws ImportException {
		ExternalCommentMapper mapper = null;
		try {
			String className = configBean.getStringValue(MAPPER_PREFIX + "comment");
			if (StringUtils.isNotEmpty(className)) {
				Class mapperClass = ClassLoaderUtils.loadClass(className, this.getClass());
				mapper = (ExternalCommentMapper) mapperClass.newInstance();
			}
		}
		catch (Exception e) {
			log.warn("Loading custom comment mapper class failed", e);
			throw new ImportException(e);
		}

		return mapper;
	}

	@Nullable
	public TimeEstimateConverter getCustomTimeEstimateConverter() throws ImportException {
		TimeEstimateConverter converter = null;
		try {
			String className = configBean.getStringValue(MAPPER_PREFIX + "time.estimate.converter");
			if (StringUtils.isNotEmpty(className)) {
				Class mapperClass = ClassLoaderUtils.loadClass(className, this.getClass());
				converter = (TimeEstimateConverter) mapperClass.newInstance();
			}
		}
		catch (Exception e) {
			log.warn("Loading custom time estimate converter class failed", e);
			throw new ImportException(e);
		}

		return converter;
	}

}

