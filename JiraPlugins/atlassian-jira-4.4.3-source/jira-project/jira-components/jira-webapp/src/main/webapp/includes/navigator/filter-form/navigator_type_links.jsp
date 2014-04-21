<%@ taglib uri="webwork" prefix="ww" %>
<ul id="filter-switch" class="filter-type item-details">
    <li>
        <ww:if test="/navigatorTypeAdvanced == false">
            <ww:text name="'navigator.filter.switch.advanced'">
                <ww:param name="'value0'"><a id="switchnavtype" href="IssueNavigator!switchView.jspa?navType=advanced<ww:if test="$createNew == true">&createNew=true</ww:if>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </ww:if>
        <ww:else>
            <ww:if test="/currentQueryTooComplex == true">
                <strong><ww:text name="'jira.jql.query.too.complex'"/></strong>
                <ww:text name="'jira.jql.query.too.complex.create.new'">
                    <ww:param name="'value0'"><a href="IssueNavigator.jspa?mode=show&createNew=true"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </ww:if>
            <ww:else>
                <ww:text name="'navigator.filter.switch.simple'">
                    <ww:param name="'value0'"><a id="switchnavtype" href="IssueNavigator!switchView.jspa?navType=simple<ww:if test="$createNew == true">&createNew=true</ww:if>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </ww:else>
        </ww:else>
    </li>
</ul>
