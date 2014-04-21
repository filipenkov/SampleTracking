/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package com.atlassian.jira.plugins.importer.web.csv;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.csv.ImportException;
import com.atlassian.jira.plugins.importer.managers.CreateConstantsManager;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.ImporterControllerFactory;
import com.atlassian.jira.plugins.importer.web.ImporterProcessSupport;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.web.WebInterfaceManager;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("unused")
public class CsvValueMappingsPage extends ImporterProcessSupport.Csv {
	private String constantType;
	private String constantValue;
	private String constantField;
	private boolean addConstant;

	private final CreateConstantsManager createConstantsManager;
	private final ConstantsManager constantsManager;

	public CsvValueMappingsPage(UsageTrackingService usageTrackingService, ImporterControllerFactory importerControllerFactory,
			CreateConstantsManager createConstantsManager, ConstantsManager constantsManager,
			WebInterfaceManager webInterfaceManager) {
		super(usageTrackingService, importerControllerFactory, webInterfaceManager);
		this.createConstantsManager = createConstantsManager;
		this.constantsManager = constantsManager;
	}

	@RequiresXsrfCheck
	public String doAddConstant() throws Exception {
		final CsvConfigBean configBean = getConfigBean();
		if (StringUtils.isNotEmpty(getConstantType()) && StringUtils.isNotEmpty(getConstantValue())
				&& configBean != null) {
			String id = createConstantsManager.addConstant(getConstantValue(), getConstantType());
			String valueMappingName = configBean.getValueMappingName(getConstantField(), getConstantValue());
			configBean.setValue(valueMappingName, id);
		}
		return getRedirect(getActionName() + "!default.jspa?externalSystem=" + getExternalSystem());
	}

	@Override
	public String doDefault() throws Exception {
		String result = super.doDefault();

		if (INPUT.equals(result)) {
			try {
				getConfigBean().populateUniqueCsvFieldValues();
			} catch (ImportException e) {
				addErrorMessage(getText("jira-importer-plugin.csv.value.mapping.page.populate.failed", e.getMessage()));
			}
		}

		return result;
	}

	@Override
	@RequiresXsrfCheck
	protected void doValidation() {
		super.doValidation();

		final CsvConfigBean configBean = getConfigBean();
		if (configBean == null) {
			return;
		}

		final Map/*<String, String[]*/ params = ActionContext.getParameters();

		configBean.populateConfigBean(params);
	}

	public String getConstantType() {
		return constantType;
	}

	public void setConstantType(String constantType) {
		this.constantType = constantType;
	}

	public String getConstantValue() {
		return constantValue;
	}

	public void setConstantValue(String constantValue) {
		this.constantValue = constantValue;
	}

	public String getConstantField() {
		return constantField;
	}

	public void setConstantField(String constantField) {
		this.constantField = constantField;
	}

	public boolean isAddConstant() {
		return addConstant;
	}

	public void setAddConstant(boolean addConstant) {
		this.addConstant = addConstant;
	}

	@Override
	@RequiresXsrfCheck
	protected String doExecute() throws Exception {
		if (addConstant) {
			return doAddConstant();
		}

		return super.doExecute();
	}

	public Collection<Priority> getPriorities() {
		return constantsManager.getPriorityObjects();
	}

	public Collection<IssueType> getIssueTypes() {
		return constantsManager.getAllIssueTypeObjects();
	}

	public Collection<Resolution> getResolutions() {
		return constantsManager.getResolutionObjects();
	}

	public Collection<Status> getStatuses() {
		return constantsManager.getStatusObjects();
	}

	public boolean issueConstantsIncludeName(Collection<? extends IssueConstant> collection, @Nullable String name) {
		if (name != null) {
			for (IssueConstant issueConstant : collection) {
				if (StringUtils.equalsIgnoreCase(name, issueConstant.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getFormTitle() {
		return "Setup value mappings";
	}
}
