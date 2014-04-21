/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * This transformer must work for Bugzilla 2.20 which has not custom fields and 3.x which does have them.
 * We achieve this by detecting fielddefs structure in the runtime.  
 */
public class CustomFieldTransformer implements ResultSetTransformer<ExternalCustomField> {
	public String getSqlQuery() {
		return "SELECT * FROM fielddefs WHERE obsolete=0";
	}

	public ExternalCustomField transform(ResultSet rs) throws SQLException {
		Set<String> columns = Sets.newHashSet(SqlUtils.getColumnNames(rs.getMetaData()));
		if (!columns.contains("custom") || !columns.contains("type")
				|| !columns.contains("name") || !columns.contains("description")) {
			return null;
		}

		if (rs.getInt("custom") == 0) {
			return null;
		}

		switch(rs.getInt("type")) {
			// 1 -> unknown
			case 2: return ExternalCustomField.createSelect(rs.getString("name"), rs.getString("description"));
			case 3: return ExternalCustomField.createMultiSelect(rs.getString("name"), rs.getString("description"));
			case 4: return ExternalCustomField.createFreeText(rs.getString("name"), rs.getString("description"));
			case 5: return ExternalCustomField.createDatetime(rs.getString("name"), rs.getString("description"));
			case 6: return new ExternalCustomField(rs.getString("name"), rs.getString("description"),
					CustomFieldConstants.TEXT_FIELD_TYPE, CustomFieldConstants.EXACT_TEXT_SEARCHER);
			default: return ExternalCustomField.createText(rs.getString("name"), rs.getString("description"));
		}
	}
}
