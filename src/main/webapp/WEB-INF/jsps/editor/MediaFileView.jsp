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
  var directoryId = "<c:out value='${param.directoryId}'/>";
  var msg = {
    confirmDeleteFilesTmpl: "<fmt:message key='mediafileView.confirmDeleteFilesTmpl'/>",
    confirmDeleteFolderTmpl: "<fmt:message key='mediafileView.confirmDeleteFolderTmpl'/>",
    confirmMoveFilesTmpl: "<fmt:message key='mediafileView.confirmMoveFilesTmpl'/>"
  };
</script>

<div id="template">

  <input id="refreshURL" type="hidden"
    value="<c:url value='/tb-ui/app/authoring/mediaFileView'/>?weblogId=<c:out value='${param.weblogId}'/>" />

  <p class="pagetip">
    <fmt:message key="mediaFileView.rootPageTip" />
  </p>

  <success-message-box v-bind:message="successMessage" @close-box="successMessage = null"></success-message-box>
  <error-list-message-box v-bind:in-error-obj="errorObj" @close-box="errorObj.errors=null"></error-list-message-box>

  <div class="control clearfix">
    <span style="padding-left:7px">
      <input type="checkbox" v-bind:disabled="mediaFiles.length == 0" v-model="allFilesSelected"
        v-on:input="toggleCheckboxes($event.target.checked)"
        title="<fmt:message key='mediaFileView.selectAllLabel'/>" />
    </span>

    <span style="float:right">
      <%-- Folder to View combo-box --%>
      <fmt:message key="mediaFileView.viewFolder" />:
      <select v-model="currentFolderId" v-on:change="loadMediaFiles()" size="1" required>
        <option v-for="dir in mediaDirectories" v-bind:value="dir.id">{{dir.name}}</option>
      </select>
    </span>

  </div>

  <%-- ***************************************************************** --%>

  <%-- Media file folder contents --%>

  <div width="720px" height="500px" v-cloak>
    <ul id="myMenu">
      <li v-if="mediaFiles.length == 0" style="text-align: center;list-style-type:none;">
        <fmt:message key="mediaFileView.noFiles" />
      </li>

      <li v-if="mediaFiles.length > 0" class="align-images" v-for="mediaFile in mediaFiles" v-bind:id="mediaFile.id">
        <div class="mediaObject">
          <c:url var="editUrl" value="/tb-ui/app/authoring/mediaFileEdit">
            <c:param name="weblogId" value="${actionWeblog.id}" />
          </c:url>

          <a
            v-bind:href="'<c:out value='${editUrl}'/>&amp;directoryId=' + currentFolderId + '&amp;mediaFileId=' + mediaFile.id">
            <img v-if="mediaFile.imageFile" v-bind:src='mediaFile.thumbnailURL' v-bind:alt='mediaFile.altText'
              v-bind:title='mediaFile.name'>

            <img v-if="!mediaFile.imageFile" src='<c:out value="/images/page_white.png"/>'
              v-bind:alt='mediaFile.altText' style="padding:40px 50px;">
          </a>
        </div>

        <div class="mediaObjectInfo">
          <input type="checkbox" name="idSelections" v-model="mediaFile.selected" v-bind:value="mediaFile.id">

          {{mediaFile.name | str_limit(47) }}

          <span style="float:right">
            <input type="image" v-on:click="copyToClipboard(mediaFile)"
              src='<c:url value="/images/copy_to_clipboard.png"/>' alt="Copy URL to clipboard"
              title="Copy URL to clipboard">
          </span>
        </div>
      </li>
    </ul>
  </div>

  <div style="clear:left;"></div>

  <div class="control clearfix" style="margin-top: 15px">
    <span style="padding-left:7px">

      <c:url var="mediaFileAddURL" value="/tb-ui/app/authoring/mediaFileAdd">
        <c:param name="weblogId" value="${actionWeblog.id}" />
      </c:url>
      <a v-bind:href="'${mediaFileAddURL}&directoryId=' + currentFolderId" style='font-weight:bold;'>
        <button type="button">
          <img src='<c:url value="/images/image_add.png"/>' border="0" alt="icon">
          <fmt:message key="mediaFileView.add" />
        </button>
      </a>

      <span v-show="mediaFiles.length > 0">
        <button type="button" v-bind:disabled="filesSelectedCount == 0" v-on:click="showMoveFilesModal()"
          v-show="mediaDirectories.length > 1">
          <fmt:message key="mediaFileView.moveSelected" />
        </button>

        <select id="moveTargetMenu" size="1" required v-model="targetFolderId" v-show="mediaDirectories.length > 1">
          <option v-for="dir in moveToFolders" v-bind:value="dir.id">{{dir.name}}</option>
        </select>
      </span>
    </span>

    <span style="float:right">
      <button type="button" v-bind:disabled="filesSelectedCount == 0" v-on:click="showDeleteFilesModal()">
        <fmt:message key="mediaFileView.deleteSelected" />
      </button>

      <button type="button" v-on:click="showDeleteFolderModal()" v-show="mediaDirectories.length > 1">
        <fmt:message key="mediaFileView.deleteFolder" />
      </button>
    </span>
  </div>

  <div class="menu-tr sidebarFade">
    <div class="sidebarInner">
      <div>
        <img src='<c:url value="/images/folder_add.png"/>' border="0" alt="icon">
        <fmt:message key="mediaFileView.addFolder" /><br />
        <div style="padding-left:2em; padding-top:1em">
          <fmt:message key="generic.name" />:
          <input type="text" v-model="newFolderName" size="10" maxlength="25" />
          <button type="button" v-on:click="addFolder()" v-bind:disabled="newFolderName == ''">
            <fmt:message key="mediaFileView.create" />
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Delete media files modal -->
  <div class="modal fade" id="deleteFilesModal" tabindex="-1" role="dialog" aria-labelledby="deleteFilesTitle"
    aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="deleteFilesTitle">
            <fmt:message key="generic.confirm.delete" />
          </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <span v-html="modalMessage"></span>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">
            <fmt:message key='generic.cancel' /></button>
          <button type="button" class="btn btn-danger" v-on:click="deleteFiles()">
            <fmt:message key='generic.delete' /></button>
        </div>
      </div>
    </div>
  </div>

  <!-- Delete media folder modal -->
  <div class="modal fade" id="deleteFolderModal" tabindex="-1" role="dialog" aria-labelledby="deleteFolderTitle"
    aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="deleteFolderTitle">
            <fmt:message key="generic.confirm.delete" />
          </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <span v-html="modalMessage"></span>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">
            <fmt:message key='generic.cancel' /></button>
          <button type="button" class="btn btn-danger" v-on:click="deleteFolder()">
            <fmt:message key='generic.delete' /></button>
        </div>
      </div>
    </div>
  </div>

  <!-- Move files modal -->
  <div class="modal fade" id="moveFilesModal" tabindex="-1" role="dialog" aria-labelledby="moveFilesTitle"
    aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="moveFilesTitle">
            <fmt:message key="generic.confirm.move" />
          </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <span v-html="modalMessage"></span>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">
            <fmt:message key='generic.cancel' /></button>
          <button type="button" class="btn btn-warning" v-on:click="moveFiles()">
            <fmt:message key='generic.confirm' /></button>
        </div>
      </div>
    </div>
  </div>

</div>

<script src="<c:url value='/tb-ui/scripts/components/messages.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/components/stringfilters.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/mediafileview.js'/>"></script>