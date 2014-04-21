<%@ taglib uri="webwork" prefix="ww"%>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib prefix="decorator" uri="sitemesh-decorator" %>
<%-- Deprecated in 5.0 - will be removed in 6.0 --%>
<%-- Was only used as a temporary workaround for the attach file popup which is no longer in use --%>
<%-- IE 7 & 8 won't parse the HTML properly when used as the content of an <iframe> unless it has a <body>. --%>
<body>
    <fieldset class="hidden parameters">
        <input type="hidden" id="closelink" value="<ww:text name="'admin.common.words.close'"/>"/>
    </fieldset>
    <div class="aui-dialog-content">
        <decorator:body />
    </div>
</body>
