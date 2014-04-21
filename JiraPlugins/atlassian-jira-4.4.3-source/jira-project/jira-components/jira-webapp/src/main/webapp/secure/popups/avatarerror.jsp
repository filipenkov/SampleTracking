<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<ww:iterator value="flushedErrorMessages">
<div class="module message error">
<p><ww:property /></p>
<button class="cancel"><ww:text name="'admin.common.words.cancel'"/></button>
</div>
</ww:iterator>
<ww:iterator value="errors/values">
<div class="module message error">
<p><ww:property /></p>
<button class="cancel"><ww:text name="'admin.common.words.cancel'"/></button>
</div>
</ww:iterator>
