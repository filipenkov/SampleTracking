package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntityImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeImpl;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.commons.collections.MultiHashMap;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: amazkovoi
 * Date: 1/09/2004
 * Time: 16:23:16
 */
public class UpgradeTask_Build82 extends AbstractUpgradeTask
{
    private final ConstantsManager constantsManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final OfBizDelegator ofBizDelegator;
    private final ProjectManager projectManager;

    public UpgradeTask_Build82(ProjectManager projectManager, ConstantsManager constantsManager, FieldLayoutManager fieldLayoutManager, OfBizDelegator ofBizDelegator)
    {
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.ofBizDelegator = ofBizDelegator;
    }

    public String getBuildNumber()
    {
        return "82";
    }

    public String getShortDescription()
    {
        return "Upgrade field layouts.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        GenericValue defaultFieldLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", EasyMap.build("layoutscheme", null)));
        if (defaultFieldLayoutGV != null)
        {
            // We have a persisted default field layout - update it
            defaultFieldLayoutGV.set("type", FieldLayoutManager.TYPE_DEFAULT);
            defaultFieldLayoutGV.set("name", EditableDefaultFieldLayout.NAME);
            defaultFieldLayoutGV.set("description", EditableDefaultFieldLayout.DESCRIPTION);
            defaultFieldLayoutGV.store();
        }

        // Go through all Field Layout Schemes and copy their names and descriptions to Field Layouts
        List fieldLayoutSchemeGVs = ofBizDelegator.findAll("FieldLayoutScheme");
        for (Iterator iterator = fieldLayoutSchemeGVs.iterator(); iterator.hasNext();)
        {
            GenericValue fieldLayoutSchemeGV = (GenericValue) iterator.next();
            // Retrieve field layout scheme's field layout and update it
            GenericValue fieldLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", EasyMap.build("layoutscheme", fieldLayoutSchemeGV.getLong("id"))));
            if (fieldLayoutGV != null)
            {
                fieldLayoutGV.set("name", fieldLayoutSchemeGV.getString("name"));
                fieldLayoutGV.set("description", fieldLayoutSchemeGV.getString("description"));
                fieldLayoutGV.store();
            }
            // Remove the field layout record in the data store - we will create a new entry for it later in the upgrade task
            fieldLayoutSchemeGV.remove();
        }

        // Go through the list of field layout association table records
        List associations = ofBizDelegator.findAll("FieldLayoutSchemeAssociation", EasyList.build("project", "issuetype"));
        MultiHashMap matrices = new MultiHashMap();
        FieldLayoutSchemeMatrix fieldLayoutSchemeMatrix = null;
        Long projectId = null;

        // Determine a unique set of Field Layout Schemes that we need to create, and determine with which projects these schemes need to be associated.
        // This can be done by using a MultiHashMap where a potential field layout scheme is used as a key and project ids will be stored as values.
        // MultiHashMap will add a project id to a collection of already stored project ids when we 'put' a new project id into the map.
        for (Iterator iterator = associations.iterator(); iterator.hasNext();)
        {
            GenericValue associationGV = (GenericValue) iterator.next();

            Long associationProjectId = associationGV.getLong("project");

            if (associationProjectId.equals(projectId))
            {
                FieldLayoutSchemeMatrixEntry fieldLayoutSchemeMatrixEntry = new FieldLayoutSchemeMatrixEntry(associationGV.getString("issuetype"), associationGV.getLong("fieldlayoutscheme"));
                fieldLayoutSchemeMatrix.addEntry(fieldLayoutSchemeMatrixEntry);
            }
            else
            {
                if (fieldLayoutSchemeMatrix != null)
                {
                    // As we are using a MultiHashMap - this will either add this project id to the collection of project ids that already use this matrix
                    // Or create a new entry in the map with only this project id as a value
                    matrices.put(fieldLayoutSchemeMatrix, projectId);
                }

                projectId = associationProjectId;
                fieldLayoutSchemeMatrix = new FieldLayoutSchemeMatrix();
                FieldLayoutSchemeMatrixEntry fieldLayoutSchemeMatrixEntry = new FieldLayoutSchemeMatrixEntry(associationGV.getString("issuetype"), associationGV.getLong("fieldlayoutscheme"));
                fieldLayoutSchemeMatrix.addEntry(fieldLayoutSchemeMatrixEntry);
            }
        }

        if (fieldLayoutSchemeMatrix != null)
        {
            matrices.put(fieldLayoutSchemeMatrix, projectId);
        }

        // Loop over all unique matrices and create a FieldLayoutScheme for each one.
        for (Iterator iterator = matrices.keySet().iterator(); iterator.hasNext();)
        {
            FieldLayoutSchemeMatrix matrix = (FieldLayoutSchemeMatrix) iterator.next();
            createFieldLayoutScheme(matrix, (Collection) matrices.get(matrix));
        }
    }

    private void createFieldLayoutScheme(FieldLayoutSchemeMatrix fieldLayoutSchemeMatrix, Collection projectIds)
            throws GenericEntityException
    {
        // Create Field Layout Scheme for this matrix
        FieldLayoutScheme fieldLayoutScheme = new FieldLayoutSchemeImpl(fieldLayoutManager, null);

        StringBuffer schemeDescription = new StringBuffer("Field Configuration Scheme for ");
        List projects = new LinkedList();
        for (Iterator iterator = projectIds.iterator(); iterator.hasNext();)
        {
            GenericValue project = projectManager.getProject((Long) iterator.next());
            if (project == null)
            {
                throw new RuntimeException("JIRA appears to contain a fieldlayoutschemeassociation record referring to a nonexistent project."); // JSP-1561, JSP-2386
            }
            schemeDescription.append(project.getString("name")).append(',');
            projects.add(project);
        }

        // MUst have at least one projects to be associated with
        String schemeName = "Field Configuration Scheme " + ((GenericValue) projects.get(0)).getString("name");
        fieldLayoutScheme.setName(schemeName);
        fieldLayoutScheme.setDescription(schemeDescription.substring(0, schemeDescription.length() - 2));
        fieldLayoutScheme.store();
        boolean hasDefault = false;
        for (Iterator iterator = fieldLayoutSchemeMatrix.getEntries().iterator(); iterator.hasNext();)
        {
            FieldLayoutSchemeMatrixEntry fieldLayoutSchemeMatrixEntry = (FieldLayoutSchemeMatrixEntry) iterator.next();

            FieldLayoutSchemeEntity fieldLayoutSchemeEntity = new FieldLayoutSchemeEntityImpl(fieldLayoutManager, null, constantsManager);
            if (fieldLayoutSchemeMatrixEntry.getIssuetype() == null)
            {
                fieldLayoutSchemeEntity.setIssueTypeId(null);
                hasDefault = true;
            }
            else
            {
                fieldLayoutSchemeEntity.setIssueTypeId(fieldLayoutSchemeMatrixEntry.getIssuetype());
            }
            fieldLayoutSchemeEntity.setFieldLayoutId(getFieldLayoutId(fieldLayoutSchemeMatrixEntry.getFieldLayoutScheme()));
            fieldLayoutScheme.addEntity(fieldLayoutSchemeEntity);
        }

        if (!hasDefault)
        {
            // Create default entry
            FieldLayoutSchemeEntity fieldLayoutSchemeEntity = new FieldLayoutSchemeEntityImpl(fieldLayoutManager, null, constantsManager);
            fieldLayoutSchemeEntity.setIssueTypeId(null);
            fieldLayoutSchemeEntity.setFieldLayoutId(null);
            fieldLayoutScheme.addEntity(fieldLayoutSchemeEntity);
        }

        // Associate the newly created scheme with the projects
        for (Iterator iterator = projects.iterator(); iterator.hasNext();)
        {
            fieldLayoutManager.addSchemeAssociation((GenericValue) iterator.next(), fieldLayoutScheme.getId());
        }
    }

    /**
     * Find the field layout associated to the field layout scheme referenced by the given project/issue type association
     *
     * @param fieldLayoutSchemeId field layout scheme id
     * @return field layout id, can be null
     */
    private Long getFieldLayoutId(Long fieldLayoutSchemeId)
    {
        if (fieldLayoutSchemeId == null)
        {
            return null;
        }
        else
        {
            GenericValue fieldLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", EasyMap.build("layoutscheme", fieldLayoutSchemeId)));
            // If the record in the database does not exist then the default is used - return null
            return fieldLayoutGV == null ? null : fieldLayoutGV.getLong("id");
        }
    }
}

class FieldLayoutSchemeMatrix
{
    private final Set entries;

    public FieldLayoutSchemeMatrix()
    {
        entries = new HashSet();
    }

    public void addEntry(FieldLayoutSchemeMatrixEntry fieldLayoutSchemeMatrixEntry)
    {
        entries.add(fieldLayoutSchemeMatrixEntry);
    }

    public Collection getEntries()
    {
        return entries;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FieldLayoutSchemeMatrix))
        {
            return false;
        }
        final FieldLayoutSchemeMatrix fieldLayoutSchemeMatrix = (FieldLayoutSchemeMatrix) o;
        return entries.equals(fieldLayoutSchemeMatrix.entries);
    }

    public int hashCode()
    {
        return entries.hashCode();
    }
}

class FieldLayoutSchemeMatrixEntry
{
    private final String issuetype;
    private final Long fieldLayoutScheme;

    public FieldLayoutSchemeMatrixEntry(String issuetype, Long fieldLayoutScheme)
    {
        this.issuetype = issuetype;
        this.fieldLayoutScheme = fieldLayoutScheme;
    }

    public String getIssuetype()
    {
        return issuetype;
    }

    public Long getFieldLayoutScheme()
    {
        return fieldLayoutScheme;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FieldLayoutSchemeMatrixEntry))
        {
            return false;
        }

        final FieldLayoutSchemeMatrixEntry fieldLayoutSchemeMatrixEntry = (FieldLayoutSchemeMatrixEntry) o;

        if (fieldLayoutScheme != null ? !fieldLayoutScheme.equals(fieldLayoutSchemeMatrixEntry.fieldLayoutScheme) : fieldLayoutSchemeMatrixEntry.fieldLayoutScheme != null)
        {
            return false;
        }
        if (issuetype != null ? !issuetype.equals(fieldLayoutSchemeMatrixEntry.issuetype) : fieldLayoutSchemeMatrixEntry.issuetype != null)
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int result;
        result = (issuetype != null ? issuetype.hashCode() : 0);
        result = 29 * result + (fieldLayoutScheme != null ? fieldLayoutScheme.hashCode() : 0);
        return result;
    }
}