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
    var msg = {
        confirmDeleteTmpl: "<fmt:message key='entryEdit.confirmDeleteTmpl'/>",
    };
</script>

<div id="template">

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/entries'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

<p class="pagetip">
    <fmt:message key="entries.tip" />
</p>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarInner">

                <h3><fmt:message key="entries.sidebarTitle" /></h3>
                <hr size="1" noshade="noshade" />

                <p><fmt:message key="entries.sidebarDescription" /></p>

                <div>
                    <div class="sideformrow">
                        <label for="categoryId" class="sideformrow">
                        <fmt:message key="generic.category" /></label>
                        <select id="categoryId" v-model="searchParams.categoryName" size="1" required>
                           <option v-for="(value, key) in lookupFields.categories" v-bind:value="key">{{value}}</option>
                        </select>
                    </div>
                    <br /><br />

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
                            <fmt:message key="entries.label.status" />:
                        </label>
                        <div>
                            <select id="status" v-model="searchParams.status" size="1" required>
                                <option v-for="(value, key) in lookupFields.statusOptions" v-bind:value="key">{{value}}</option>
                            </select>
                        </div>
                    </div>

                    <div class="sideformrow">
                        <label for="status" class="sideformrow">
                            <fmt:message key="entries.label.sortby" />:
                            <br /><br />
                        </label>
                        <div>
                            <div v-for="(value, key) in lookupFields.sortByOptions">
                                <input type="radio" name="sortBy" v-model="searchParams.sortBy" v-bind:value="key">{{value}}<br>
                            </div>
                        </div>
                    </div>
                    <br />
                    <button type="button" v-on:click="loadEntries()"><fmt:message key='entries.button.query'/></button>
                </div>
            </div> <!-- sidebarInner -->
        </div>
    </div>
</div>


<%-- ============================================================= --%>
<%-- Number of entries and date message --%>
<%-- ============================================================= --%>

<div class="tablenav" v-cloak>

    <div style="float:left;">
        {{entriesData.entries.length}} <fmt:message key="entries.nowShowing"/>
    </div>
    <span v-if="entriesData.entries.length > 0">
        <div style="float:right;">
            <span v-if="entriesData.entries[0].pubTime != null">
                {{ entriesData.entries[0].pubTime | standard_datetime }}
            </span>
            ---
            <span v-if="entriesData.entries[entriesData.entries.length - 1].pubTime != null">
                {{ entriesData.entries[entriesData.entries.length - 1].pubTime | standard_datetime }}
           </span>
        </div>
    </span>
    <br><br>

    <%-- ============================================================= --%>
    <%-- Next / previous links --%>
    <%-- ============================================================= --%>

    <span v-if="pageNum > 0 || entriesData.hasMore">
        <center>
            &laquo;
            <button type="button" v-bind:disabled="pageNum <= 0" v-on:click="previousPage()">
                <fmt:message key='entries.prev'/>
            </button>    
            |
            <button type="button" v-bind:disabled="!entriesData.hasMore" v-on:click="nextPage()">
                <fmt:message key='entries.next'/>
            </button>
            &raquo;
        </center>
    </span>

    <br>
</div>


<%-- ============================================================= --%>
<%-- Entry table--%>
<%-- ============================================================= --%>

<p>
    <span class="draftEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <fmt:message key="entries.draft" />&nbsp;&nbsp;
    <span class="pendingEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <fmt:message key="entries.pending" />&nbsp;&nbsp;
    <span class="scheduledEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <fmt:message key="entries.scheduled" />&nbsp;&nbsp;
</p>

<table class="table table-sm table-bordered table-hover" width="100%">

    <thead class="thead-light">
        <tr>
            <th width="15%"><fmt:message key="entries.pubTime" /></th>
            <th width="15%"><fmt:message key="entries.updateTime" /></th>
            <th width="8%"><fmt:message key="generic.category" /></th>
            <th><fmt:message key="entries.entryTitle" /></th>
            <th width="16%"><fmt:message key="generic.tags" /></th>
            <th width="5%"></th>
            <th width="5%"></th>
            <th width="5%"></th>
        </tr>
    </thead>

    <tbody>
        <tr v-for="entry in entriesData.entries"
            v-bind:class="entryStatusClass(entry.status)" v-cloak>

            <td>
                <span v-if="entry.pubTime != null">
                    {{ entry.pubTime | standard_datetime }}
                </span>
            </td>

            <td>
                <span v-if="entry.updateTime != null">
                    {{ entry.updateTime | standard_datetime }}
                </span>
            </td>

            <td>
                {{entry.category.name}}
            </td>

            <td>
                {{entry.title.substr(0, 80)}}
            </td>

            <td>
                {{entry.tagsAsString}}
            </td>

            <td>
                <span v-if="entry.status == 'PUBLISHED'">
                    <a v-bind:href='entry.permalink' target="_blank"><fmt:message key="entries.view" /></a>
                </span>
            </td>

            <td>
                <a target="_blank" v-bind:href="'<c:url value='/tb-ui/app/authoring/entryEdit'/>?weblogId=<c:out value='${param.weblogId}'/>&entryId=' + entry.id">
                    <fmt:message key="generic.edit" />
                </a>
            </td>

            <td class="buttontd">
                <button class="btn btn-danger" v-on:click="showDeleteModal(entry)"><fmt:message key="generic.delete" /></button>
            </td>

        </tr>
    </tbody>
</table>

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
        <button type="button" class="btn btn-danger" id="deleteButton" v-on:click="deleteWeblogEntry()"><fmt:message key='generic.delete'/></button>
      </div>
    </div>
  </div>
</div>

<span v-if="entriesData.entries.length == 0">
    <fmt:message key="entries.noneFound" />
</span>

</div>

<script src="<c:url value='/tb-ui/scripts/components/dayjsfilters.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/components/datepicker.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/entries.js'/>"></script>
