<%@ taglib uri="webwork" prefix="ww" %>
<table class="typeA" border="0" cellpadding="0" cellspacing="0" width="100%">
	<thead>
        <tr>
            <th>
                &nbsp;
            </th>
            <th>
                <ww:text name="'trackback.manage.colheader'"/>
            </th>
            <th>
                &nbsp;
            </th>
        </tr>
    </thead>
	<ww:iterator value="trackbacks" status="'status'">
        <tbody>
            <tr<ww:if test="@status/even == true"> class="alt"</ww:if>>
                <td><ww:property value="@status/count"/></td>
                <td>
                    <div class="trackback">
                        <div class="trackbackheader">
                            <img border="0" src="<%=request.getContextPath()%>/images/icons/document_exchange.gif" width=16 height=16 align=absmiddle>
                            <span class="trackbacklink"><a href="<ww:property value="url"/>"><ww:property value="title"/></a></span>
                            <span class="trackbackblogname">(<ww:property value="blogName"/>)</span>
                        </div>
                        <ww:if test="excerpt">
                            <div class="trackbackexcerpt"><ww:property value="excerpt" /></div>
                        </ww:if>
                    </div>
                </td>
                <td>
                    <ww:if test="/canDeleteTrackbacks">
                    <a href="<ww:url page="DeleteTrackback.jspa"><ww:param name="'id'" value="/issueObject/id" /><ww:param name="'trackbackId'" value="./id" /></ww:url>" id="del_<ww:property value="id" />"><ww:text name="'common.words.delete'"/></a>
                    </ww:if>
                </td>
            </tr>
        </tbody>
	</ww:iterator>
</table>
