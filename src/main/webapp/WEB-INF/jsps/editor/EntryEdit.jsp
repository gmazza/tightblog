<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<link rel="stylesheet" media="all" href='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />

<script src="<c:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.8.36/dayjs.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var entryIdParam = "<c:out value='${param.entryId}'/>";
    var newEntryUrl = "<c:url value='/tb-ui/app/authoring/entryAdd'/>?weblogId=" + weblogId;
    var loginUrl = "<c:url value='/tb-ui/app/login-redirect'/>";
    var msg = {
        confirmDeleteTmpl: "<fmt:message key='entryEdit.confirmDeleteTmpl'/>",
        commentCountTmpl: "<fmt:message key='entryEdit.hasComments'/>",
        sessionTimeoutTmpl: "<fmt:message key='entryEdit.sessionTimedOut'/>"
    };
</script>

<div id="template">

 <div id="content">
    <div id="sidebar_maincontent_wrap">
        <div id="maincontent">


<success-message-box v-bind:message="successMessage" @close-box="successMessage = null"></success-message-box>
<error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

<div>
    <table class="entryEditTable" cellpadding="0" cellspacing="0" style="width:100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="title"><fmt:message key="entryEdit.entryTitle" /></label>
            </td>
            <td>
                <input id="title" type="text" v-model="entry.title" maxlength="255" style="width:60%" autocomplete="off">
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <fmt:message key="entryEdit.status" />
            </td>
            <td v-cloak>
                <span v-show="entry.status == 'PUBLISHED'" style="color:green; font-weight:bold">
                    <fmt:message key="entryEdit.published" />
                    (<fmt:message key="entryEdit.updateTime" /> {{ entry.updateTime | standard_datetime }})
                </span>
                <span v-show="entry.status == 'DRAFT'" style="color:orange; font-weight:bold">
                    <fmt:message key="entryEdit.draft" />
                    (<fmt:message key="entryEdit.updateTime" /> {{ entry.updateTime | standard_datetime }})
                </span>
                <span v-show="entry.status == 'PENDING'" style="color:orange; font-weight:bold">
                    <fmt:message key="entryEdit.pending" />
                    (<fmt:message key="entryEdit.updateTime" /> {{ entry.updateTime | standard_datetime }})
                </span>
                <span v-show="entry.status == 'SCHEDULED'" style="color:orange; font-weight:bold">
                    <fmt:message key="entryEdit.scheduled" />
                    (<fmt:message key="entryEdit.updateTime"/> {{ entry.updateTime | standard_datetime }})
                </span>
                <span v-show="!entry.status" style="color:red; font-weight:bold">
                    <fmt:message key="entryEdit.unsaved" />
                </span>
            </td>
        </tr>

        <tr v-show="entry.id" v-cloak>
            <td class="entryEditFormLabel">
                <label for="permalink"><fmt:message key="entryEdit.permalink" /></label>
            </td>
            <td>
                <span v-show="entry.status == 'PUBLISHED'">
                    <a id="permalink" v-bind:href='entry.permalink' target="_blank">{{entry.permalink}}</a>
                    <img src='<c:url value="/images/launch-link.png"/>' />
                </span>
                <span v-show="entry.status != 'PUBLISHED'">
                    {{entry.permalink}}
                </span>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="categoryId"><fmt:message key="generic.category" /></label>
            </td>
            <td v-cloak>
                <select id="categoryId" v-model="entry.category.id" size="1" required>
                   <option v-for="(value, key) in metadata.categories" v-bind:value="key">{{value}}</option>
                </select>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="tags"><fmt:message key="generic.tags" /></label>
            </td>
            <td>
                <tag-autocomplete @update-tags="updateTags" v-model="entry.tagsAsString"></tag-autocomplete>
            </td>
        </tr>

        <tr v-cloak>
            <td class="entryEditFormLabel">
                <label for="title"><fmt:message key="entryEdit.editFormat" /></label>
            </td>
            <td v-cloak>
                <select v-model="entry.editFormat" size="1" required>
                   <option v-for="(value, key) in metadata.editFormats" v-bind:value="key">{{value}}</option>
                </select>
            </td>
        </tr>

    </table>

    <%-- ================================================================== --%>
    <%-- Weblog editor --%>

    <p class="toplabel">

    <div id="accordion">
        <h3>
            <fmt:message key="entryEdit.content" />
        </h3>
        <div>
            <textarea id="edit_content" cols="75" rows="25" style="width:100%" v-model="entry.text"></textarea>
        </div>
        <h3><fmt:message key="entryEdit.summary"/><tags:help key="entryEdit.summary.tooltip"/></h3>
        <div>
            <textarea id="edit_summary" cols="75" rows="10" style="width:100%" v-model="entry.summary"></textarea>
        </div>
        <h3><fmt:message key="entryEdit.notes"/><tags:help key="entryEdit.notes.tooltip"/></h3>
        <div>
            <textarea id="edit_notes" cols="75" rows="10" style="width:100%" v-model="entry.notes"></textarea>
        </div>
    </div>

    <%-- ================================================================== --%>
    <%-- advanced settings  --%>

    <div class="controlToggle">
        <fmt:message key="entryEdit.miscSettings" />
    </div>

    <label for="link"><fmt:message key="entryEdit.specifyPubTime" />:</label>
    <div>
        <input type="number" min="0" max="23" step="1" v-model="entry.hours"/>
        :
        <input type="number" min="0" max="59" step="1" v-model="entry.minutes"/>
        &nbsp;&nbsp;
        <date-picker @update-date="updatePublishDate" v-model="entry.dateString"></date-picker>
        {{metadata.timezone}}
    </div>
    <br />

    <span v-show="metadata.commentingEnabled">
        <fmt:message key="entryEdit.allowComments" />
        <fmt:message key="entryEdit.commentDays" />
        <select id="commentDaysId" v-model="entry.commentDays" size="1" required>
           <option v-for="(value, key) in metadata.commentDayOptions" v-bind:value="key">{{value}}</option>
        </select>
        <br />
    </span>

    <br />

    <table>
        <tr>
            <td><fmt:message key="entryEdit.searchDescription" />:<tags:help key="entryEdit.searchDescription.tooltip"/></td>
            <td style="width:75%"><input type="text" style="width:100%" maxlength="255" v-model="entry.searchDescription"></td>
        </tr>
        <tr>
            <td><fmt:message key="entryEdit.enclosureURL" />:<tags:help key="entryEdit.enclosureURL.tooltip"/></td>
            <td><input type="text" style="width:100%" maxlength="255" v-model="entry.enclosureUrl"></td>
        </tr>
        <tr v-show="entryId">
            <td></td>
            <td>
                <span v-show="entry.enclosureType">
                    <fmt:message key="entryEdit.enclosureType" />: {{entry.enclosureType}}
                </span>
                <span v-show="entry.enclosureLength">
                    <fmt:message key="entryEdit.enclosureLength" />: {{entry.enclosureLength}}
                </span>
            </td>
        </tr>
    </table>

    <%-- ================================================================== --%>
    <%-- the button box --%>

    <br>
    <div class="control">
        <span style="padding-left:7px">
            <button type="button" v-on:click="saveEntry('DRAFT')"><fmt:message key='entryEdit.save'/></button>
            <span v-show="entry.id">
                <button type="button" v-on:click="previewEntry()"><fmt:message key='entryEdit.fullPreviewMode'/></button>
            </span>
            <span v-show="metadata.author">
                <button type="button" v-on:click="saveEntry('PUBLISHED')"><fmt:message key='entryEdit.post'/></button>
            </span>
            <span v-show="!metadata.author">
                <button type="button" v-on:click="saveEntry('PENDING')"><fmt:message key='entryEdit.submitForReview'/></button>
            </span>
        </span>

        <span style="float:right" v-show="entry.id">
            <button type="button" v-on:click="showDeleteModal(entry)"><fmt:message key='entryEdit.deleteEntry'/></button>
        </span>
    </div>
</div>

<!-- Delete entry modal -->
<div class="modal fade" id="deleteEntryModal" tabindex="-1" role="dialog" aria-labelledby="deleteEntryModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteEntryModalTitle"><fmt:message key="generic.confirm.delete"/></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
          <span id="confirmDeleteMsg" v-html="deleteModalMsg"></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" v-on:click="deleteWeblogEntry()"><fmt:message key='generic.delete'/></button>
      </div>
    </div>
  </div>
</div>

</div></div>

<div id="sidebar_rightcontent_wrap">
    <div id="rightcontent">

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">

            <div class="sidebarInner" v-cloak>

                <h3><fmt:message key="entryEdit.comments" /></h3>

                <div v-show="entry.commentCountIncludingUnapproved > 0">
                    <a v-bind:href="entry.commentsUrl" v-html="commentCountMsg"></a>
                </div>
                <div v-show="entry.commentCountIncludingUnapproved == 0">
                    <fmt:message key="generic.none" />
                </div>

                <div v-show="recentEntries.PENDING && recentEntries.PENDING.length > 0">
                    <hr size="1" noshade="noshade" />
                    <h3><fmt:message key="entryEdit.pendingEntries" /></h3>

                    <span v-for="post in recentEntries.PENDING">
                        <span class="entryEditSidebarLink">
                            <img src='${pageContext.request.contextPath}/images/table_edit.png'
                                 align="absmiddle" border="0" alt="icon" title="Edit" />
                            <a v-bind:href="post.editUrl">{{post.title}}</a>
                        </span>
                        <br>
                    </span>
                </div>

                <div v-show="recentEntries.DRAFT && recentEntries.DRAFT.length > 0">
                    <hr size="1" noshade="noshade" />
                    <h3><fmt:message key="entryEdit.draftEntries" /></h3>

                    <span v-for="post in recentEntries.DRAFT">
                        <span class="entryEditSidebarLink">
                            <img src='${pageContext.request.contextPath}/images/table_edit.png'
                                 align="absmiddle" border="0" alt="icon" title="Edit" />
                            <a v-bind:href="post.editUrl">{{post.title}}</a>
                        </span>
                        <br>
                    </span>
                </div>

                <div v-show="recentEntries.PUBLISHED && recentEntries.PUBLISHED.length > 0">
                    <hr size="1" noshade="noshade" />
                    <h3><fmt:message key="entryEdit.publishedEntries" /></h3>

                    <span v-for="post in recentEntries.PUBLISHED">
                        <span class="entryEditSidebarLink">
                            <img src='${pageContext.request.contextPath}/images/table_edit.png'
                                 align="absmiddle" border="0" alt="icon" title="Edit" />
                            <a v-bind:href="post.editUrl">{{post.title}}</a>
                        </span>
                        <br>
                    </span>
                </div>

                <div v-show="recentEntries.SCHEDULED && recentEntries.SCHEDULED.length > 0">
                    <hr size="1" noshade="noshade" />
                    <h3><fmt:message key="entryEdit.scheduledEntries" /></h3>

                    <span v-for="post in recentEntries.SCHEDULED">
                        <span class="entryEditSidebarLink">
                            <img src='${pageContext.request.contextPath}/images/table_edit.png'
                                 align="absmiddle" border="0" alt="icon" title="Edit" />
                            <a v-bind:href="post.editUrl">{{post.title}}</a>
                        </span>
                        <br>
                    </span>
                </div>

                <br>
                <br>
            </div>
        </div>
    </div>
</div>

</div></div></div>

</div>

<script src="<c:url value='/tb-ui/scripts/components/dayjsfilters.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/components/datepicker.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/entryedit.js'/>"></script>
