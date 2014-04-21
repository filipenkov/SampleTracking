package org.jcvi.jira.importer.mappers;

import org.jcvi.glk.ctm.*;
import org.jcvi.jira.importer.jiramodel.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pedworth
 * Date: 2/25/13
 * Time: 8:59 AM
 * Encapsulates the logic used to generate a Sample from the CTM data
 */
public class SampleFactory {
    //ChangeGroup
    ////////////////////////////////////////////////////////////////////////////
    //FACTORIES
    ////////////////////////////////////////////////////////////////////////////
    //Used to avoid creating ChangeGroups that contain no changes
    ////////////////////////////////////////////////////////////////////////////
    public static ChangeGroup createChangeGroup(ReferenceHistory history)
            throws UnmappedField {
        Set<ChangeItem> changes = new HashSet<ChangeItem>();
        //from a ReferenceHistory the only thing that could have been updated
        //is the State
        ReferenceStatus status = history.getStatus();
        //Map the status
        Status jiraStatus = StatusMapper.getStatus(status);

        changes.add(ChangeItemFactory.createStatusChangeItem(jiraStatus));
        //Test if the status contains an embedded assignee
        JIRAUser impliedUser = StatusMapper.getImpliedAssignee(status);
        if (impliedUser != null) {
            changes.add(ChangeItemFactory.createAssigneeChangeItem(impliedUser));
        }
        //Test for field changes caused by the status
        changes.addAll(
                StatusMapper.getImpliedFieldValues(status));
        //Test if there is a resolution to use
        Resolution resolution = StatusMapper.getImpliedResolution(jiraStatus);
        //Create a change item even if it is null, the change item may be needed
        //to cleared
        changes.add(ChangeItemFactory.createResolutionChangeItem(resolution));

        //remove any nulls that were added
        changes.remove(null);

        return ChangeGroupFactory.createChangeGroup(history,changes);
    }
}
