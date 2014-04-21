package com.atlassian.support.tools.salext.bundle;

public enum BundleManifest {
	AUTH_CONFIG("auth-cfg", BundlePriority.RECOMMENDED),
	APPLICATION_CONFIG("application-config", BundlePriority.RECOMMENDED),
	APPLICATION_PROPERTIES("application-properties", BundlePriority.HIGHLY_RECOMMENDED),
	APPLICATION_LOGS("application-logs", BundlePriority.HIGHLY_RECOMMENDED),
	CACHE_CONFIG("cache-cfg", BundlePriority.DEFAULT),
	FECRU_OUT("fecru-out", BundlePriority.HIGHLY_RECOMMENDED),
	FECRU_PLUGIN_STATE("fecru-pluginstate-properties", BundlePriority.RECOMMENDED),
	MODZ("modz", BundlePriority.RECOMMENDED),
	PLUGIN_CONFIG("fecru-plugin-cfg", BundlePriority.DEFAULT),
	THREAD_DUMP("thread-dump", BundlePriority.RECOMMENDED),
	TOMCAT_CONFIG("tomcat-config", BundlePriority.DEFAULT),
	TOMCAT_LOGS("tomcat-logs", BundlePriority.RECOMMENDED);
	
	private final String key;
	private final BundlePriority priority;
	
	private BundleManifest(String key, BundlePriority priority) {
		this.key = key;
		this.priority = priority;
	}

	public String getKey() {
		return key;
	}
	
	public BundlePriority getPriority() {
		return priority;
	}
}
