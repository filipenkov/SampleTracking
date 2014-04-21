<%@ taglib prefix="ww" uri="webwork" %>
<html>
    <head>
        <meta name="admin.active.section" content="admin_system_menu/top_system_section/user_interface"/>
        <meta name="admin.active.tab" content="edit_default_dashboard"/>
        <title><ww:text name="'admin.globalsettings.defaultdashboard.configure.default.dashboard'" /></title>
        <%-- THIS IS A TOTAL HACK FOR SUMMIT! Once dashboards implements tabs properly: REMOVE!--%>
        <style type="text/css">
            .dashboard .dashboard-title {
                display:none !important;
            }

            .dashboard ul.menu {
                margin:0 !important;
            }
        </style>
        <%-- END TOTAL HACK--%>
    </head>
    <body>
        <div class="jiraform maxWidth">
            <div class="jiraformheader">
                <h3 class="formtitle"><ww:text name="'admin.globalsettings.defaultdashboard.configure.default.dashboard'" /></h3>
                <br/>
                <ww:text name="'admin.globalsettings.defaultdashboard.note'" value0="'<span class=\"note\">'" value1="'</span>'"/>
            </div>
            <div class="jiraformcontents"><ww:property value="/dashboardHtml" escape="false"/></div>
        </div>
    </body>
    <%--## Need this at the bottom to make sure it overrides the dashboard decorator--%>
    <meta name="decorator" content="admin">
</html>
