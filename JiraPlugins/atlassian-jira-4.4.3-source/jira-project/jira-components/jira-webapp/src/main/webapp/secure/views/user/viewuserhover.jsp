<%@ taglib prefix="ww" uri="webwork" %>

<div class="hoverpopup" id="<ww:property value="/username"/>_user_hover">
    <div class="user-hover-info">
        <div class="user-hover-avatar">
            <ww:if test="/currentUserNoAvatar == true">
                <a id="update_avatar_link" class="hover_profile_link"
                   title="<ww:text name="'admin.user.edit.your.avatar.description'"/>"
                   href="<ww:url page="/secure/ViewProfile.jspa"><ww:param name="'name'" value="username"/></ww:url>">
                    <img class="avatar-image" src="<ww:url page="/images/icons/ico_add_avatar.png" />" width="48" height="48">
                </a>
            </ww:if>
            <ww:else>
                <a id="avatar-image-link" title="<ww:property value="/username"/>" href="<ww:url page="/secure/ViewProfile.jspa"><ww:param name="'name'" value="username"/></ww:url>">
                   <img class="avatar-image" src="<ww:url page="/secure/useravatar"><ww:param name="'size'" value="'large'"/><ww:param name="'ownerId'" value="username"/><ww:param name="'avatarId'" value="/avatarId"/></ww:url><ww:if test="/remoteUser == null">&loggedout=true</ww:if>" width="48" height="48">
                </a>
            </ww:else>
        </div>
        <div class="user-hover-details">
            <h4>
                <ww:if test="/user != null">
                    <a id="avatar-full-name-link" title="<ww:property value="/username"/>" href="<ww:url page="/secure/ViewProfile.jspa"><ww:param name="'name'" value="username"/></ww:url>">
                        <ww:property value="/user/fullName"/>
                    </a>
                </ww:if>
                <ww:else>
                    <ww:property value="/username"/>
                </ww:else>
            </h4>
            <ww:if test="/remoteUser != null">
                <h5>
                    <ww:property value="/formattedEmail" escape="false"/>
                </h5>
                <ww:if test="/user != null">
                    <h5 class="time-zone-info" style="white-space: nowrap; overflow: hidden;">
                        <span class="user-time-icon hour-of-day-<ww:property value="/hourOfDay"/>"></span>
                        <div class="user-time-text">
                            <ww:text name="'user.hover.timeofday'">
                                <ww:param value="/time"/>
                                <ww:if test="/isWeekend">
                                    <ww:param value="'<span class=\"weekend\">'"/>
                                </ww:if>
                                <ww:else>
                                    <ww:param value="'<span>'"/>
                                </ww:else>
                                <ww:param value="/dayOfWeek"/>
                                <ww:param value="'</span>'"/>
                                <ww:param value="/timeZoneCity"/>
                            </ww:text>
                        </div>
                    </h5>
                </ww:if>
            </ww:if>
        </div>
    </div>
    <ww:if test="/hasViewUserPermission == true">
        <div class="user-hover-buttons">
            <ul>
                <ww:if test="/hoverLinks/size <= 2">
                    <ww:iterator value="hoverLinks"><li><a class="user-hover-more" id="<ww:property value="./id"/>" title="<ww:property value="./title"/>" href="<ww:property value="./url"/>"><ww:property value="./label"/></a></li></ww:iterator>
                </ww:if>
                <ww:else>
                    <li>
                        <ww:property value="/firstHoverLink">
                            <a class="user-hover-more" id="<ww:property value="./id"/>" title="<ww:property value="./title"/>" href="<ww:property value="./url"/>"><ww:property value="./label"/></a>
                        </ww:property>
                    </li><li class="aui-dd-parent">
                     <a id="user-hover-more-trigger" class="user-hover-more lnk aui-dd-link standard" href="#" hidefocus title="<ww:text name="'common.concepts.more'"/>"><span><ww:text name="'common.concepts.more'"/></span></a>
                    <div id="user-hover-more-dropdown" class="aui-list hidden">
                        <ul>
                             <ww:iterator value="/remainingLinks">
                                 <li class="aui-list-item">
                                     <a class="aui-list-item-link" id="<ww:property value="./id"/>" title="<ww:property value="./title"/>" href="<ww:property value="./url"/>"><ww:property value="./label"/></a>
                                 </li>
                             </ww:iterator>
                         </ul>
                    </div>
                 </li>
                </ww:else>
            </ul>
        </div>
    </ww:if>
</div>