package com.atlassian.support.tools.properties;

import java.util.HashMap;
import java.util.Map;

public class DefaultPropertyStore extends AbstractPropertyStore implements PropertyStore {
	private Map<String,PropertyStore> categories = new HashMap<String, PropertyStore>();
	
	@Override
	public PropertyStore addCategory(String key) {
		PropertyStore store = categories.get(key);
		if (store == null) {
			store = new DefaultPropertyStore();
			categories.put(key, store);
		}
		return store;
	}

	@Override
	public PropertyStore addCategory(String key, PropertyStore store) {
		categories.put(key, store);
		return store;
	}
	
	@Override
	public Map<String, PropertyStore> getCategories() {
		return categories;
	}
}
