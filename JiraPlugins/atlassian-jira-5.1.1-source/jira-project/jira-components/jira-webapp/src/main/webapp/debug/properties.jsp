<%@ taglib uri="webwork" prefix="ww" %>

<table class="aui">
    <thead>
        <tr>
            <th>&nbsp;</th>
            <th>Key</th>
            <th>Value</th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="." status="'rowStatus'">
        <tr>
            <td><ww:property value="." /></td>
            <td><ww:property value="key" /></td>
            <td><ww:property value="value" /></td>
        </tr>
    </ww:iterator>
    </tbody>
</table>