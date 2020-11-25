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

<script>
var contextPath = "${pageContext.request.contextPath}";
var weblogId = "<c:out value='${actionWeblog.id}'/>";
</script>

<div id="template">

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/members'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<p><fmt:message key="members.description" /></p>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            <div class="sidebarInner">
            <h4>
                <fmt:message key="members.roleDefinitionsTitle" />
            </h4>
            <hr size="1" noshade="noshade" />
            <fmt:message key="members.roleDefinitions" />
		    <br />
		    <br />
        </div>
            </div>
        </div>
    </div>
</div>

<success-message-box v-bind:message="successMessage" @close-box="successMessage=null"></success-message-box>
<error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

    <table class="table table-bordered table-hover">
        <thead class="thead-light">
          <tr>
             <th scope="col" width="20%"><fmt:message key="members.userName" /></th>
             <th scope="col" width="20%"><fmt:message key="members.owner" /></th>
             <th scope="col" width="20%"><fmt:message key="members.publisher" /></th>
             <th scope="col" width="20%"><fmt:message key="members.contributor" /></th>
             <th scope="col" width="20%"><fmt:message key="members.remove" /></th>
          </tr>
        </thead>
        <tbody v-cloak>
            <tr v-for="role in roles" v-bind:id="role.user.id" v-bind:class="{pending_member: role.pending}">
                <td>
                  <img src='<c:url value="/images/user.png"/>' border="0" alt="icon" />
                  {{role.user.userName}}
                </td>
                <td>
                  <input type="radio" v-model="role.weblogRole" value='OWNER'
                        <c:if test="${!userIsAdmin}">disabled</c:if>
                  >
                </td>
                <td>
                  <input type="radio" v-model="role.weblogRole" value='POST'
                        <c:if test="${!userIsAdmin}">disabled</c:if>
                  >
                </td>
                <td>
                  <input type="radio" v-model="role.weblogRole" value='EDIT_DRAFT'
                        <c:if test="${!userIsAdmin}">disabled</c:if>
                  >
                </td>
                <td>
                  <input type="radio" v-model="role.weblogRole" value='NOBLOGNEEDED'
                        <c:if test="${!userIsAdmin}">disabled</c:if>
                  >
                </td>
           </tr>
       </tbody>
    </table>
    <c:if test="${userIsAdmin}">
        <br>
          <div class="control">
              <button type="button" v-on:click="updateRoles()"><fmt:message key='generic.save'/></button>
          </div>

          <div v-if="!userToAdd" v-cloak>
               <fmt:message key="members.nobodyToAdd" />
          </div>

          <div v-else v-cloak class="menu-tr sidebarFade">
            <div class="sidebarInner">
              <p><fmt:message key="members.addMemberPrompt" /></p>

              <select v-model="userToAdd" size="1" required>
                <option v-for="(value, key) in potentialMembers" v-bind:value="key">{{value}}</option>
              </select>

              <fmt:message key="members.roles" />:

              <input type="radio" v-model="userToAddRole" value="OWNER"  />
              <fmt:message key="members.owner" />

              <input type="radio" v-model="userToAddRole" value="POST" />
              <fmt:message key="members.publisher" />

              <input type="radio" v-model="userToAddRole" value="EDIT_DRAFT" checked />
              <fmt:message key="members.contributor" /><br><br>

              <button type="button" v-on:click="addUserToWeblog()"><fmt:message key='generic.add'/></button>
            </div>
          </div>
      </c:if>

</div>

<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/members.js'/>"></script>
