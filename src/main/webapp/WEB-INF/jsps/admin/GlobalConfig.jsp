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

<p class="subtitle"><fmt:message key="globalConfig.subtitle" /></p>
<script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
<!--script-- src="https://cdn.jsdelivr.net/npm/vue"></!--script-->
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
</script>

<div id="template">

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/admin/globalConfig'/>"/>

<div id="successMessageDiv" class="alert alert-success" role="alert" v-if="successMessage" v-cloak>
    {{successMessage}}
    <button type="button" class="close" v-on:click="successMessage = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
</div>

<p><fmt:message key="globalConfig.prompt"/></p>

<table class="formtable">

    <tr>
        <td colspan="3"><h2><fmt:message key="globalConfig.siteSettings" /></h2></td>
    </tr>
    <tr>
        <td class="label"><fmt:message key="globalConfig.frontpageWeblogHandle" /></td>
        <td class="field">
            <select v-model="webloggerProps.mainBlog.id" size="1">
                <option v-for="(value, key) in metadata.weblogList" v-bind:value="key">{{value}}</option>
                <option value=""><fmt:message key="globalConfig.none" /></option>
            </select>
        </td>
        <td class="description"><fmt:message key="globalConfig.tip.frontpageWeblogHandle"/></td>
    </tr>
    <tr>
        <td class="label"><fmt:message key="globalConfig.requiredRegistrationProcess" /></td>
        <td class="field">
             <select v-model="webloggerProps.registrationPolicy" size="1" required>
                 <option v-for="(value, key) in metadata.registrationOptions" v-bind:value="key">{{value}}</option>
             </select>
        </td>
        <td class="description"><fmt:message key="globalConfig.tip.requiredRegistrationProcess"/></td>
    </tr>
    <tr>
        <td class="label"><fmt:message key="globalConfig.newUsersCreateBlogs" /></td>
        <td class="field"><input type="checkbox" v-model="webloggerProps.usersCreateBlogs"></td>
        <td class="description"><fmt:message key="globalConfig.tip.newUsersCreateBlogs"/></td>
    </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3"><h2><fmt:message key="globalConfig.weblogSettings" /></h2></td>
    </tr>
        <tr>
               <td class="label"><fmt:message key="globalConfig.htmlWhitelistLevel" /></td>
               <td class="field">
                   <select v-model="webloggerProps.blogHtmlPolicy" size="1" required>
                       <option v-for="(value, key) in metadata.blogHtmlLevels" v-bind:value="key">{{value}}</option>
                   </select>
               </td>
               <td class="description"><fmt:message key="globalConfig.tip.htmlWhitelistLevel"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.allowCustomTheme" /></td>
            <td class="field"><input type="checkbox" v-model="webloggerProps.usersCustomizeThemes"></td>
            <td class="description"><fmt:message key="globalConfig.tip.allowCustomTheme"/></td>
        </tr>
        <c:if test="${showMediaFileTab}">
            <tr>
                <td class="label"><fmt:message key="globalConfig.maxMediaFileAllocationMb" /></td>
                <td class="field"><input type="number" v-model="webloggerProps.maxFileUploadsSizeMb" size='35'></td>
                <td class="description"><fmt:message key="globalConfig.tip.maxMediaFileAllocationMb"/></td>
            </tr>
        </c:if>
        <tr>
            <td class="label"><fmt:message key="globalConfig.defaultAnalyticsTrackingCode" /></td>
            <td class="field"><textarea rows="10" cols="70" v-model="webloggerProps.defaultAnalyticsCode"></textarea></td>
            <td class="description"><fmt:message key="globalConfig.tip.defaultAnalyticsTrackingCode"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.allowAnalyticsCodeOverride" /></td>
            <td class="field"><input type="checkbox" v-model="webloggerProps.usersOverrideAnalyticsCode"></td>
            <td class="description"><fmt:message key="globalConfig.tip.allowAnalyticsCodeOverride"/></td>
        </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3"><h2><fmt:message key="globalConfig.commentSettings" /></h2></td>
    </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.enableComments" /></td>
            <td class="field">
                <select v-model="webloggerProps.commentPolicy" size="1" required>
                    <option v-for="(value, key) in metadata.commentOptions" v-bind:value="key">{{value}}</option>
                </select>
            </td>
            <td class="description"></td>
        </tr>
        <tr v-if="webloggerProps.commentPolicy != 'NONE'">
            <td class="label"><fmt:message key="globalConfig.commentHtmlWhitelistLevel" /></td>
            <td class="field">
                <select v-model="webloggerProps.commentHtmlPolicy" size="1" required>
                    <option v-for="(value, key) in metadata.commentHtmlLevels" v-bind:value="key">{{value}}</option>
                </select>
            </td>
            <td class="description"><fmt:message key="globalConfig.tip.commentHtmlWhitelistLevel"/></td>
        </tr>
        <tr v-if="webloggerProps.commentPolicy != 'NONE'">
            <td class="label"><fmt:message key="globalConfig.spamPolicy" /></td>
            <td class="field">
                <select v-model="webloggerProps.spamPolicy" size="1" required>
                    <option v-for="(value, key) in metadata.spamOptions" v-bind:value="key">{{value}}</option>
                </select>
            </td>
            <td class="description"><fmt:message key="globalConfig.tip.spamPolicy"/></td>
        </tr>
        <tr v-if="webloggerProps.commentPolicy != 'NONE'">
            <td class="label"><fmt:message key="globalConfig.emailComments" /></td>
            <td class="field"><input type="checkbox" v-model="webloggerProps.usersCommentNotifications"></td>
            <td class="description"><fmt:message key="globalConfig.tip.emailComments"/></td>
        </tr>
        <tr v-if="webloggerProps.commentPolicy != 'NONE'">
            <td class="label"><fmt:message key="globalConfig.ignoreUrls" /></td>
            <td class="field"><textarea rows="7" cols="80" v-model="webloggerProps.globalSpamFilter"></textarea></td>
            <td class="description"><fmt:message key="globalConfig.tip.ignoreUrls"/></td>
        </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
</table>

<div class="control">
    <input class="buttonBox" type="button" value="<fmt:message key='generic.save'/>" v-on:click="updateProperties()"/>
</div>

</div>

<script src="<c:url value='/tb-ui/scripts/globalconfig.js'/>"></script>