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
--%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>

<script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.8.36/dayjs.min.js"></script>

<script>
  var contextPath = "${pageContext.request.contextPath}";
  var actionWeblogId = "<c:out value='${param.weblogId}'/>";
  var currentTheme = "<c:out value='${actionWeblog.theme}'/>";
  var templatePageUrl = "<c:url value='/tb-ui/app/authoring/templates'/>?weblogId=" + actionWeblogId;
  var msg = {
    success: '<fmt:message key="generic.changes.saved"/>',
    switchThemeTitleTmpl: '<fmt:message key="templates.confirmSwitch"/>',
    confirmDeleteTemplate: '<fmt:message key="templates.confirmDelete"/>'
  };
</script>

<div id="template">

  <success-message-box v-bind:message="successMessage" @close-box="successMessage=null"></success-message-box>
  <error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

  <p class="pagetip">
    <fmt:message key="templates.tip" />
  </p>

  <input id="refreshURL" type="hidden"
  value="<c:url value='/tb-ui/app/authoring/templates'/>?weblogId=<c:out value='${param.weblogId}'/>" />
  
  <div class="control clearfix">
    <span style="padding-left:7px">
      <fmt:message key="templates.currentTheme" />: <b><c:out value="${actionWeblog.theme}" /></b>
    </span>

    <c:url var="templateEditUrl" value="/tb-ui/app/authoring/themeEdit">
      <c:param name="weblogId" value="${weblogId}" />
    </c:url>

    <span style="float:right" v-show="switchToThemes.length > 0">
      <button type="button" v-on:click="showSwitchThemeModal()">
        <fmt:message key="templates.switchTheme" />
      </button>

      <select id="switchThemeMenu" size="1" required v-model="targetThemeId">
        <option v-for="theme in switchToThemes" v-bind:value="theme.id">{{theme.name}}</option>
      </select>
    </span>    
  </div>

  <table class="table table-sm table-bordered table-striped">
    <thead class="thead-light">
      <tr>
        <th width="4%"><input type="checkbox" v-model="allFilesSelected"
            v-on:input="toggleCheckboxes($event.target.checked)"
            title="<fmt:message key='templates.selectAllLabel'/>" /></th>
        <th width="17%">
          <fmt:message key="generic.name" />
        </th>
        <th width="16%">
          <fmt:message key="templates.role" />
        </th>
        <th width="38%">
          <fmt:message key="templates.description" />
        </th>
        <th width="8%">
          <fmt:message key="templates.source" /> <tags:help key="templates.source.tooltip"/>
        </th>
        <th width="13%">
          <fmt:message key="generic.lastModified" />
        </th>
        <th width="4%">
          <fmt:message key="generic.view" />
        </th>
      </tr>
    </thead>
    <tbody v-cloak>
      <tr v-for="tpl in weblogTemplateData.templates">
        <td class="center" style="vertical-align:middle">
          <span v-if="!isBuiltIn(tpl)">
            <input type="checkbox" name="idSelections" v-model="tpl.selected" v-bind:value="tpl.id" />
          </span>
        </td>

        <td style="vertical-align:middle">
          <c:url var="edit" value="/tb-ui/app/authoring/templateEdit">
            <c:param name="weblogId" value="${actionWeblog.id}" />
          </c:url>
          <span>
            <a v-if="isBuiltIn(tpl)" v-bind:href="'${edit}&templateName=' + tpl.name">{{tpl.name}}</a>
            <a v-else v-bind:href="'${edit}&templateId=' + tpl.id">{{tpl.name}}</a>
          </span>
        </td>

        <td style="vertical-align:middle">
          {{tpl.role.readableName}}
        </td>

        <td style="vertical-align:middle">
          <span v-if="tpl.role.singleton != true && tpl.description != null && tpl.description != ''">
            {{tpl.description}}
          </span>
        </td>

        <td style="vertical-align:middle">
          {{tpl.derivation}}
        </td>

        <td>
          <span v-if="tpl.lastModified != null">
            {{tpl.lastModified | standard_datetime }}
          </span>
        </td>

        <td class="buttontd">
          <span v-if="tpl.role.accessibleViaUrl">
            <a target="_blank" v-bind:href="'<c:out value='${actionWeblogURL}'/>page/' + tpl.name">
              <img src='<c:url value="/images/world_go.png"/>' border="0" alt="icon" />
            </a>
          </span>
        </td>
      </tr>
    </tbody>
  </table>

  <div class="control">
    <span style="padding-left:7px">
      <button type="button" v-bind:disabled="templatesSelectedCount == 0" v-on:click="showDeleteTemplatesModal()">
        <fmt:message key='generic.deleteSelected' />
      </button>
    </span>
  </div>

  <div class="menu-tr sidebarFade">
    <div class="sidebarInner">
      <form name="myform">
        <div>
          <fmt:message key="templates.addNewPage" />
        </div>
        <table cellpadding="0" cellspacing="6">
          <tr>
            <td>
              <fmt:message key="generic.name" />
            </td>
            <td><input type="text" v-model="newTemplateName" maxlength="40" required /></td>
          </tr>
          <tr>
            <td>
              <fmt:message key="templates.role" />
            </td>
            <td>
              <select v-model="newTemplateRole" size="1" required>
                <option v-for="(value, key) in weblogTemplateData.availableTemplateRoles" v-bind:value="key">{{value}}
                </option>
              </select>
            </td>
          </tr>
          <tr>
            <td colspan="2" class="field">
              <p>{{weblogTemplateData.templateRoleDescriptions[newTemplateRole]}}</p>
            </td>
          </tr>
          <tr>
            <td>
              <button type="button" v-on:click="addTemplate()">
                <fmt:message key='templates.add' />
              </button>
            </td>
          </tr>
        </table>
      </form>
    </div>
  </div>

  <!-- Delete templates modal -->
  <div class="modal fade" id="deleteTemplatesModal" tabindex="-1" role="dialog" aria-labelledby="deleteTemplatesTitle"
    aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="deleteTemplatesTitle" v-html="deleteDialogTitle"></h5>
        </div>
        <div class="modal-body">
          <span id="deleteTemplatesMsg" class="text-danger">
            <fmt:message key="templates.deleteWarning" /></span>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">
            <fmt:message key='generic.cancel' /></button>
          <button type="button" class="btn btn-danger" v-on:click="deleteTemplates()">
            <fmt:message key='generic.delete' /></button>
        </div>
      </div>
    </div>
  </div>

  <!-- Switch theme modal -->
  <div class="modal fade" id="switchThemeModal" tabindex="-1" role="dialog" aria-labelledby="switchThemeTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="switchThemeTitle" v-html="switchThemeTitle"></h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div id="confirmSwitchMsg" class="text-danger"><fmt:message key="templates.switchWarning" /></div>
          <div id="confirmSwitchMsg"><fmt:message key="templates.tryTestBlog" /></div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
          <button type="button" class="btn btn-danger" v-on:click="switchTheme()"><fmt:message key='generic.confirm'/></button>
        </div>
      </div>
    </div>
  </div>

</div>

<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/components/dayjsfilters.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/templates.js'/>"></script>