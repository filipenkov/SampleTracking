<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<script language="JavaScript" type="text/javascript" src="<%=request.getContextPath()%>/includes/js/logger.js"></script>
<link type="text/css" rel="StyleSheet" href="<ww:url value="'/styles/logger.css'" />" />

<div id="fvlogger">
  <dl>
    <dt>fvlogger</dt>
    <dd class="all">
      <a href="#fvlogger" onclick="showAll();return false;"
        title="show all">all</a>
    </dd>
    <dd class="debug">
      <a href="#fvlogger" onclick="showDebug();return false;"
        title="show debug">debug</a>
    </dd>
    <dd class="info">
      <a href="#fvlogger" onclick="showInfo();return false;"
        title="show info">info</a>
    </dd>
    <dd class="warn">
      <a href="#fvlogger" onclick="showWarn();return false;"
        title="show warnings">warn</a>
    </dd>
    <dd class="error">
      <a href="#fvlogger" onclick="showError();return false;"
        title="show errors">error</a>
    </dd>
    <dd class="fatal">
      <a href="#fvlogger" onclick="showFatal();return false;"
        title="show fatals">fatal</a>
    </dd>
    <dd>
      <a href="#fvlogger" onclick="eraseLog(true);return false;"
        title="erase">erase</a>
      </dd>
  </dl>
</div>
