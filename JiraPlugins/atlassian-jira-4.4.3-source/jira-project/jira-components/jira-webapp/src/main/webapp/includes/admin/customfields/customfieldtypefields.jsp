<%@ taglib uri="webwork" prefix="ui" %>

    <ui:textfield label="'Field Name'" name="'name'" size="50" >
        <ui:param name="'mandatory'" value="'true'" />
    </ui:textfield>
    <ui:textarea label="'Description'" name="'description'" rows="5" cols="50">
    <ui:param name="'description'">
                A description of this particular custom field.<br>
                You can include HTML, make sure to close all your tags.
        </ui:param>
    </ui:textarea>
	<ui:textfield label="'Implementation Class'" name="'className'" size="50" >
        <ui:param name="'mandatory'" value="'true'" />
    </ui:textfield>
	<ui:textfield label="'Edit Template'" name="'editTmpl'" size="50" >
        <ui:param name="'mandatory'" value="'true'" />
    </ui:textfield>
	<ui:textfield label="'View Template'" name="'viewTmpl'" size="50" >
        <ui:param name="'mandatory'" value="'true'" />
    </ui:textfield>
	<ui:textfield label="'Search Template'" name="'searchTmpl'" size="50" />
