<!--
    Copyright 2017 the original author or authors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var msg = {
        replaceTagTitleTmpl: "<fmt:message key='tags.replace.title'/>",
        addTagTitleTmpl: "<fmt:message key='tags.add.title'/>"
    };
</script>

<div id="template">

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/tags'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<success-message-box v-bind:message="successMessage" @close-box="successMessage = null"></success-message-box>
<error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

<p class="pagetip">
    <fmt:message key="tags.prompt"/>
</p>

<div class="tablenav">

    <span style="text-align:center;" v-if="pageNum > 0 || tagData.hasMore" v-cloak>
        &laquo;
        <button type="button" v-bind:disabled="pageNum <= 0" v-on:click="previousPage()">
            <fmt:message key='weblogEntryQuery.prev'/>
        </button>             |
        <button type="button" v-bind:disabled="!tagData.hasMore" v-on:click="nextPage()">
            <fmt:message key='weblogEntryQuery.next'/>
        </button>
        &raquo;
    </span>

    <br>
</div>

<table class="table table-sm  table-bordered table-striped">
    <thead class="thead-light">
        <tr>
            <th width="4%"><input type="checkbox"
                v-bind:disabled="tagData.tags.length == 0" 
                v-on:input="toggleCheckboxes($event.target.checked)"
                title="<fmt:message key='generic.selectAll'/>"/></th>
            <th width="20%"><fmt:message key="tags.column.tag" /></th>
            <th width="10%"><fmt:message key="categories.column.count" /></th>
            <th width="10%"><fmt:message key="categories.column.firstEntry" /></th>
            <th width="10%"><fmt:message key="categories.column.lastEntry" /></th>
            <th width="15%"><fmt:message key="tags.view.published" /></th>
            <th width="13%"></th>
            <th width="13%"></th>
        </tr>
    </thead>
    <tbody>
        <tr v-for="tag in tagData.tags" v-cloak>
            <td class="center" style="vertical-align:middle">
                  <input type="checkbox" v-bind:title="'checkbox for ' + tag.name"
                    v-model="tag.selected" v-bind:value="tag.name" />
            </td>
            <td>{{tag.name}}</td>
            <td>{{tag.total}}</td>
            <td>{{tag.firstEntry}}</td>
            <td>{{tag.lastEntry}}</td>

            <td>
                <a v-bind:href='tag.viewUrl' target="_blank"><fmt:message key="tags.column.view" /></a>
            </td>

            <td class="buttontd">
              <button class="btn btn-warning" v-on:click="showReplaceModal(tag)">
                <fmt:message key="tags.replace" />
              </button>
          </td>

          <td class="buttontd">
              <button class="btn btn-warning" v-on:click="showAddModal(tag)">
                <fmt:message key="generic.add" />
              </button>
          </td>
      </tr>
    </tbody>
</table>

<div class="control" v-if="tagData.tags.length > 0">
  <span style="padding-left:7px">
    <button v-bind:disabled="!tagsSelected" data-toggle="modal" data-target="#deleteTagsModal">
        <fmt:message key='generic.deleteSelected'/>
    </button>
  </span>
</div>

<!-- Delete tag modal -->
<div class="modal fade" id="deleteTagsModal" tabindex="-1" role="dialog" aria-labelledby="deleteTagsModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteTagsModalTitle"><fmt:message key='tags.confirmDelete'/></h5>
      </div>
      <div class="modal-body">
        <span id="confirmDeleteMsg"><fmt:message key='tags.deleteWarning'/></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" id="deleteButton" v-on:click="deleteTags()">
            <fmt:message key='generic.delete'/>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Replace/Add tag modal -->
<div class="modal fade" id="changeTagModal" tabindex="-1" role="dialog" aria-labelledby="changeTagModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="changeTagModalTitle" v-html="editModalTitle"></h5>
      </div>
      <div class="modal-body">
          <label for="newTag"><fmt:message key='generic.name'/>:</label>
          <input id="newTag" v-model="newTagName" type="text">
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" v-on:click="inputClear()" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" v-bind:disabled="!newTagName" class="btn btn-warning" id="changeButton" v-on:click="tagUpdate()"
                action="populatedByJS" data-currentTag="populatedByJS" >
            <fmt:message key='generic.save'/>
        </button>
      </div>
    </div>
  </div>
</div>

<span v-if="tagData.tags.length == 0">
    <fmt:message key="tags.noneFound" />
</span>

</div>

<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/tags.js'/>"></script>
