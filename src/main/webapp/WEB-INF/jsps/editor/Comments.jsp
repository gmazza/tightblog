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
    var entryId = "<c:out value='${param.entryId}'/>";
    var nowShowingTmpl = "<fmt:message key='comments.nowShowing'/>";
    var commentHeaderTmpl = "<fmt:message key='comments.commentHeader'/>";
    var entryTitleTmpl = "<fmt:message key='comments.entry.subtitle'/>";
</script>

<div id="template">

<c:choose>
    <c:when test="${param.entryId == null}">
        <input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/authoring/comments'/>?weblogId=<c:out value='${param.weblogId}'/>"/>
    </c:when>
    <c:otherwise>
        <input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/authoring/comments'/>?weblogId=<c:out value='${param.weblogId}'/>&entryId=<c:out value='${param.entryId}'/>"/>
    </c:otherwise>
</c:choose>

<error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

<p class="subtitle" v-show="entryTitleMsg != ''">
    <span>
        <span v-html="entryTitleMsg"></span>
    </span>
</p>

    <p class="pagetip">
        <fmt:message key="comments.tip" />
    </p>
    <div class="sidebarFade">
        <div class="menu-tr">
            <div class="menu-tl">
                <div class="sidebarInner">

                    <h3><fmt:message key="comments.sidebarTitle" /></h3>
                    <hr size="1" noshade="noshade" />

                    <p><fmt:message key="comments.sidebarDescription" /></p>

                    <div class="sideformrow">
                        <label for="searchText" class="sideformrow"><fmt:message key="comments.searchString" />:</label>
                        <input id="searchText" type="text" v-model="searchParams.searchText" size="30"/>
                    </div>
                    <br />
                    <br />

                    <div class="sideformrow">
                        <label for="startDateString" class="sideformrow"><fmt:message key="entries.label.startDate" />:</label>
                        <date-picker @update-date="updateStartDate" v-once></date-picker>
                    </div>

                    <div class="sideformrow">
                        <label for="endDateString" class="sideformrow"><fmt:message key="entries.label.endDate" />:</label>
                        <date-picker @update-date="updateEndDate" v-once></date-picker>
                    </div>
                    <br /><br />

                    <div class="sideformrow">
                        <label for="status" class="sideformrow">
                            <fmt:message key="comments.pendingStatus" />:
                        </label>
                        <div>
                            <select id="status" v-model="searchParams.status" size="1" required>
                                <option v-for="(value, key) in lookupFields" v-bind:value="key">{{value}}</option>
                            </select>
                        </div>
                    </div>
                    <br><br>
                    <button type="button" v-on:click="loadComments()"><fmt:message key='entries.button.query'/></button>
                    <br>
                </div>
            </div>
        </div>
    </div>

<div v-if="commentData.comments.length == 0">
    <fmt:message key="comments.noCommentsFound" />
</div>

<div v-if="commentData.comments.length > 0">

    <%-- ============================================================= --%>
    <%-- Number of comments and date message --%>
    <%-- ============================================================= --%>

        <div class="tablenav">

        <div style="float:left" v-html="nowShowingMsg"></div>

        <span v-if="commentData.comments.length > 0">
            <div style="float:right;">
                {{ commentData.comments[0].postTime | standard_datetime }}
                ---
                {{ commentData.comments[commentData.comments.length - 1].postTime | standard_datetime }}
            </div>
        </span>
        <br><br>


        <%-- ============================================================= --%>
        <%-- Next / previous links --%>
        <%-- ============================================================= --%>

        <span v-if="pageNum > 0 || commentData.hasMore" v-cloak>
            <center>
                &laquo;
                <button type="button" v-bind:disabled="pageNum <= 0" v-on:click="previousPage()">
                    <fmt:message key='weblogEntryQuery.prev'/>
                </button>
                |
                <button type="button" v-bind:disabled="!commentData.hasMore" v-on:click="nextPage()">
                    <fmt:message key='weblogEntryQuery.next'/>
                </button>
                &raquo;
            </center>
        </span>

        </div>


        <table class="table table-sm table-bordered table-hover" width="100%">

        <%-- ======================================================== --%>
        <%-- Comment table header --%>

        <thead class="thead-light">
            <tr>
                <th width="8%"><fmt:message key="comments.showhide" /></th>
                <th width="8%" ><fmt:message key="generic.delete" /></th>
                <th><fmt:message key="comments.columnComment" /> -
                    <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <fmt:message key="comments.pending" />&nbsp;&nbsp;
                    <span class="spamCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <fmt:message key="comments.spam" />&nbsp;&nbsp;
                </th>
            </tr>
        </thead>

        <%-- ========================================================= --%>
        <%-- Loop through comments --%>
        <%-- ========================================================= --%>

        <tbody>
            <tr v-for="comment in commentData.comments">
                <td>
                    <button type="button" v-if="comment.status == 'SPAM' || comment.status == 'DISAPPROVED' || comment.status == 'PENDING'"
                        v-on:click="approveComment(comment)">
                        <fmt:message key='comments.approve'/>
                    </button>
                    <button type="button" v-if="comment.status == 'APPROVED'" v-on:click="hideComment(comment)">
                        <fmt:message key='comments.hide'/>
                    </button>
                </td>
                <td>
                    <button type="button" v-on:click="deleteComment(comment)"><fmt:message key='generic.delete'/></button>
                </td>

                <td v-bind:class="commentStatusClass(comment.status)">

                    <%-- comment details table in table --%>
                    <table class="innertable" >
                        <tr>
                            <td class="viewbody">
                            <div class="viewdetails bot">
                                <div class="details">
                                    <fmt:message key="comments.entryTitled" />:&nbsp;
                                    <a v-bind:href='comment.weblogEntry.permalink' target="_blank">{{comment.weblogEntry.title}}</a>
                                </div>
                                <div class="details">
                                    <fmt:message key="comments.commentBy" />:&nbsp;
                                    <span v-html="getCommentHeader(comment)"></span>
                                </div>
                                <span v-if="comment.url">
                                    <div class="details">
                                        <fmt:message key="comments.commentByURL" />:&nbsp;
                                        <a v-href='comment.url'>
                                        {{comment.url}}
                                        </a>
                                    </div>
                                </span>
                                <div class="details">
                                    <fmt:message key="comments.postTime" />: {{ comment.postTime | standard_datetime }}
                                </div>
                            </div>
                            <div class="viewdetails bot">
                                 <div class="details bot">
                                      <textarea style='width:100%' rows='10' v-model="comment.content" v-bind:readonly="!comment.editable"></textarea>
                                 </div>
                                 <div class="details" v-show="!comment.editable">
                                      <a v-on:click="editComment(comment)"><fmt:message key="generic.edit"/></a>
                                 </div>
                                 <div class="details" v-show="comment.editable">
                                      <a v-on:click="saveComment(comment)"><fmt:message key="generic.save"/></a> &nbsp;|&nbsp;
                                      <a v-on:click="editCommentCancel(comment)"><fmt:message key="generic.cancel"/></a>
                                 </div>
                            </div>
                        </tr>
                    </table>
                </td>
            </tr>
        </tbody>
    </table>
    <br>
</div>

</div>

<script src="<c:url value='/tb-ui/scripts/components/dayjsfilters.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/components/datepicker.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/comments.js'/>"></script>
