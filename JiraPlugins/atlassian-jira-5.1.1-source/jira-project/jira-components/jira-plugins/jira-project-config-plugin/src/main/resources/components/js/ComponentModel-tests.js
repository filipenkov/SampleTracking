AJS.test.require("com.atlassian.jira.jira-project-config-plugin:project-config-components");

test("getInvalidAssigneeMsg", function () {

    var componentModel = new JIRA.Admin.Component.ComponentModel();


    ok(!componentModel.getAssigneeInvalidMsg(), "Expected Undefined to be returned if the assignee is valid");

    componentModel.set({
        assigneeType: "UNASSIGNED"
    });

    equals(componentModel.getAssigneeInvalidMsg(), "Unassigned not allowed");

    componentModel.set({
        assigneeType: "COMPONENT_LEAD",
        assignee: {
            active: true,
            name: "scott",
            displayName: "Scott"
        }
    });

    equals(componentModel.getAssigneeInvalidMsg(), "Component Lead (Scott) not assignable");

    componentModel.set({
        assigneeType: "COMPONENT_LEAD",
        assignee: {
            name: "scott",
            displayName: "Scott",
            active: false
        }
    });

    equals(componentModel.getAssigneeInvalidMsg(), "Component Lead (Scott) does not exist");

    componentModel.set({
        assigneeType: "PROJECT_LEAD",
        assignee: {
            active: true,
            name: "scott",
            displayName: "Scott"
        }
    });

    equals(componentModel.getAssigneeInvalidMsg(), "Project Lead (Scott) not assignable");

    componentModel.set({
        assigneeType: "PROJECT_LEAD",
        assignee: {
            name: "scott",
            displayName: "Scott",
            active: false
        }
    });

    equals(componentModel.getAssigneeInvalidMsg(), "Project Lead (Scott) does not exist");

    componentModel.set({
        assigneeType: "PROJECT_DEFAULT",
        assignee: {
            active: true,
            name: "scott",
            displayName: "Scott"
        }
    });

    equals(componentModel.getAssigneeInvalidMsg(), "Project Default (Scott) not assignable or does not exist");

});