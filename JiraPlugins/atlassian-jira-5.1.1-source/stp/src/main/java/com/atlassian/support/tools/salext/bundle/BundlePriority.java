package com.atlassian.support.tools.salext.bundle;

public enum BundlePriority {
	DEFAULT(0,""),
	RECOMMENDED(1,"stp.bundle.priority.recommended"),
	HIGHLY_RECOMMENDED(2,"stp.bundle.priority.highly.recommended"),
	REQUIRED(3,"stp.bundle.priority.required");

	private final int priority;
	private final String key;

	private BundlePriority(int priority, String key) {
		this.priority = priority;
		this.key = key;
	}

	public int getPriority() {
		return priority;
	}
	
	public String getPriorityKey() {
		return key;
	}
}
