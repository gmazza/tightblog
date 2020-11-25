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
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var templateId = "<c:out value='${param.templateId}'/>";
    var templateName = "<c:out value='${param.templateName}'/>";
    var weblogUrl = "<c:out value='${actionWeblogURL}'/>";
</script>

<c:url var="refreshUrl" value="/tb-ui/app/authoring/templateedit">
    <c:param name="weblogId" value="${param.weblogId}"/>
    <c:param name="templateId" value="${param.templateId}"/>
    <c:param name="templateName" value="${param.templateName}"/>
</c:url>

<input id="refreshURL" type="hidden" value="${refreshURL}"/>

<div id="template">

<div id="successMessageDiv" class="alert alert-success" role="alert" v-show="showSuccessMessage" v-cloak>
    <p><fmt:message key="generic.changes.saved"/> - {{templateData.lastModified | standard_datetime}}</p>
    <button type="button" class="close" v-on:click="showSuccessMessage = false" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
</div>

<error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

<p class="subtitle">
   <fmt:message key="templateEdit.subtitle"/>
</p>

<p class="pagetip"><fmt:message key="templateEdit.tip" /></p>

<table cellspacing="5">
    <tr>
        <td class="label"><fmt:message key="generic.name"/>&nbsp;</td>
        <td class="field">
            <input id="name" type="text" v-model="templateData.name" size="50" maxlength="255" style="background: #e5e5e5" v-bind:readonly="templateData.derivation != 'Blog-Only'"/>
            <span v-if="templateLoaded && templateData.role.accessibleViaUrl">
                <br/>
                <c:out value="${actionWeblogURL}"/>page/<span id="linkPreview" style="color:red">{{templateData.name}}</span>
                <span v-if="lastSavedName != null">
                    [<a id="launchLink" v-on:click="launchPage()"><fmt:message key="templateEdit.launch" /></a>]
                </span>
            </span>
        </td>
    </tr>

    <tr v-if="templateLoaded">
        <td class="label"><fmt:message key="templateEdit.role" />&nbsp;</td>
        <td class="field">
             <span>{{templateData.role.readableName}}</span>
        </td>
    </tr>

    <tr v-if="templateLoaded && !templateData.role.singleton">
        <td class="label" valign="top" style="padding-top: 4px">
            <fmt:message key="generic.description"/>&nbsp;
        </td>
        <td class="field">
            <textarea id="description" type="text" v-model="templateData.description" cols="50" rows="2"></textarea>
        </td>
    </tr>

</table>

<textarea v-model="templateData.template" rows="20" style="width:100%"></textarea>

<c:url var="templatesUrl" value="/tb-ui/app/authoring/templates">
    <c:param name="weblogId" value="${param.weblogId}" />
</c:url>

<table style="width:100%">
    <tr>
        <td>
            <button type="button" v-on:click="saveTemplate()"><fmt:message key='generic.save'/></button>
            <button type="button" onclick="window.location='${templatesUrl}'"><fmt:message key='generic.cancel'/></button>
        </td>
    </tr>
</table>

</div>

<script src="<c:url value='/tb-ui/scripts/components/dayjsfilters.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/templateedit.js'/>"></script>
