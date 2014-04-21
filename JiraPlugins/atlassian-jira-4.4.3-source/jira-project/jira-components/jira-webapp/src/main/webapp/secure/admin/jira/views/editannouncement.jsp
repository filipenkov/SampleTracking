<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/user_interface"/>
    <meta name="admin.active.tab" content="edit_announcement"/>
    <title><ww:text name="'admin.announcement.edit.announcement.banner'"/></title>
</head>
<body>
    <page:applyDecorator name="jiraform">
        <page:param name="action">EditAnnouncementBanner.jspa</page:param>
        <page:param name="submitId">set_banner</page:param>
        <page:param name="submitName"><ww:text name="'admin.announcement.set.banner'"/></page:param>
        <ww:if test="/preview == true">
            <page:param name="buttons">
                <input class="toolbar-trigger" type="button" value="<ww:text name="'admin.announcement.reset'"/>" onclick="location.href='EditAnnouncementBanner!default.jspa'"/>
            </page:param>
        </ww:if>

        <page:param name="autoSelectFirst">false</page:param>
        <page:param name="title"><ww:text name="'admin.announcement.edit.announcement.banner'"/></page:param>
        <page:param name="description">
            <p><ww:text name="'admin.announcement.description'"/></p>
        </page:param>

        <ui:textarea label="text('admin.announcement')" name="'announcement'" rows="'8'" cols="'60'">
            <ui:param name="'description'"><ww:text name="'admin.announcement.close.tags'"/></ui:param>
            <ui:param name="'cssId'">announcement</ui:param>
        </ui:textarea>
        <ui:radio label="text('admin.announcement.level')" name="'bannerVisibility'" list="visibilityModes" listKey="'id'" listValue="'name'"/>
    </page:applyDecorator>
    <script type="text/javascript">
        AJS.$(function() {
            AJS.$("#previewButton").click(function(e) {
                e.preventDefault();
                AJS.$("form[name=jiraform]").append(
                    AJS.$("<input/>").attr("name", "announcement_preview_banner_st").attr("type", "hidden").val(AJS.$("#announcement").val()));
                AJS.$("form[name=jiraform]").attr("action", "EditAnnouncementBanner!default.jspa").submit();
            });
        });
    </script>
</body>
</html>
