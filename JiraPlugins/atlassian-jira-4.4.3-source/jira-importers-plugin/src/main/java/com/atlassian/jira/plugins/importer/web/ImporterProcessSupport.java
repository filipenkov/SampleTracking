/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.SQLRuntimeException;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.SortTool;
import webwork.action.Action;
import webwork.action.ActionContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@WebSudoRequired
public abstract class ImporterProcessSupport extends JiraWebActionSupport {

	private final static String JIM_FOOTER_LOCATION = "jim.footer";

	private final static String JIM_HEADER_LOCATION = "jim.header";
	private final static String STEP_I18N_PREFIX = "jira-importer-plugin.wizard.step.";
	private final static String STEP_I18N_FINAL = ImporterLogsPage.class.getSimpleName();

	private String externalSystem;

	public static final String BUTTON_NAME_NEXT = "nextBtn";

	public static final String BUTTON_NAME_PREVIOUS = "previousBtn";

	private String submitBtn;

	private String finishButton;

	protected final ImporterControllerFactory importerControllerFactory;

	private final WebInterfaceManager webInterfaceManager;

	private final Map<String, String> htmlHints = Maps.newHashMap();
	private final UsageTrackingService usageTrackingService;

	public ImporterProcessSupport(UsageTrackingService usageTrackingService,
			ImporterControllerFactory importerControllerFactory, WebInterfaceManager webInterfaceManager) {
		this.importerControllerFactory = importerControllerFactory;
		this.webInterfaceManager = webInterfaceManager;
		this.usageTrackingService = usageTrackingService;
		usageTrackingService.includeTrackingWhenActive();
	}

	public final int getTotalSteps() {
		return getController().getSteps().size();
	}

	public int getCurrentStep() {
		final String actionName = getActionName();
		final List<String> steps = getController().getSteps();
		return (STEP_I18N_FINAL.equals(actionName) ? steps.size() : steps.indexOf(actionName)) + 1;
	}

	@SuppressWarnings("unused")
	public void setExternalSystem(String system) {
		if (importerControllerFactory.getSupportedImporters().contains(system)) {
			this.externalSystem = system;
		} else {
			throw new RuntimeException("unknown importer type [" + system + "]");
		}
	}

	/**
	 * @return save external system id
	 */
	@Nullable
	public String getExternalSystem() {
		return externalSystem;
	}

	protected boolean isPreviousClicked() {
		return isButtonClickedByName(BUTTON_NAME_PREVIOUS);
	}

	protected boolean isNextClicked() {
		return isButtonClickedByName(BUTTON_NAME_NEXT);
	}

	protected boolean isFinishClicked() {
		return isNextClicked() && getCurrentStep() >= getTotalSteps();
	}

	protected boolean isButtonClickedByName(String name) {
		return StringUtils.isNotBlank(ParameterUtils.getStringParam(ActionContext.getParameters(), name));
	}

	@SuppressWarnings("unused")
	public String getSubmitBtn() {
		return submitBtn;
	}

	@SuppressWarnings("unused")
	public void setSubmitBtn(String submitBtn) {
		this.submitBtn = submitBtn;
	}

	@SuppressWarnings("unused")
	public String getFinishButton() {
		return finishButton;
	}

	@SuppressWarnings("unused")
	public void setFinishButton(String finishButton) {
		this.finishButton = finishButton;
	}

	@Override
	public String execute() throws Exception {
		String result;
		try {
			result = super.execute();
		} catch (SQLRuntimeException e) {
			log.error(e);
			addErrorMessage("Unexpected exception: " + e.getMessage());
			result = Action.INPUT;
		}

		if (Action.INPUT.equals(result)) {
			try {
				prepareModel();
			} catch (SQLRuntimeException e) {
				log.error(e);
				addErrorMessage("Unexpected exception: " + e.getMessage());
			}
		}
		return result;
	}

	protected void prepareModel() {
	}

	@Override
	@RequiresXsrfCheck
	protected String doExecute() throws Exception {
		int currentStep = getController().getSteps().indexOf(getActionName());

		if (isPreviousClicked()) {
			currentStep--;
		} else if (!isFinishClicked() && isNextClicked()) {
			currentStep++;
		}

		if (isFinishClicked()) {
			return getRedirect("ImporterLogsPage!import.jspa?externalSystem=" + getExternalSystem()
					+ "&atl_token=" + getXsrfToken());
		}

		final String redirect = currentStep >= 0 ?
				getController().getSteps().get(currentStep) + "!default.jspa?externalSystem=" + getExternalSystem()
				: "ExternalImport1.jspa";
		return getRedirect(redirect);
	}

	public final boolean isExternalUserManagementEnabled() {
		return getApplicationProperties().getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
	}

	@Override
	public ResourceBundle getTexts(String bundleName) {
		return TextsUtil.getTexts(this, bundleName);
	}

	public BrowserUtils getBrowserUtils() {
		return new BrowserUtils();
	}

	public HelpUtil getHelpUtil() {
		return HelpUtil.getInstance();
	}

	@SuppressWarnings("unused")
	public EscapeTool getEsc() {
		return new EscapeTool();
	}

	public String getTitle() {
		return getController().getTitle();
	}

	public ImporterController getController() {
		return importerControllerFactory.getController(getExternalSystem());
	}

	@Override
	protected void doValidation() {
		super.doValidation();

		if (isNextClicked() && getCurrentStep() + 1 == getTotalSteps()
				&& getController() != null && getController().getImportProcessBeanFromSession() != null
				&& getController().getImportProcessBeanFromSession().getConfigBean() != null) {
			getController().getImportProcessBeanFromSession().getConfigBean().validateJustBeforeImport(this);
		}
	}

	@Override
	public String doDefault() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}
		if (getController().getImportProcessBeanFromSession() == null) {
			return "restartimporterneeded";
		}
		return super.doDefault();
	}

	public static abstract class Database extends ImporterProcessSupport {

		public Database(UsageTrackingService usageTrackingService, ImporterControllerFactory importerControllerFactory,
				WebInterfaceManager webInterfaceManager) {
			super(usageTrackingService, importerControllerFactory, webInterfaceManager);
		}

		@Nullable
		public AbstractConfigBean2 getConfigBean() {
			try {
				if (getController() == null || getController().getImportProcessBeanFromSession() == null) {
					return null;
				}
				return (AbstractConfigBean2) getController().getImportProcessBeanFromSession().getConfigBean();
			} catch (ClassCastException e) {
				return null;
			}
		}
	}

	public static abstract class Csv extends ImporterProcessSupport {

		public Csv(UsageTrackingService usageTrackingService, ImporterControllerFactory importerControllerFactory,
				WebInterfaceManager webInterfaceManager) {
			super(usageTrackingService, importerControllerFactory, webInterfaceManager);
		}

		@Nullable
		public CsvConfigBean getConfigBean() {
			try {
				if (getController() == null || getController().getImportProcessBeanFromSession() == null) {
					return null;
				}
				return (CsvConfigBean) getController().getImportProcessBeanFromSession().getConfigBean();
			} catch (ClassCastException e) {
				return null;
			}
		}
	}

	public void addErrorMessage(String errorMessage, String htmlHint) {
		addErrorMessage(errorMessage);
		htmlHints.put(errorMessage, htmlHint);
	}

	public void addErrorMessages(Map<String, String> errorMessages) {
		htmlHints.clear();
		super.addErrorMessages(errorMessages.keySet());
		htmlHints.putAll(errorMessages);
	}

	public String getHtmlHint(String errorMessage) {
		return htmlHints.get(errorMessage);
	}

	public SortTool getSorter() {
		return new SortTool();
	}

	public boolean isAdministrator() {
		return isHasPermission(Permissions.ADMINISTER);
	}

	public boolean isAttachmentsEnabled() {
		return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS);
	}

	public boolean isSubtasksEnabled() {
		return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS);
	}

	public UsageTrackingService getUsageTrackingService() {
		return usageTrackingService;
	}

	public Collection<String> getHeaderPanels() {
		return getPanels(JIM_HEADER_LOCATION);
	}

	public Collection<String> getFooterPanels() {
		return getPanels(JIM_FOOTER_LOCATION);
	}

	public Collection<String> getPanels(final String location) {
		final List<WebPanel> summaryPanels = webInterfaceManager.getDisplayableWebPanels(
				location, Collections.<String, Object>emptyMap());

		return Collections2.transform(summaryPanels, new Function<WebPanel, String>() {
			@Override
			public String apply(@Nullable WebPanel input) {
				return input.getHtml(getPanelsContext(location));
			}
		});
	}

	@Nullable
	public String getWizardActiveSection() {
		return getController().getSection();
	}

	public String getWizardActiveTab() {
		return getClass().getSimpleName();
	}

	protected Map<String, Object> getPanelsContext(String location) {
		return MapBuilder.<String, Object>newBuilder()
				.add("externalSystem", getExternalSystem())
				.add("action", getActionName()).toMap();
	}

	@Nullable
	public abstract String getFormTitle();

	@Nullable
	public String getFormDescription() {
		return null;
	}

}
