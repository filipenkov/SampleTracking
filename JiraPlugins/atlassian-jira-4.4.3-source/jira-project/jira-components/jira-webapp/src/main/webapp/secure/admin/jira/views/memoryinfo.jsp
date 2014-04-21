<%@ taglib uri="webwork" prefix="ww" %>
<ww:bean id="math" name="'com.atlassian.core.bean.MathBean'"/>

<html>
<head>
	<title><ww:text name="'admin.systeminfo.memory.info'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="system_info"/>
</head>

<body>
<p>
<table border="0" bgcolor="#bbbbbb" cellpadding="3" cellspacing="1" width="100%">

	<tr bgcolor="#f0f0f0">
		<td class="colHeaderLink" colspan="2">
			<h3 class="formtitle"><ww:text name="'admin.systeminfo.java.vm.memory.statistics'"/></h3>
		</td>
	</tr>
    <tr bgcolor="#ffffff">
		<td valign="top" width="40%">&nbsp; <b><ww:text name="'admin.systeminfo.memory.graph'"/></b></td>
		<td valign="top" width="60%">
            <table  border="0" cellpadding="0" cellspacing="0" style="float:left;" width="40%">
                <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)">
                    <td bgcolor="#CC3333" width="<ww:property value="."/>%">
                        <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                 alt=""
                                 height="15"
                                 width="100%"
                                 border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                             <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                         </ww:text>">
                        </a>
                    </td>
                </ww:property>

                <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)">
                <td bgcolor="#00CC00" width="<ww:property value="."/>%">
                    <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                             alt=""
                             height="15"
                             width="100%"
                             border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                             <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                         </ww:text>">
                    </a>
                </td>
            </table>
            <b>&nbsp;&nbsp;<ww:text name="'admin.systeminfo.memory.percent.free'">
                <ww:param name="'value0'"><ww:property value="."/> </ww:param>
            </ww:text> </b>
            (<ww:text name="'common.words.used'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/usedMemory"/> MB
            <ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalMemory"/> MB)
            &nbsp;&nbsp;<span class="small">(<a href="ViewSystemInfo!garbageCollection.jspa"><ww:text name="'admin.systeminfo.force.garbage.collection'"><ww:param name="value0">garbage collection</ww:param></ww:text></a>)</span>
        </td>
                </ww:property>
        </td>
    </tr>

    <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory != 0">
    <tr bgcolor="#fffff0">
        <td valign="top" width="40%">&nbsp; <b><ww:text name="'admin.systeminfo.perm.gen.memory.graph'"/></b></td>
        <td valign="top" width="60%">
            <table  border="0" cellpadding="0" cellspacing="0" style="float:left;" width="40%">
                <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)">
                    <td bgcolor="#CC3333" width="<ww:property value="."/>%">
                        <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                 alt=""
                                 height="15"
                                 width="100%"
                                 border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                             <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                         </ww:text>">
                        </a>
                    </td>
                </ww:property>

                <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freePermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)">
                <td bgcolor="#00CC00" width="<ww:property value="."/>%">
                    <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                             alt=""
                             height="15"
                             width="100%"
                             border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                             <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                         </ww:text>">
                    </a>
                </td>
            </table>
            <b>&nbsp;&nbsp;<ww:text name="'admin.systeminfo.memory.percent.free'">
                <ww:param name="'value0'"><ww:property value="."/> </ww:param>
            </ww:text> </b>
            (<ww:text name="'common.words.used'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory"/> MB
            <ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory"/> MB)
        </td>
        </ww:property>
        </td>
    </tr>
    </ww:if>
    <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory != 0">
    <tr bgcolor="#ffffff">
        <td valign="top" width="40%">&nbsp; <b><ww:text name="'admin.systeminfo.nonheap.memory.graph'"/></b></td>
        <td valign="top" width="60%">
            <table  border="0" cellpadding="0" cellspacing="0" style="float:left;" width="40%">
                <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)">
                    <td bgcolor="#CC3333" width="<ww:property value="."/>%">
                        <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                 alt=""
                                 height="15"
                                 width="100%"
                                 border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                             <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                         </ww:text>">
                        </a>
                    </td>
                </ww:property>

                <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)">
                <td bgcolor="#00CC00" width="<ww:property value="."/>%">
                    <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                             alt=""
                             height="15"
                             width="100%"
                             border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                             <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                         </ww:text>">
                    </a>
                </td>
            </table>
            <b>&nbsp;&nbsp;<ww:text name="'admin.systeminfo.memory.percent.free'">
                <ww:param name="'value0'"><ww:property value="."/> </ww:param>
            </ww:text> </b>
            (<ww:text name="'common.words.used'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory"/> MB
            <ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory"/> MB)
        </td>
        </ww:property>
        </td>
    </tr>
    </ww:if>
</table>
</p>
<p>
<table border="0" bgcolor="#bbbbbb" cellpadding="3" cellspacing="1" width="100%">

    <tr bgcolor="#f0f0f0">
        <th class="colHeaderLink" colspan="4">
            <h3 class="formtitle"><ww:text name="'admin.systeminfo.memory.pool.list'"/></h3>
        </th>
    </tr>
    <tr>
        <td colspan="4" bgcolor="#ffffff"><ww:text name="'admin.systeminfo.memory.pool.description'"/></td>
    </tr>
    <ww:iterator value="/runtimeInformation/memoryPoolInformation" status="'status'">
    <tr bgcolor="<ww:if test="@status/odd == false">#ffffff</ww:if><ww:else>#fffff0</ww:else>">
        <td valign="top" width="40%">&nbsp; <strong><ww:property value="name"/></strong></td>
        <td valign="top" width="60%">
            <table  border="0" cellpadding="0" cellspacing="0" style="float:left;" width="40%">
                <ww:property value="@math/percentage(used, total)">
                    <td bgcolor="#CC3333" width="<ww:property value="."/>%">
                        <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                 alt=""
                                 height="15"
                                 width="100%"
                                 border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                             <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                         </ww:text>">
                        </a>
                    </td>
                </ww:property>

                <ww:property value="@math/percentage(free, total)">
                <td bgcolor="#00CC00" width="<ww:property value="."/>%">
                    <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                             alt=""
                             height="15"
                             width="100%"
                             border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                             <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                         </ww:text>">
                    </a>
                </td>
            </table>
            <b>&nbsp;&nbsp;<ww:text name="'admin.systeminfo.memory.percent.free'">
                <ww:param name="'value0'"><ww:property value="."/> </ww:param>
            </ww:text> </b>
            (<ww:text name="'common.words.used'"/>: <ww:property value="used"/> MB
            <ww:text name="'common.words.total'"/>: <ww:property value="total"/> MB)
        </td>
        </ww:property>
        </td>
    </tr>
    </ww:iterator>
</table>

</p>
</body>
</html>
