<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>

<html>
<head>
    <meta content="frontpage" name="decorator"/>
</head>
<body>
<h2><ww:text name="'websudo.title'"/> - <ww:text name="'websudo.retry.name'"/></h2>

<div class="aui-message warning">
    <p>
    <span class="aui-icon icon-warning"></span>
        <ww:text name="'websudo.retry.message'"/>
    </p>

    <p>
        <em><ww:text name="'xsrf.retry.note2'"/></em>
    </p>
</div>



<page:applyDecorator id="resubmit-form" name="auiform">
    <page:param name="action"><%= request.getContextPath() %><ww:property value="/webSudoDestination"/></page:param>
    <page:param name="method">post</page:param>

    <ww:iterator value="/requestParameters">
        <ww:iterator value="./value">
                <ww:component name="../key" value="." template="hidden.jsp"/>
        </ww:iterator>
    </ww:iterator>
    <input type="submit" name="retry_button" value="<ww:text name="'websudo.retry.name'"/>"/>

</page:applyDecorator>

</body>
</html>
