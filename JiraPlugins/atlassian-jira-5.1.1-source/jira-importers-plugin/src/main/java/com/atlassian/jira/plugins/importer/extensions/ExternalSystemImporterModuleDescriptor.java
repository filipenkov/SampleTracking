package com.atlassian.jira.plugins.importer.extensions;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.StateAware;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.ConditionElementParser;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since v4.0
 */
public class ExternalSystemImporterModuleDescriptor extends AbstractJiraModuleDescriptor<ImporterController> implements
		StateAware {

	private String logoModuleKey;
	private String logoFile;
	private String documentationUrl;
	private String supportedVersions;
	private String description;
	private Class<? extends ImporterController> controllerFactory;
	private Condition condition;
    private int weight;

	private final ConditionElementParser conditionElementParser;
	protected Element element;

	public ExternalSystemImporterModuleDescriptor(JiraAuthenticationContext authenticationContext,
												  ModuleFactory moduleFactory, final WebInterfaceManager webInterfaceManager) {
		super(authenticationContext, moduleFactory);

		this.conditionElementParser = new ConditionElementParser(new ConditionElementParser.ConditionFactory() {
			public Condition create(String className, Plugin plugin) throws ConditionLoadingException {
				return webInterfaceManager.getWebFragmentHelper().loadCondition(className, plugin);
			}
		});

	}

	public Condition getCondition() {
		return condition;
	}


	@Override
	public void init(Plugin plugin, Element element) throws PluginParseException {
		super.init(plugin, element);
		this.element = element;
		this.description = element.attributeValue("i18n-description-key");
		this.logoModuleKey = element.attributeValue("logo-module-key");
		if (StringUtils.isBlank(logoModuleKey)) {
			throw new PluginParseException("logo-module-key must not be empty");
		}
		this.logoFile = element.attributeValue("logo-file");
		if (StringUtils.isBlank(logoFile)) {
			throw new PluginParseException("logo-file must not be empty");
		}
		this.documentationUrl = element.attributeValue("documentation-url");
		this.supportedVersions = element.attributeValue("i18n-supported-versions-key");
        if (StringUtils.isNotBlank(element.attributeValue("weight"))) {
            this.weight = Integer.valueOf(element.attributeValue("weight"));
        }
	}

	@Override
	public void enabled() {
		super.enabled();
		condition = makeConditions(element, ConditionElementParser.CompositeType.AND);
	}

	@Override
	public void disabled() {
		condition = null;
		super.disabled();
	}

	@Nonnull
	public String getLogoModuleKey() {
		return logoModuleKey;
	}

	@Nonnull
	public String getLogoFile() {
		return logoFile;
	}

	@Nullable
	public String getDocumentationUrl() {
		return documentationUrl;
	}

	@Nonnull
	public String getSupportedVersions() {
		return getI18nBean().getText(StringUtils.defaultString(supportedVersions));
	}

	@Nullable
	public String getDescription() {
		return description == null ? null : getI18nBean().getText(description);
	}

    public int getWeight() {
        return weight;
    }

    protected Condition makeConditions(final Element element, final int type) throws PluginParseException {
		return conditionElementParser.makeConditions(plugin, element, type);
	}

}
