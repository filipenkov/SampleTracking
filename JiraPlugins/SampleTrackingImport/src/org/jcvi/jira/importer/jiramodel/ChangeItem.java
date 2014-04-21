package org.jcvi.jira.importer.jiramodel;

import noNamespace.*;
import org.jcvi.jira.importer.utils.UID;

import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: pedworth
* Date: 2/25/13
* Time: 9:02 AM
* To change this template use File | Settings | File Templates.
*/ ////////////////////////////////////////////////////////////////////////////
//ChangeItem inner class
////////////////////////////////////////////////////////////////////////////
//used to store the specifics of the change
////////////////////////////////////////////////////////////////////////////
public class ChangeItem {
    private final int uid;
    ChangeItemFieldJiraEnum.Enum field;
    private final NameIDPair newValue;

    //private as this should only be used from the factories
    public ChangeItem(ChangeItemFieldJiraEnum.Enum field, NameIDPair newValue) {
        this.uid = UID.getUID(ChangeItem.class);
        this.field = field;
        this.newValue = newValue;
    }

    public ChangeItemFieldJiraEnum.Enum getField() {
        return field;
    }

    public NameIDPair getValue() {
        return newValue;
    }

    //called by a ChangeGroup
    //These must be called in date order for the state to be correct
    public void addToXml(
                         ChangeGroup group,
                         EntityEngineXmlType xml,
                         Map<ChangeItemFieldJiraEnum.Enum,NameIDPair> state) {
        if (!testUpdate(state)) {
            return;
        }
        ChangeItemType item = xml.addNewChangeItem();
        item.setFieldtype(ChangeItemFieldtypeEnum.JIRA);
        item.setGroup(group.getUID());
        item.setId(this.uid);

        //somewhat complex way of dealing with a union enum type
        //create an object of the resulting type
        //then set it's value using one of the unioned enums values
        ChangeItemFieldEnum fieldEnum = ChangeItemFieldEnum.Factory.newInstance();
        fieldEnum.setObjectValue(field);
        item.xsetField(fieldEnum);

        //resolution, and possibly other fields, are cleared by not having
        //a newValue in the XML
        if (newValue != null) {
            item.setNewstring2(newValue.getName());
            item.setNewvalue2("" + newValue.getID());
        }

        NameIDPair oldValue = state.get(field);
        if (oldValue != null) {
            item.setOldstring2(oldValue.getName());
            item.setOldvalue2(""+oldValue.getID());
        }
    }

    public Map<ChangeItemFieldJiraEnum.Enum,NameIDPair>
                updateState(Map<ChangeItemFieldJiraEnum.Enum,NameIDPair> state) {
        state.put(field,newValue);
        return state;
    }

    /**
     *
     * @param state As input, the state before this change has taken place.
     * @return  true iff the state will be changed by this changeItem
     */
    public boolean testUpdate(Map<ChangeItemFieldJiraEnum.Enum,NameIDPair> state) {
        NameIDPair oldValue = state.get(field);
//System.out.println("oldValue="+oldValue+" newValue="+newValue);

        return !(newValue == null && oldValue == null)
                && !(newValue != null && newValue.equals(oldValue));

    }
}
