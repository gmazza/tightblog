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
                
                <div v-show="recentEntries.PENDING.length > 0">
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

                <div v-show="recentEntries.DRAFT.length > 0">
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

                <div v-show="recentEntries.PUBLISHED.length > 0">
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

                <div v-show="recentEntries.SCHEDULED.length > 0">
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
