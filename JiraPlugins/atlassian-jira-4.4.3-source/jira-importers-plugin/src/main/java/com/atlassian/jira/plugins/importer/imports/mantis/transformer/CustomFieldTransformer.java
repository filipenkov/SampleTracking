/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomFieldTransformer implements ResultSetTransformer<ExternalCustomField> {
	public String getSqlQuery() {
		return "SELECT id,name,type FROM mantis_custom_field_table";
	}

	public ExternalCustomField transform(ResultSet rs) throws SQLException {
		// Mantis custom field types, defined (in 1.0) with:
		// $s_custom_field_type_enum_string = '0:String,1:Numeric,2:Float,3:Enumeration,4:E-mail,5:Checkbox,6:List,7:Multiselection list,8:Date,9:Radio';

		switch(rs.getInt("type")) {
			// number and float are the same in JIRA
			case 1:
			case 2: return ExternalCustomField.createNumber(rs.getString("id"), rs.getString("name"));
			// treating enumeration as a list
			case 3: return ExternalCustomField.createSelect(rs.getString("id"), rs.getString("name"));
			case 5: return ExternalCustomField.createCheckboxes(rs.getString("id"), rs.getString("name"));
			case 6: return ExternalCustomField.createSelect(rs.getString("id"), rs.getString("name"));
			case 7: return ExternalCustomField.createMultiSelect(rs.getString("id"), rs.getString("name"));
			case 8: return ExternalCustomField.createDatetime(rs.getString("id"), rs.getString("name"));
			case 9: return ExternalCustomField.createRadio(rs.getString("id"), rs.getString("name"));
			// string, e-mail
			default: return ExternalCustomField.createText(rs.getString("id"), rs.getString("name"));
		}
	}
}
