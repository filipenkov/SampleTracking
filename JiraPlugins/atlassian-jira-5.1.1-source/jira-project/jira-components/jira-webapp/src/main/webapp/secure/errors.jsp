<%@ page import="com.atlassian.jira.license.LicenseJohnsonEventRaiser"%>
<%@ page import="com.atlassian.johnson.JohnsonEventContainer"%>
<%@ page import="com.atlassian.johnson.event.Event"%>
<%@ page import="com.atlassian.johnson.event.EventType"%>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.johnson.event.EventLevel" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<!DOCTYPE html>
<html>
<head>
    <title><ww:text name="'system.error.access.constraints.title'"/></title>
    <meta http-equiv="refresh" content="30" />
    <meta name="decorator" content="none" />
    <style type="text/css">
        /*
         * Styles copied directly from AUI (aui-message, aui-table, etc.) to
         * avoid plugin resource corruption problems.
         */
        html, body {
            background: #F0F0F0;
            color: black;
            font-family: Arial, FreeSans, Helvetica, sans-serif;
            font-size: 13px;
            line-height: 1.3077;
            margin: 0;
            padding: 0;
        }

        body {
            min-width: 990px;
        }

        a, a:hover, a:visited {
            color: #326CA6;
            text-decoration: none;
        }

        a:hover {
            text-decoration: underline;
        }

        h1 {
            color: #292929;
            font-size: 25px;
            line-height: 1.16;
            margin: 0;
        }

        p {
            margin: 16px 0 0 0;
        }

        #content {
            background: #FFF;
            border: 1px solid #BBB;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            -moz-box-sizing: border-box;
            -ms-box-sizing: border-box;
            -o-box-sizing: border-box;
            -webkit-box-sizing: border-box;
            box-sizing: border-box;
            margin: 16px auto 0 auto;
            padding: 16px;
            width: 572px;
        }

        .message {
            background: #F0F0F0;
            border: 1px solid #BBB;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            color: #333;
            margin-top: 16px;
            padding: 1em 1em 1em 35px;
            position: relative;
        }

        .message p {
            margin-top: 0;
        }

        .message .icon {
            background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAKgAAAAYCAYAAABugbbBAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAABzhJREFUeNrsWc1vE0cUf7ux8+EAIYBICB8NiHJFBO7ABfVCTdprJZtLONp/Aki99ZIckwtG4tJD61rcOOUPIEGcKtHyLSWQL0wCCbEdb99vZsfZdXaXnTFVe9gnPXs87zdv1zO/efNmxpqenj577Nixv4jl+vXrFgXIgwcPHHwvLi5+OzEx8TcZypxl3eCvcoh5/KLj/KHrc2ZmxvvzCmuRNdsGq7BOss6qCv4fsfxb09NG/p1bt7T7x7o1F+nfmb44q+vz0sylPH/d1Wx289HEo5LpONOMdZA/8+6vEk04VVNXKZBzZGSEVldXqVQqOfl83kdS1MF++PBh/ASRLVNyZjKZ8rlz56irq8tn29nZoadPn5YZY0TSCxcu0OPHjyf37dtXOHr0KA0MDPjsHz58yC4tLWU/fvx4j7F5xuo9YGyMaH5+kvbvL1A6TbS25rcfOpSlej1LGxv3GJtnrAk5J6m7u0Dc13SQx1f1EfcNVatZWljIMuYekzSv6foujY4SyfH7sjAP6OXLu4JY5uSc5b46L35vbOS57oopSVP4ePfuHffJQcLgekmKMup44AWmk8jZ29tbHuWOAhmh7QLb8+fPjUjKhCvxJMotLCwQk9BnO3nyJPPnEPfXflpeXs4xtupGqfgyP1+i48dzgixv39Jv167RD6dPC9PvL17Qjw8fEg0PEx04kGOstn8mXol6e3N05owk5qtXRO/fS+PgINGJE0RnzxJ3UI6xVSap3vvzf6daLdyOsV1aIiYA0dAQdSi3KZM5T2qMUd7cvK3d567YWNY5utDKyjL76uPBHBTEhKKMOtiACUsB4pDz1KlT1Gw2OdDUAxU2YIB1U4G4cqW/vz83iIF0l26lkDdv3nDAW6NGo0HAMLbgLtWx/XOjnCBKGzkhKKMONoHR9C+W9XQ6x3+euBOIO0OQk0koVBAVdbABk04X3FQgvqB9mC4uCnLyki5JijrzpX2UbLsgyj99kipYxnWwmRAUH4iYa2vvObqvUU9Pj4g2UJRRB1v70q8h5WGOLjWewZ8/f45UYICNyFODpIjoDwIGCYgKkq6vrwsMsJqzuSiWXNe/l5xekgoBphP/iHJBkU7Vm/nfbd+uiJwrK5KcXqy5lDjC7a2VdUYpQ0oVQEBEzZ2dBvX19ancjaPzVifkbOWYIEkcwZKsKVlMpHrIzFebKDz/DC+hwAZsQiL9c1iPF1mAAVbXf3e33z/3P0fJVtlnA1bPv2zPY8mzFGkIEXJ097ePnAprFj1vUCp1mVcQYtL4bairVi8LzIRe+pbaS6YGv2OtVf4aoqIb55ZfSgdCI2Ec/0HR00tUE9/tg4acsz2Koq6jwcXyDVWyteUvNxqdDkCLjLyrl8/iXN1LTlHPe40OnjUpiB+2AsC2uoqTDi2C2t7deleXzemCTZ8+bQpFGXWwddI/dY2BqxsMsspjvxYuNIfjtAcboj/VBoYFZbFJwkZE4Uz9e9q3ctAQu+kEE6QMImcmQ7FXir3R8zZH9m+IA0xge9TBBgywugSVBGyyD4cn7BZHzqZQlFEHWyckVaRAhIxSU4Iid63FyJ3i4kIJJNMD+vXZs5apVYbNlEB4pygCem2m7++RPeTEu+P4zOT9Nxcui5zY+/8dXuLv90tF2d9/RbdNPILiEL7ZRFh3+L/XBWeRc8q803brHAJGHdibEDTDMzSOGhC0gokUZ/kGZksunxUd/yKn8gzenbk5cYAPRdlHIpl/6fnf3pZLq9KgJVopsHr+ZTsmoCCj/xBfEjOVin5+lNw/fpMj44BIG1T7/iOe/PPIbj0wwKKNTg7abO6IY6t0use3IVIbp3p9myzzbdI479DL3TK5jxXl0EYn92H/2fZNUdhEwWkByVuf+LnV9rZvUxJ4DqomliSQnv9aLRvZwd5zY9k/k1ojgPY4X+U9AEjZykVBTNQHnEtrSK41CVpRdXP3iAlRdK9/tIl14WDj+nJoaISGh0cCd+uogw0YYHXfHofuzWZzfJsHDhEsSoEBVvOgfpbbTKEtJkGUuv6nyHMlGcc/d/BU65gnKkoBA6yGf3F96ThTvigqz0d3d/Le6MlY7StPb2TkfYUgp23vjc4mEVRNgLgrgOZkSLl365Hx0eSAvp2kuCFiEpZt2w7ZyIpdrOlVZxHXl+yjYIVEIoejB+uUwmrJ2FhRXF9aVgGRTkRM32m7uzkA0RRWQ3AzJMjYaAj/1N5HGFR5AjKlfYvUHoHV+8JfZ5FTyRP2c963AiBtQORUZe+phPwfT7R38f+2uMQbBxGD1JScEJdwRSbgVfZVCfBfgQ0YbXJCJOGKBB+OUwlgf4Vc/yb38Iqk/CX9q2MnpfKZV43ISXRTkEJH0SauWNb3/PkL6+uWrq+/ZlJKRdlrI/qZ9bvY7h3HoUQS+b+KnXRBIglBE0kkIWgiCUETSSQhaCKJJARNJCFoIokkBE0kIWgiifx38o8AAwALTO1zkLnsWgAAAABJRU5ErkJggg==");
            background-repeat: no-repeat;
            display: block;
            height: 14px;
            left: 10px;
            position: absolute;
            top: 0.9em;
            width: 14px;
        }

        .message.error {
            background: #FFE7E7;
            border-color: #DF9898;
        }

        .message.error .icon {
            background-position: -24px 0;
        }

        .message.success {
            background: #DDFADE;
            border-color: #93C49F;
        }

        .message.success .icon {
            background-position: -120px 0;
        }

        .message p {
            margin: 0;
        }

        table {
            border-collapse: collapse;
            margin-top: 16px;
            width: 100%;
        }

        thead {
            border-bottom: 1px solid #BBB;
        }

        th {
            color: #999;
            font-weight: normal;
            padding: 0.5em 0.4em 0.5em 0.6em;
            text-align: left;
            vertical-align: top;
        }

        tbody > tr {
            border-bottom: 1px solid #DDD;
        }

        td {
            padding: 0.5em 0.4em 0.5em 0.6em;
            vertical-align: top;
        }

        pre {
            margin: 0;
        }
    </style>
</head>
<body>
    <div id="content">
        <header>
            <h1><ww:text name="'system.error.access.constraints.title'"/></h1>
        </header>
        <%
            JohnsonEventContainer appEventContainer = JohnsonEventContainer.get(pageContext.getServletContext());

            // If there are events outstanding, then display them in a table.
            if (appEventContainer.hasEvents() ) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        %>
            <div class="message error">
                <span class="icon"></span>
                <p><ww:text name="'system.error.access.constrinats.desc'"/></p>
            </div>
            <table>
                <thead>
                    <tr>
                        <th width="40%">
                            <ww:text name="'common.words.description'"/>
                        </th>
                        <th width="10%">
                            <ww:text name="'common.words.time'"/>
                        </th>
                        <th>
                            <ww:text name="'common.words.level'"/>
                        </th>
                        <th width="40%">
                            <ww:text name="'common.words.exception'"/>
                        </th>
                    </tr>
                </thead>
                <tbody>
                <%
                    com.atlassian.jira.web.util.ExternalLinkUtil externalLinkUtil = com.atlassian.jira.web.util.ExternalLinkUtilImpl.getInstance();
                    boolean onlyWarnings = true;
                    final Collection events  = appEventContainer.getEvents();
                    for (Iterator iterator = events.iterator(); iterator.hasNext();)
                    {
                        Event event = (Event) iterator.next();
                        onlyWarnings &= EventLevel.WARNING.equals(event.getLevel().getLevel());
                %>
                    <tr>
                        <td>
                        <% if (EventType.get("export-illegal-xml").equals(event.getKey())) { %>
                            <ww:component template="help.jsp" name="'autoexport'"><ww:param name="'helpURLFragment'"/></ww:component><br/>
                        <% } %>
                        <%= event.getDesc() %><br/>
                        <% if (event.hasProgress()) {%>
                            <br/><ww:text name="'system.error.progress.completed'">
                                <ww:param name="value0"><%=event.getProgress()%></ww:param>
                            </ww:text>
                        <%}%>
                        <% if (EventType.get(LicenseJohnsonEventRaiser.LICENSE_INVALID).equals(event.getKey()))
                           { %>
                           <br/><a href="<%= request.getContextPath() %>/secure/ConfirmInstallationWithLicense!default.jspa"><ww:text name="'system.error.edit.license'"/></a>
                        <% }
                           else if (EventType.get(LicenseJohnsonEventRaiser.LICENSE_TOO_OLD).equals(event.getKey()))
                           { %>
                           <br/><a href="<%= request.getContextPath() %>/secure/ConfirmNewInstallationWithOldLicense!default.jspa"><ww:text name="'system.error.edit.license.or.evaluate'"/></a>
                        <% }
                           else if (EventType.get("export-illegal-xml").equals(event.getKey()))
                           { %>
                           <br/><a href="<%= request.getContextPath() %>/secure/CleanData!default.jspa"><ww:text name="'system.error.clean.characters.from.database'"/></a><br/>
                           <ww:text name="'system.error.disable.export.on.upgrade.desc'">
                               <ww:param name="value0"><b></ww:param>
                               <ww:param name="value1"></b></ww:param>
                           </ww:text> &nbsp;
                        <% }
                           else if (EventType.get("index-lock-already-exists").equals(event.getKey()))
                           { %>
                            <p>
                                <ww:text name="'system.error.unexpected.index.lock.found.desc1'"/>
                                <br/>
                                <br/>
                                <%
                                   Object lockFiles = event.getAttribute("lockfiles");
                                   if (lockFiles != null)
                                   {
                                       out.println(lockFiles);
                                   }
                                %>
                                <br/>
                                <br/>
                                <ww:text name="'system.error.unexpected.index.lock.found.desc2'"/>
                            </p>
                            <p>
                                <ww:text name="'system.error.unexpected.index.lock.found.desc3'">
                                    <ww:param name="value0"><strong></ww:param>
                                    <ww:param name="value1"></strong></ww:param>
                                </ww:text>
                            </p>
                        <% }
                           else if (EventType.get("upgrade").equals(event.getKey()))
                           {
                               String exportFilePath = ComponentManager.getInstance().getUpgradeManager().getExportFilePath();
                               if (TextUtils.stringSet(exportFilePath))
                               {
                               %>
                               <br/>
                                <ww:text name="'system.error.data.before.upgrade.exported.to'">
                                    <ww:param name="value0"><%= exportFilePath %></ww:param>
                                </ww:text>
                            <% } %>
                        <% } %>
                            <!-- (<ww:text name="'system.error.type'">
                                    <ww:param name="value0"><%= event.getKey().getType() %></ww:param>
                                </ww:text>) -->

                        </td>
                        <td><%=event.getDate()%></td>
                        <td><%=event.getLevel().getLevel()%> </td>
                        <td><pre><%= event.getException() == null ? "" : event.getException() %></pre></td>
                    </tr>
                <% }

                    if (onlyWarnings)
                    {
                        response.setHeader("Retry-After", "30");
                    }
                %>
                </tbody>
            </table>
        <% } else { %>
            <div class="message success">
                <span class="icon"></span>
                <p><ww:text name="'system.error.no.problems.accessing.jira'"/></p>
            </div>
            <p><a href="<%=request.getContextPath()%>/secure/MyJiraHome.jspa"><ww:text name="'system.error.go.to.jira.home'"/></a></p>
        <% } %>
    </div>
</body>
</html>
