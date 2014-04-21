package com.atlassian.support.tools.properties;

import org.apache.commons.collections.map.MultiValueMap;

public class MultiValuePropertyStore extends AbstractPropertyStore implements PropertyStore {
	MultiValueMap categories = new MultiValueMap();

	@Override
	public PropertyStore addCategory(String key) {
		PropertyStore store = new MultiValuePropertyStore();
		categories.put(key,store);
		return store;
	}

	@Override
	public PropertyStore addCategory(String key, PropertyStore store) {
		categories.put(key, store);
		return store;
	}

	@Override
	public MultiValueMap getCategories() {
		return categories;
	}
}
