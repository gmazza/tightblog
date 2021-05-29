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
    var msg = {
        deleteDialogTitleTmpl: '<fmt:message key="weblogConfig.deleteConfirm"/>',
        deleteDialogInstructionTmpl: '<fmt:message key="weblogConfig.deleteInstruction"/>',
        successMessage: '<fmt:message key="generic.changes.saved"/>'
    };
    // Below populated for weblog update only
    var weblogId = "<c:out value='${weblogId}'/>";
    var homeUrl = "<c:url value='/tb-ui/app/home'/>";
</script>

<div id="template">

<success-message-box v-bind:message="successMessage" @close-box="successMessage = null"></success-message-box>
<error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

<c:choose>
    <%-- Create Weblog --%>
    <c:when test="${weblogId == null}">
        <fmt:message var="saveButtonText" key="weblogConfig.create.button.save"/>
        <fmt:message var="subtitlePrompt" key="weblogConfig.create.prompt"/>
        <input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/createWeblog'/>"/>
        <c:url var="refreshUrl" value="/tb-ui/app/createWeblog"/>
    </c:when>
    <%-- Update Weblog --%>
    <c:otherwise>
        <fmt:message var="saveButtonText" key="weblogConfig.button.update"/>
        <fmt:message var="subtitlePrompt" key="weblogConfig.prompt">
            <fmt:param value="${actionWeblog.handle}"/>
        </fmt:message>
        <c:url var="refreshUrl" value="/tb-ui/app/weblogConfig">
            <c:param name="weblogId" value="${param.weblogId}"/>
        </c:url>
    </c:otherwise>
</c:choose>

<input id="refreshURL" type="hidden" value="${refreshURL}"/>

<p class="subtitle">
    ${subtitlePrompt}
</p>

<table class="formtable">

    <tr>
        <td colspan="3"><h2><fmt:message key="weblogConfig.generalSettings"/></h2></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.websiteTitle"/>*</td>
        <td class="field"><input type="text" v-model="weblog.name" size="40" maxlength="255"></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.tagline"/></td>
        <td class="field"><input type="text" v-model="weblog.tagline" size="40" maxlength="255"></td>
        <td class="description"><fmt:message key="weblogConfig.tip.tagline"/></td>
    </tr>

    <tr>
        <td class="label"><label for="handle"><fmt:message key="weblogConfig.handle"/>*</label></td>
        <td class="field">
        <input id="handle" type="text" v-model="weblog.handle" size="30" maxlength="30"
        <c:choose>
            <c:when test="${weblogId == null}">required</c:when>
            <c:otherwise>readonly</c:otherwise>
        </c:choose>
        >
            <br>
            <span style="text-size:70%">
                <fmt:message key="weblogConfig.weblogUrl"/>:&nbsp;
                {{metadata.absoluteSiteURL}}/<span style="color:red">{{weblog.handle}}</span>
            </span>
        </td>
        <td class="description"><fmt:message key="weblogConfig.tip.handle"/></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.about"/></td>
        <td class="field"><textarea v-model="weblog.about" rows="3" cols="40" maxlength="255"></textarea></td>
        <td class="description"><fmt:message key="weblogConfig.tip.about"/></td>
    </tr>

    <c:if test="${weblogId == null}">
        <tr>
        <td class="label"><label for="theme"><fmt:message key="weblogConfig.theme"/>*</label></td>
        <td class="field">
        <select id="theme" v-model="weblog.theme" size="1">
            <option v-for="(theme, key) in metadata.sharedThemeMap" v-bind:value="key">{{theme.name}}</option>
        </select>
        <div style="height:400px">
            <p>{{metadata.sharedThemeMap[weblog.theme].description}}</p>
            <img v-bind:src="metadata.absoluteSiteURL + metadata.sharedThemeMap[weblog.theme].previewPath"></img>
        </div>
        </td>
        <td class="description"><fmt:message key="weblogConfig.tip.theme"/></td>
        </tr>
    </c:if>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.editFormat"/></td>
        <td class="field">
            <select v-model="weblog.editFormat" size="1" required>
                <option v-for="(value, key) in metadata.editFormats" v-bind:value="key">{{value}}</option>
            </select>
       </td>
       <td class="description"><fmt:message key="weblogConfig.tip.editFormat"/></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.visible"/></td>
        <td class="field"><input type="checkbox" v-model="weblog.visible"></td>
        <td class="description"><fmt:message key="weblogConfig.tip.visible"/></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.entriesPerPage"/></td>
        <td class="field"><input type="number" min="1" max="100" step="1" v-model="weblog.entriesPerPage" size="3"></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.locale"/>*</td>
        <td class="field">
            <select v-model="weblog.locale" size="1">
                <option v-for="(value, key) in metadata.locales" v-bind:value="key">{{value}}</option>
            </select>
        </td>
        <td class="description"><fmt:message key="weblogConfig.tip.locale"/></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.timeZone"/>*</td>
        <td class="field">
            <select v-model="weblog.timeZone" size="1">
                <option v-for="(value, key) in metadata.timezones" v-bind:value="key">{{value}}</option>
            </select>
        </td>
        <td class="description"><fmt:message key="weblogConfig.tip.timezone"/></td>
    </tr>

    <tr v-if="metadata.usersOverrideAnalyticsCode">
        <td class="label"><fmt:message key="weblogConfig.analyticsTrackingCode"/></td>
        <td class="field"><textarea v-model="weblog.analyticsCode" rows="10" cols="70" maxlength="1200"></textarea></td>
        <td class="description"><fmt:message key="weblogConfig.tip.analyticsTrackingCode"/></td>
    </tr>

<c:if test="${globalCommentPolicy != 'NONE'}">

    <tr>
        <td colspan="3"><h2><fmt:message key="weblogConfig.commentSettings"/></h2></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.allowComments"/></td>
        <td class="field">
            <select v-model="weblog.allowComments" size="1">
                <option v-for="(value, key) in metadata.commentOptions" v-bind:value="key">{{value}}</option>
            </select>
        </td>
    </tr>

    <tr v-show="weblog.allowComments != 'NONE'">
        <td class="label"><fmt:message key="weblogConfig.defaultCommentDays"/></td>
        <td class="field">
            <select v-model="weblog.defaultCommentDays" size="1">
                <option v-for="(value, key) in metadata.commentDayOptions" v-bind:value="key">{{value}}</option>
            </select>
        </td>
    </tr>

    <tr v-if="weblog.allowComments != 'NONE' && weblog.id != null">
        <td class="label"><fmt:message key="weblogConfig.applyCommentDefaults"/></td>
        <td class="field"><input type="checkbox" v-model="weblog.applyCommentDefaults"></td>
    </tr>

    <tr v-show="weblog.allowComments != 'NONE'">
        <td class="label"><fmt:message key="weblogConfig.spamPolicy"/></td>
        <td class="field">
            <select v-model="weblog.spamPolicy" size="1">
                <option v-for="(value, key) in metadata.spamOptions" v-bind:value="key">{{value}}</option>
            </select>
        </td>
        <td class="description"><fmt:message key="weblogConfig.tip.spamPolicy"/></td>
    </tr>

    <tr v-show="weblog.allowComments != 'NONE'">
        <td class="label"><fmt:message key="weblogConfig.ignoreUrls"/></td>
        <td class="field"><textarea v-model="weblog.blacklist" rows="7" cols="70"></textarea></td>
        <td class="description"><fmt:message key="weblogConfig.tip.ignoreUrls"/></td>
    </tr>

</c:if>

</table>

<br>

<div class="control">
    <span>
        <button type="button" class="buttonBox" v-on:click="updateWeblog()">${saveButtonText}</button>
        <button type="button" class="buttonBox" v-on:click="cancelChanges()"><fmt:message key='generic.cancel'/></button>
    </span>

    <c:if test="${weblogId != null}">
        <span style="float:right">
            <button type="button" data-toggle="modal" data-target="#deleteWeblogModal">
                <fmt:message key="weblogConfig.button.delete"/>
            </button>
        </span>
    </c:if>
</div>

<br><br>

<!-- Delete weblog modal -->
<div class="modal fade" id="deleteWeblogModal" tabindex="-1" role="dialog" aria-labelledby="deleteModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" v-html="deleteDialogTitle"></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <span id="confirmDeleteMsg" class="text-danger"><fmt:message key="weblogConfig.deleteWarning"/></span><br>
        <span id="confirmDeleteMsg" v-html="deleteDialogInstruction"></span>
        <div>
          <label for="newTag"><fmt:message key='weblogConfig.handle'/>:</label>
          <input v-model="deleteHandle" type="text">
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" v-on:click="deleteWeblog()"><fmt:message key='generic.delete'/></button>
      </div>
    </div>
  </div>
</div>

</div>

<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/weblogconfig.js'/>"></script>

