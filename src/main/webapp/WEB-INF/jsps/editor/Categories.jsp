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
    var msg = {
        addTitle: '<fmt:message key="categories.add.title"/>',
        editTitleTmpl: '<fmt:message key="categories.renameTitleTmpl"/>',
        confirmDeleteTmpl: '<fmt:message key="categories.deleteCategoryTmpl"/>'
    };
    var actionWeblogId = "<c:out value='${param.weblogId}'/>";
</script>

<div id="template">

<error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

<p class="pagetip">
    <fmt:message key="categories.rootPrompt"/>
</p>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/categories'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

    <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
        <tr>
            <th width="20%"><fmt:message key="generic.category"/></th>
            <th width="20%"><fmt:message key="categories.column.count"/></th>
            <th width="20%"><fmt:message key="categories.column.firstEntry"/></th>
            <th width="20%"><fmt:message key="categories.column.lastEntry"/></th>
            <th width="10%"><fmt:message key="generic.rename"/></th>
            <th width="10%"><fmt:message key="generic.delete"/></th>
        </tr>
      </thead>
      <tbody v-cloak>
          <tr v-for="item in orderedItems">
              <td>{{item.name}}</td>
              <td>{{item.numEntries}}</td>
              <td>{{item.firstEntry}}</td>
              <td>{{item.lastEntry}}</td>
              <td class="buttontd">
                  <button class="btn btn-warning" v-on:click="showEditModal(item)">
                    <fmt:message key="generic.rename" />
                  </button>
              </td>
              <td class="buttontd">
                  <span v-if="items.length > 1">
                      <button class="btn btn-danger" v-on:click="showDeleteModal(item)">
                          <fmt:message key="generic.delete" />
                      </button>
                  </span>
              </td>
          </tr>
      </tbody>
    </table>

    <div class="control clearfix">
        <button type="button" v-on:click="showAddModal()">
          <fmt:message key='categories.addCategory'/>
        </button>
    </div>

<!-- Add/Edit Category modal -->
<div class="modal fade" id="editCategoryModal" tabindex="-1" role="dialog" aria-labelledby="editCategoryModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="editCategoryModalTitle" v-html="editModalTitle"></h5>
      </div>
      <div class="modal-body">
        <span v-if="showUpdateErrorMessage">
            <fmt:message key='categories.error.duplicateName'/><br>
        </span>
        <label for="category-name"><fmt:message key='generic.name'/>:</label>
        <input id="category-name" v-model="itemToEdit.name" maxlength="80" size="40"/>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" v-on:click="inputClear()" data-dismiss="modal">
            <fmt:message key='generic.cancel'/>
        </button>
        <button type="button" class="btn btn-warning" v-bind:disabled="!itemToEdit.name || itemToEdit.name.trim().length == 0" 
              id="saveButton" v-on:click="updateItem()">
            <fmt:message key='generic.save'/>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Delete category modal -->
<div class="modal fade" id="deleteCategoryModal" tabindex="-1" role="dialog" aria-labelledby="deleteCategoryModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteCategoryModalTitle" v-html="deleteModalTitle"></h5>
      </div>
      <div class="modal-body">
        <p>
            <fmt:message key="categories.deleteMoveToWhere"/>
            <select v-model="targetCategoryId" size="1" required>
                <option v-for="item in moveToCategories" v-bind:value="item.id">{{item.name}}</option>
            </select>
        </p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" v-bind:disabled="!targetCategoryId" v-on:click="deleteItem()" id="deleteButton"><fmt:message key='generic.delete'/></button>
      </div>
    </div>
  </div>
</div>

</div>

<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/categories.js'/>"></script>
