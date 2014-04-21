<%@ taglib prefix="ww" uri="webwork" %>
<script type="text/javascript">window.dhtmlHistory.create();</script>
<div class="item-header" id="content-top">
    <jsp:include page="viewprofile-tools.jsp" />
    <h1 class="item-name avatar">
        <ww:if test="/userAvatarEnabled == true">
            <img class="avatar-image" src="<ww:url page="/secure/useravatar"><ww:param name="'size'" value="'large'"/><ww:param name="'ownerId'" value="user/name"/><ww:param name="'avatarId'" value="/avatarId(user)"/></ww:url>" width="48" height="48">
        </ww:if>
        <ww:else>
            <img id="project-avatar" alt="" class="project-avatar-48" height="48" src="<%= request.getContextPath() %>/images/icons/Avatar-default.png" width="48" />
        </ww:else>
        <span><ww:text name="'common.concepts.profile'"/>: <ww:property value="user/fullName"/></span>
    </h1>
    <h2 class="item-summary" id="up-user-title"><ww:text name="'common.concepts.profile'"/>: <span id="up-user-title-name"><ww:property value="user/fullName"/></span></h2>
</div>

<div id="main-content">
    <jsp:include page="viewprofile-tabs.jsp" />
    <div class="active-area">
        <h2 id="up-tab-title"><ww:property value="/labelForSelectedTab"/></h2>
        <jsp:include page="viewprofile-content.jsp" />
    </div>
</div>
