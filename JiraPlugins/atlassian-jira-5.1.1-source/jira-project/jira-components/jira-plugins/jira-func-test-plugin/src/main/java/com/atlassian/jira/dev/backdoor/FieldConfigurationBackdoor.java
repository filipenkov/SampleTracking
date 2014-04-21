package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * A backdoor for manipulating field configurations. There is more functionality available in JIRA's Func Tests.
 *
 * @since v5.0.1
 * @author mtokar
 */
@Path("fieldConfiguration")
public class FieldConfigurationBackdoor
{
    private final FieldLayoutManager fieldLayoutManager;

    public FieldConfigurationBackdoor(FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    @GET
    @AnonymousAllowed
    @Path("copy")
    public Response copyFieldConfiguration(@QueryParam("name") String fieldConfigName, @QueryParam("copyName") String newFieldConfigName)
    {
        EditableFieldLayout originalLayout = getFieldLayout(fieldConfigName);
        EditableFieldLayout editableFieldLayout = new EditableFieldLayoutImpl(null, originalLayout.getFieldLayoutItems());
        if (StringUtils.isBlank(newFieldConfigName))
        {
            editableFieldLayout.setName("Copy of " + originalLayout.getName());
        }
        else
        {
            editableFieldLayout.setName(newFieldConfigName);
        }
        editableFieldLayout.setDescription(originalLayout.getDescription());
        fieldLayoutManager.storeEditableFieldLayout(editableFieldLayout);

        return Response.ok().build();
    }

    private EditableFieldLayout getFieldLayout(String fieldConfigName)
    {
        List<EditableFieldLayout> editableFieldLayouts = fieldLayoutManager.getEditableFieldLayouts();
        for (EditableFieldLayout editableFieldLayout : editableFieldLayouts)
        {
            if (fieldConfigName.equalsIgnoreCase(editableFieldLayout.getName()))
            {
                return editableFieldLayout;
            }
        }
        return null;
    }

}
