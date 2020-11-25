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

<script src="https://cdn.jsdelivr.net/npm/lodash@4.17.15/lodash.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var msg= {
        editTitle: '<fmt:message key="blogroll.editLink"/>',
        addTitle: '<fmt:message key="blogroll.addLink"/>'
    };
    var actionWeblogId = "<c:out value='${param.weblogId}'/>";
</script>

<div id="template">

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/bookmarks'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

<p class="pagetip">
    <fmt:message key="blogroll.rootPrompt" />
</p>

<table class="table table-sm table-bordered table-striped">
    <thead class="thead-light">
      <tr>
          <th width="5%"><input type="checkbox"
              v-bind:disabled="items.length == 0" 
              v-on:input="toggleCheckboxes($event.target.checked)"
              title="<fmt:message key='blogroll.selectAllLabel'/>"/></th>
          <th width="25%"><fmt:message key="blogroll.linkLabel" /></th>
          <th width="25%"><fmt:message key="blogroll.urlHeader" /></th>
          <th width="35%"><fmt:message key="generic.description" /></th>
          <th width="10%"><fmt:message key="generic.edit" /></th>
      </tr>
    </thead>
    <tbody id="tableBody" v-cloak>
      <tr v-for="item in orderedItems">
        <td class="center" style="vertical-align:middle">
            <input type="checkbox" name="idSelections" v-bind:title="'checkbox for ' + item.name"
                v-model="item.selected" v-bind:value="item.id" />
        </td>
        <td>{{item.name}}</td>
        <td><a target="_blank" v-bind:href="item.url">{{item.url}}</a></td>
        <td>{{item.description}}</td>
        <td class="buttontd">
            <button class="btn btn-warning" v-on:click="editItem(item)">
                <fmt:message key="generic.edit" />
            </button>
        </td>
      </tr>
    </tbody>
</table>

<div class="control clearfix">
    <button type="button" v-on:click="addItem()">
      <fmt:message key='blogroll.addLink'/>
    </button> 

    <span v-if="items.length > 0">
        <button v-bind:disabled="!itemsSelected" data-toggle="modal" data-target="#deleteLinksModal">
            <fmt:message key='generic.deleteSelected'/>
        </button>
    </span>
</div>

<!-- Add/Edit Link modal -->
<div class="modal fade" id="editLinkModal" v-show="showEditModal" tabindex="-1" role="dialog" aria-labelledby="editLinkModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="editLinkModalTitle">{{editModalTitle}}</h5>
      </div>
      <div class="modal-body">
        <p class="pagetip">
            <fmt:message key="blogroll.requiredFields">
                <fmt:param><fmt:message key="blogroll.linkLabel"/></fmt:param>
                <fmt:param><fmt:message key="blogroll.url"/></fmt:param>
            </fmt:message>
        </p>
        <form>
            <div class="form-group">
                <label for="name" class="col-form-label"><fmt:message key='blogroll.linkLabel'/></label>
                <input type="text" class="form-control" v-model="itemToEdit.name" maxlength="80"/>
            </div>
            <div class="form-group">
                <label for="url" class="col-form-label"><fmt:message key='blogroll.url'/></label>
                <input type="text" class="form-control" v-model="itemToEdit.url" maxlength="128"/>
            </div>
            <div class="form-group">
                <label for="description" class="col-form-label"><fmt:message key='generic.description'/></label>
                <input type="text" class="form-control" v-model="itemToEdit.description" maxlength="128"/>
            </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" v-on:click="inputClear()" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-warning" v-bind:disabled="!itemToEdit.name || !itemToEdit.url" id="saveButton" v-on:click="updateItem()">
            <fmt:message key='generic.save'/>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Delete selected links modal -->
<div class="modal fade" id="deleteLinksModal" tabindex="-1" role="dialog" aria-labelledby="deleteLinksModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteLinksModalTitle"><fmt:message key='generic.confirm.delete'/></h5>
      </div>
      <div class="modal-body">
        <span id="confirmDeleteMsg"><fmt:message key='blogroll.deleteWarning'/></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" id="deleteButton" v-on:click="deleteLinks()">
            <fmt:message key='generic.delete'/>
        </button>
      </div>
    </div>
  </div>
</div>

</div>

<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/blogroll.js'/>"></script>
