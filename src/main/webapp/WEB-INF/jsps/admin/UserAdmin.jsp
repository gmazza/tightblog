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
<script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
<!--script-- src="https://cdn.jsdelivr.net/npm/vue"></!--script-->
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.8.36/dayjs.min.js"></script>

<script>
   var contextPath = "${pageContext.request.contextPath}";
</script>

<div id="template">

<input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/admin/userAdmin'/>"/>

<div id="successMessageDiv" class="alert alert-success" role="alert" v-show="successMessage" v-cloak>
    {{successMessage}}
    <button type="button" class="close" v-on:click="successMessage = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
</div>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" v-show="errorObj.errors" v-cloak>
    <button type="button" class="close" v-on:click="errorObj.errors = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
    <ul class="list-unstyled">
       <li v-for="item in errorObj.errors">{{item.message}}</li>
    </ul>
</div>

<div id="pendingList" v-cloak>
   <span v-for="item in pendingList" style='color:red'>New registration request: {{item.screenName}} ({{item.emailAddress}}):
       <button type="button" v-on:click="approveUser(item.id)"><fmt:message key='userAdmin.accept' /></button>
       <button type="button" v-on:click="declineUser(item.id)"><fmt:message key='userAdmin.decline' /></button>
       <br>
   </span>
</div>

<p class="subtitle"><fmt:message key="userAdmin.subtitle" /></p>
<span id="userEdit" v-cloak>
    <select v-model="userToEdit" size="1">
        <option v-for="(value, key) in userList" v-bind:value="key">{{value}}</option>
    </select>
    <button type="button" v-on:click="loadUser()" style="margin:4px">
        <fmt:message key="generic.edit" />
    </button>
</span>

<table class="formtable" v-if="userBeingEdited" v-cloak>
  <tr>
      <td class="label"><fmt:message key="userSettings.username" /></td>
      <td class="field">
        <input type="text" size="30" maxlength="30" v-model="userBeingEdited.userName" readonly cssStyle="background: #e5e5e5">
      </td>
      <td class="description">
        <fmt:message key="userSettings.tip.username" />
      </td>
  </tr>

  <tr>
      <td class="label"><fmt:message key="userSettings.accountCreateDate" /></td>
      <td class="field">{{ formatDate(userBeingEdited.dateCreated) }}</td>
      <td class="description"></td>
  </tr>

  <tr>
      <td class="label"><fmt:message key="userSettings.lastLogin" /></td>
      <td class="field">{{ formatDate(userBeingEdited.lastLogin) }}</td>
      <td class="description"></td>
  </tr>

  <tr>
      <td class="label"><label for="screenName"><fmt:message key="userSettings.screenname" /></label></td>
      <td class="field"><input id="screenName" type="text" size="30" v-model="userBeingEdited.screenName" minlength="3" maxlength="30" required></td>
      <td class="description"><fmt:message key="userAdmin.tip.screenName" /></td>
  </tr>

  <tr>
      <td class="label"><label for="emailAddress"><fmt:message key="userSettings.email" /></label></td>
      <td class="field"><input id="emailAddress" type="email" size="40" v-model="userBeingEdited.emailAddress" maxlength="40" required></td>
      <td class="description"><fmt:message key="userAdmin.tip.email" /></td>
  </tr>

  <tr v-if="userBeingEdited.status == 'ENABLED'">
      <td class="label"><label for="passwordText"><fmt:message key="userSettings.password" /></label></td>
      <td class="field">
      <input id="passwordText" type="password" size="20" v-model="userCredentials.passwordText" minlength="8" maxlength="20"></td>
      <td class="description"><fmt:message key="userAdmin.tip.password" /></td>
  </tr>
  <tr v-if="userBeingEdited.status == 'ENABLED'">
      <td class="label"><label for="passwordConfirm"><fmt:message key="userSettings.passwordConfirm" /></label></td>
      <td class="field">
      <input id="passwordConfirm" type="password" size="20" v-model="userCredentials.passwordConfirm" minlength="8" maxlength="20"></td>
      <td class="description"><fmt:message key="userAdmin.tip.passwordConfirm" /></td>
  </tr>

  <tr>
      <td class="label"><label for="userStatus"><fmt:message key="userAdmin.userStatus" /></label></td>
      <td class="field">
          <select id="userStatus" v-model="userBeingEdited.status" size="1">
              <option v-for="(value, key) in metadata.userStatuses" v-bind:value="key">{{value}}</option>
          </select>
      </td>
      <td class="description"><fmt:message key="userAdmin.tip.userStatus" /></td>
  </tr>

  <tr>
      <td class="label"><label for="globalRole"><fmt:message key="userAdmin.globalRole" /></label></td>
      <td class="field">
          <select id="globalRole" v-model="userBeingEdited.globalRole" size="1">
              <option v-for="(value, key) in metadata.globalRoles" v-bind:value="key">{{value}}</option>
          </select>
      </td>
      <td class="description"><fmt:message key="userAdmin.tip.globalRole" /></td>
  </tr>

  <c:if test='${mfaEnabled}'>
      <tr>
          <td class="label"><label for="hasMfaSecret"><fmt:message key="userAdmin.hasMfaSecret" /></label></td>
          <td class="field">
              <input type="text" size="5" maxlength="5" v-model="userCredentials.hasMfaSecret" readonly>
              <span v-show="userCredentials.hasMfaSecret == true">
                  <input type="checkbox" id="eraseSecret" v-model="userCredentials.eraseMfaSecret">
                  <label for="eraseSecret"><fmt:message key="userAdmin.mfaSecret.erase" /></label>
              </span>
          </td>
          <td class="description"><fmt:message key="userAdmin.tip.mfaSecret"/></td>
      </tr>
  </c:if>
</table>

<br>

<div class="showinguser" v-if="userBeingEdited" v-cloak>
    <p><fmt:message key="userAdmin.userMemberOf"/></p>
    <table class="table table-bordered table-hover">
      <thead class="thead-light">
        <tr>
            <th style="width:30%"><fmt:message key="generic.weblog" /></th>
            <th style="width:10%"><fmt:message key="userAdmin.pending" /></th>
            <th style="width:10%"><fmt:message key="generic.role" /></th>
            <th style="width:25%"><fmt:message key="generic.edit" /></th>
            <th width="width:25%"><fmt:message key="userAdmin.manage" /></th>
        </tr>
      </thead>
      <tbody>
          <tr v-for="weblogRole in userBlogList">
              <td>
                  <a v-bind:href='weblogRole.weblog.absoluteURL'>
                      {{weblogRole.weblog.name}} [{{weblogRole.weblog.handle}}]
                  </a>
              </td>
              <td>
                  {{weblogRole.pending}}
              </td>
              <td>
                  {{weblogRole.weblogRole}}
              </td>
              <td>
                  <img src='<c:url value="/images/page_white_edit.png"/>' />
                  <a target="_blank" v-bind:href="'<c:url value='/tb-ui/app/authoring/entries'/>?weblogId=' + weblogRole.weblog.id">
                      <fmt:message key="userAdmin.editEntries" />
                  </a>
              </td>
              <td>
                  <img src='<c:url value="/images/page_white_edit.png"/>' />
                  <a target="_blank" v-bind:href="'<c:url value='/tb-ui/app/authoring/weblogConfig'/>?weblogId=' + weblogRole.weblog.id">
                      <fmt:message key="userAdmin.manage" /></a>
                  </a>
              </td>
          </tr>
      </tbody>
    </table>
</div>

<br>
<br>

<div class="control" v-show="userBeingEdited" v-cloak>
    <button type="button" class="buttonBox" v-on:click="updateUser()"><fmt:message key='generic.save'/></button>
    <button type="button" class="buttonBox" v-on:click="cancelChanges()"><fmt:message key='generic.cancel'/></button>
</div>

</div>

<script src="<c:url value='/tb-ui/scripts/useradmin.js'/>"></script>
