package com.atlassian.jira.plugins.importer.tracking;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import java.util.HashMap;
import java.util.Map;

public class UsageTrackingServiceImpl implements UsageTrackingService {
	private Boolean isActive;
	private static final String PS_ENTITY_NAME = "JiraImportersPluginSettings";
	private final PropertySet propertySet = initPropertySet();
	private static final String IS_ACTIVE_PSK = "isActive";
	private final WebResourceManager webResourceManager;

    static final String GA_RESOURCE = "com.atlassian.jira.plugins.jira-importers-plugin:ga";

	private static PropertySet initPropertySet() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("delegator.name", "default");
		map.put("entityName", PS_ENTITY_NAME);
		map.put("entityId", 1L);
		return PropertySetManager.getInstance("ofbiz", map);
	}

	public UsageTrackingServiceImpl(WebResourceManager webResourceManager) {
		this.webResourceManager = webResourceManager;
		try {
			if (propertySet.exists(IS_ACTIVE_PSK)) {
				isActive = propertySet.getBoolean(IS_ACTIVE_PSK); // this shitty thing returns false when this property does not exist
			}
		} catch (PropertyException e) {
			isActive = null; // just to be explicit - initially we do NOT track users until they choose it.
		}
	}

	@Override
    public void includeTrackingWhenActive() {
		if (isActive()) {
			webResourceManager.requireResource(GA_RESOURCE);
		}
	}

	@Override
    public synchronized  boolean isActive() {
		return isActive != null && isActive;
	}

	@Override
    public boolean isTrackingStatusDefined() {
		return isActive != null;
	}

	private void updatePropertySet() {
		propertySet.setBoolean(IS_ACTIVE_PSK, isActive);
	}

	@Override
    public synchronized void activate() {
		isActive = true;
		updatePropertySet();
	}

	@Override
    public synchronized void deactivate() {
		isActive = false;
		updatePropertySet();
	}
}
