<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<ww:bean name="'com.atlassian.core.user.UserUtils'" id="userUtils" />
<% request.setAttribute("contextPath", request.getContextPath()); %>

<ww:if test="/areAttachmentsEmpty == false">
<ww:bean name="'com.atlassian.core.util.FileSize'" id="sizeFormatter" />
	<div id="attachmentmodule" class="module toggle-wrap">
		<div class="mod-header">
            <ul class="ops">
            <ww:if test="/canCreateAttachments == true">
                <li><a id="add-attachments-link" href="<ww:url page="/secure/AttachFile!default.jspa"><ww:param name="'id'" value="../long('id')" /></ww:url>" class="issueaction-attach-file icon icon-add16" title="<ww:text name="'admin.issue.operations.plugin.attach.file.name'"/>"><span><ww:text name="'admin.issue.operations.plugin.attach.file.name'"/></span></a></li>
            </ww:if>
                <li class="drop" id="attachment-sorting-options">
                    <div class="aui-dd-parent">
                        <a href="#" id="attachment-sort-key-list" class="icon drop-menu aui-dropdown-trigger" title="<ww:text name="'viewissue.attachments.sort.options'"/>"><span><ww:text name="'viewissue.attachments.sort.options'"/></span></a>
                        <div class="aui-dropdown-content aui-list aui-list-checked">
                            <ul class="aui-list-section">
                                <li class="aui-list-item<ww:if test="/attachmentSortBy == 'fileName'">  aui-checked</ww:if>">
                                    <a id="attachment-sort-key-name" class="aui-list-item-link" rel="nofollow"
                                       href="<ww:url value="'/browse/' + issue/string('key')" atltoken="false"><ww:param name="'attachmentSortBy'" value="'fileName'"/></ww:url>#attachmentmodule"
                                       title="<ww:text name="'viewissue.attachments.sort.key.name'"/>">
                                        <ww:text name="'viewissue.attachments.sort.key.name'"/>
                                    </a>
                                </li>
                                <li class="aui-list-item<ww:if test="/attachmentSortBy == 'dateTime'">  aui-checked</ww:if>">
                                    <a id="attachment-sort-key-date" class="aui-list-item-link" rel="nofollow"
                                       href="<ww:url value="'/browse/' + issue/string('key')" atltoken="false"><ww:param name="'attachmentSortBy'" value="'dateTime'"/></ww:url>#attachmentmodule"
                                       title="<ww:text name="'viewissue.attachments.sort.key.date'"/>">
                                        <ww:text name="'viewissue.attachments.sort.key.date'"/>
                                    </a>
                                </li>
                            </ul>
                            <ul class="aui-list-section <ww:if test="/zipSupport == false && /attachable == false"> last</ww:if>" id="attachment-sort-direction-list">
                                <li class="aui-list-item<ww:if test="/attachmentOrder == 'asc'"> aui-checked</ww:if>">
                                    <a id="attachment-sort-direction-asc" class="aui-list-item-link" rel="nofollow"
                                       href="<ww:url value="'/browse/' + issue/string('key')" atltoken="false"><ww:param name="'attachmentOrder'" value="'asc'"/></ww:url>#attachmentmodule"
                                       title="<ww:text name="'viewissue.attachments.sort.direction.asc'"/>">
                                        <ww:text name="'viewissue.attachments.sort.direction.asc'"/>
                                    </a>
                                </li>
                                <li class="aui-list-item<ww:if test="/attachmentOrder == 'desc'">  aui-checked</ww:if>">
                                    <a id="attachment-sort-direction-desc" class="aui-list-item-link" rel="nofollow"
                                       href="<ww:url value="'/browse/' + issue/string('key')" atltoken="false"><ww:param name="'attachmentOrder'" value="'desc'"/></ww:url>#attachmentmodule"
                                       title="<ww:text name="'viewissue.attachments.sort.direction.desc'"/>">
                                        <ww:text name="'viewissue.attachments.sort.direction.desc'"/>
                                    </a>
                                </li>
                            </ul>
                            <ww:if test="/zipSupport == true || /attachable == true">
                                <ul class="aui-list-section aui-last">
                                    <ww:if test="/zipSupport == true">
                                        <li class="aui-list-item">
                                            <a id="aszip" href="<ww:property value="@contextPath"/>/secure/attachmentzip/<ww:property value="/id"/>.zip"
                                               class="aui-list-item-link" title="<ww:text name="'common.concepts.attachments.as.a.zip'"/>">
                                                <ww:text name="'common.concepts.attachments.as.a.zip.short'"/>
                                            </a>
                                        </li>
                                    </ww:if>
                                    <ww:if test="/attachable == true">
                                        <li class="aui-list-item">
                                            <a id="manage-attachment-link" href="<ww:url page="/secure/ManageAttachments.jspa"><ww:param name="'id'" value="../long('id')" /></ww:url>"
                                               class="aui-list-item-link" title="<ww:text name="'manageattachments.tooltip'"/>">
                                                <ww:text name="'manageattachments.title'"/>
                                            </a>
                                        </li>
                                    </ww:if>
                                </ul>
                            </ww:if>
                        </div>
                    </div>
                </li>
            </ul>
            <h3 class="toggle-title"><ww:text name="'common.concepts.attachments.files'"/></h3>
        </div>
        <div class="mod-content">
        <ww:if test="/fileAttachments != null && /fileAttachments/asList()/size > 0">
            <ol id="file_attachments" class="item-attachments">
            <ww:iterator value="/fileAttachments/asList()" status="'attachmentstatus'">
                <%--markup re-use magic--%>
                <ww:declare id="attachmentinfo" value=".">
                    <dt class="attachment-title">
                        <a href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />"
                           title="<ww:if test="/fileAttachments/latestVersion(.) == true"><ww:text name="'common.words.latest'"/> </ww:if><ww:property value="/outlookDate/formatDMYHMS(created)" /> - <ww:if test="@userUtils/existsUser(author) == true"><ww:property value="@userUtils/user(author)/fullName" /></ww:if><ww:else><ww:property value="author"/></ww:else>"><ww:property value="filename"/>
                        </a>
                    </dt>
                    <dd class="attachment-date">
                        <ww:property value="/outlookDate/formatDMYHMS(created)"/></dd>
                    <dd class="attachment-size">
                        <ww:property value="@sizeFormatter/format(filesize)"/></dd>
                    <dd class="attachment-author">
                        <ww:if test="@userUtils/existsUser(author) == true"><ww:property value="@userUtils/user(author)/fullName"/></ww:if><ww:else><ww:property value="author"/></ww:else>
                    </dd>
                </ww:declare>

                <li class="attachment-content <ww:if test="/fileAttachments/latestVersion(.) == false">earlier-version</ww:if>">
                    <ww:if test="/shouldExpandAsZip(.) == true">
                        <div class="twixi-block collapsed">
                            <div class="twixi-wrap verbose">
                                <a href="#" class="twixi"><span class="icon twixi-opened"><span>$i18n.getText("admin.common.words.hide")</span></span></a>
                                <div class="attachment-thumb">
                                    <a href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />">
                                        <ww:fragment template="attachment-icon.jsp">
                                            <ww:param name="'filename'" value="filename"/>
                                            <ww:param name="'mimetype'" value="mimetype"/>
                                        </ww:fragment>
                                    </a>
                                </div>
                                <dl>
                                    <ww:write value="@attachmentinfo" escape="false"/>
                                    <dd class="zip-contents">
                                        <ww:property value="/zipEntries(.)">
                                        <ol>
                                            <ww:iterator value="./list">
                                                <ww:if test="./directory == false">
                                                    <li>
                                                        <div class="attachment-thumb">
                                                            <a href="<ww:property value="@contextPath"/>/secure/attachmentzip/unzip/<ww:property value="/id"/>/<ww:property value="../../id"/><ww:property value="urlEncoded('[')"/><ww:property value="./entryIndex"/><ww:property value="urlEncoded(']')"/>/<ww:property value="./name"/>">
                                                                <ww:fragment template="attachment-icon.jsp">
                                                                    <ww:param name="'filename'" value="name"/>                                                                    
                                                                </ww:fragment>
                                                            </a>
                                                        </div>
                                                        <a href="<ww:property value="@contextPath"/>/secure/attachmentzip/unzip/<ww:property value="/id"/>/<ww:property value="../../id"/><ww:property value="urlEncoded('[')"/><ww:property value="./entryIndex"/><ww:property value="urlEncoded(']')"/>/<ww:property value="./name"/>" title="<ww:property value="./name"/>">
                                                            <ww:property value="./abbreviatedName"/>
                                                        </a>
                                                        <span class="attachment-size">
                                                            <ww:property value="@sizeFormatter/format(./size)"/>
                                                        </span>
                                                    </li>
                                                </ww:if>
                                            </ww:iterator>
                                        </ol>
                                        <span class="zip-contents-trailer">
                                            <ww:if test="./moreAvailable == true">
                                                <ww:text name="'viewissue.attachments.zip.more'">
                                                    <ww:param name="'value0'" value="/maximumNumberOfZipEntriesToShow"/>
                                                    <ww:param name="'value1'" value="./totalNumberOfEntriesAvailable"/>
                                                </ww:text>
                                            </ww:if>
                                            <a href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />"><ww:text name="'viewissue.attachments.zip.download.as.zip'"/></a>
                                        </span>                
                                        </ww:property>
                                    </dd>
                                </dl>
                            </div>
                            <div class="twixi-wrap concise">
                                <a href="#" class="twixi"><span class="icon twixi-closed"><span>$i18n.getText("admin.common.words.show")</span></span></a>

                                <div class="attachment-thumb">
                                    <a href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />">
                                        <ww:fragment template="attachment-icon.jsp">
                                            <ww:param name="'filename'" value="filename"/>
                                            <ww:param name="'mimetype'" value="mimetype"/>
                                        </ww:fragment>
                                    </a>
                                </div>
                                <dl>
                                    <ww:write value="@attachmentinfo" escape="false"/>
                                </dl>
                            </div>
                        </div>

                    </ww:if>
                    <ww:else>
                        <div class="attachment-thumb">
                            <a href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />">
                                <ww:fragment template="attachment-icon.jsp">
                                    <ww:param name="'filename'" value="filename"/>
                                    <ww:param name="'mimetype'" value="mimetype"/>
                                </ww:fragment>
                            </a>
                        </div>
                        <dl>
                            <ww:write value="@attachmentinfo" escape="false"/>
                        </dl>
                    </ww:else>
                </li>
            </ww:iterator>
            </ol>
    <%--  Provide link:
    - if there are some attachments to look at   OR
    - if you have the ability to add attachments --%>
        </ww:if>

        <ww:if test="/thumbnails != null && /thumbnails/size > 0 && /toolkitAvailable() == true">
            <ol id="attachment_thumbnails" class="item-attachments<ww:if test="/fileAttachments != null && /fileAttachments/asList()/size > 0"> section</ww:if>">
            <ww:iterator value="/thumbnails" status="'thumbnailstatus'">
                <li class="attachment-content">
                    <ww:property value="attachmentManager/attachment(./attachmentId)" >
                        <ww:if test="/imageAttachments/latestVersion(.) == false">
                            <div class="attachment-thumb">
                                <a class="gallery" rel="gallery" href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="attachmentId" />/<ww:property value="urlEncoded(filename)" />" title="<ww:property value="filename" /> - <ww:property value="/outlookDate/formatDMYHMS(created)" /> - <ww:if test="@userUtils/existsUser(author) == true"><ww:property value="@userUtils/user(author)/fullName" /></ww:if><ww:else><ww:property value="author"/></ww:else>">
                                    <ww:if test="../filename == null">
                                        <img src="<ww:property value="@contextPath"/>/images/broken_thumbnail.png" width="<ww:property value="../width"/>" height="<ww:property value="../height" />" alt="" />                                        
                                    </ww:if>
                                    <ww:else>
                                        <img src="<ww:property value="@contextPath"/>/secure/thumbnail/<ww:property value="attachmentId" />/<ww:property value="urlEncoded(../filename)" />" width="<ww:property value="../width"/>" height="<ww:property value="../height" />" alt="" />
                                    </ww:else>
                                </a>
                            </div>
                            <dl class="earlier-version">
                                <dt><span class="blender"></span><a class="attachment-title" href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />" title="<ww:property value="filename" /> - <ww:property value="/outlookDate/formatDMYHMS(created)" /> - <ww:if test="@userUtils/existsUser(author) == true"><ww:property value="@userUtils/user(author)/fullName" /></ww:if><ww:else><ww:property value="author"/></ww:else>"><ww:property value="filename" /></a></dt>
                                <dd class="attachment-size"><ww:property value="@sizeFormatter/format(filesize)"/></dd>
                                <dd class="attachment-date"><ww:property value="/outlookDate/formatDMYHMS(created)"/></dd>
                            </dl>
                        </ww:if>
                        <ww:else>
                            <div class="attachment-thumb">
                                <a class="gallery" rel="gallery" href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="attachmentId" />/<ww:property value="urlEncoded(filename)" />" title="<ww:property value="filename" /> - <ww:text name="'common.words.latest'"/> <ww:property value="/outlookDate/formatDMYHMS(./created)" /> - <ww:if test="@userUtils/existsUser(author) == true"><ww:property value="@userUtils/user(author)/fullName" /></ww:if><ww:else><ww:property value="author"/></ww:else>">
                                    <ww:if test="../filename == null">
                                        <img src="<ww:property value="@contextPath"/>/images/broken_thumbnail.png" width="<ww:property value="../width"/>" height="<ww:property value="../height" />" alt="" />
                                    </ww:if>
                                    <ww:else>
                                        <img src="<ww:property value="@contextPath"/>/secure/thumbnail/<ww:property value="attachmentId" />/<ww:property value="urlEncoded(../filename)" />" width="<ww:property value="../width"/>" height="<ww:property value="../height" />" alt="" />
                                    </ww:else>
                                </a>
                            </div>
                            <dl>
                                <dt><span class="blender"></span><a class="attachment-title" href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />" title="<ww:property value="filename" /> - <ww:text name="'common.words.latest'"/> <ww:property value="/outlookDate/formatDMYHMS(./created)" /> - <ww:if test="@userUtils/existsUser(author) == true"><ww:property value="@userUtils/user(author)/fullName" /></ww:if><ww:else><ww:property value="author"/></ww:else>"><ww:property value="filename" /></a></dt>
                                <dd class="attachment-size"><ww:property value="@sizeFormatter/format(filesize)"/></dd>
                                <dd class="attachment-date"><ww:property value="/outlookDate/formatDMYHMS(created)"/></dd>
                            </dl>
                        </ww:else>
                    </ww:property>
                </li>
            </ww:iterator>
            </ol>
        </ww:if>
        </div>
    </div>

    <ww:if test="/imageAttachments != null && /imageAttachments/asList()/size > 0 && /toolkitAvailable() == false">
    <div class="module toggle-wrap">
		<div class="mod-header">
			<h3 class="toggle-title"><ww:text name="'common.concepts.attachments.images'"/></h3>
		</div>
		<div class="mod-content">
            <ww:iterator value="/imageAttachments/asList()" status="'attachmentstatus'">
                <ww:property value="@attachmentstatus/count" />
                <a href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />">
                    <ww:fragment template="attachment-icon.jsp">
                        <ww:param name="'filename'" value="filename"/>
                        <ww:param name="'mimetype'" value="mimetype"/>
                    </ww:fragment>
                </a>
                <a href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />">
                    <ww:property value="filename" />
                </a>
                <font size="-2">(<ww:property value="@sizeFormatter/format(filesize)"/>)</font><br/>
            </ww:iterator>
            <br/>
            <font color="#bb0000"><ww:text name="'thumbnails.error.display'"/></font>
		</div>
	</div>
    </ww:if>
</ww:if>
