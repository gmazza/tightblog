<!--
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
-->
<template>
  <div style="text-align: left; padding: 20px">
    <AppSuccessMessageBox
      :message="successMessage"
      @close-box="successMessage = null"
    />
    <AppErrorListMessageBox
      :in-error-obj="errorObj"
      @close-box="errorObj.errors = null"
    ></AppErrorListMessageBox>
    <h2>{{ $t("mediaFiles.title") }}</h2>
    <p class="pagetip" v-html="$t('mediaFiles.tip')"></p>
    <div class="control clearfix">
      <span style="padding-left: 7px">
        <input
          type="checkbox"
          v-bind:disabled="mediaFiles.length == 0"
          v-model="allFilesSelected"
          v-on:input="toggleCheckboxes($event.target.checked)"
          :title="$t('mediaFiles.selectAllLabel')"
        />
      </span>

      <span style="float: right">
        <!-- Folder list -->
        {{ mediaFiles.viewFolder }}
        <select
          v-model="currentFolderId"
          v-on:change="loadMediaFiles()"
          size="1"
          required
        >
          <option
            v-for="dir in mediaDirectories"
            :key="dir.id"
            v-bind:value="dir.id"
          >
            {{ dir.name }}
          </option>
        </select>
      </span>
    </div>
    <!-- Contents of media folder -->

    <div width="720px" height="500px" v-cloak>
      <ul id="myMenu">
        <li
          v-if="mediaFiles.length == 0"
          style="text-align: center; list-style-type: none"
        >
          {{ $t("mediaFiles.folderEmpty") }}
        </li>

        <li
          v-else
          class="align-images"
          v-for="mediaFile in mediaFiles"
          v-bind:id="mediaFile.id"
          :key="mediaFile.id"
        >
          <div class="mediaObject">
            <router-link
              :to="{
                name: 'mediaFileEdit',
                params: {
                  weblogId,
                  folderId: currentFolderId,
                  mediaFileId: mediaFile.id,
                },
              }"
            >
              <img
                v-if="mediaFile.imageFile"
                v-bind:src="mediaFile.thumbnailURL"
                v-bind:alt="mediaFile.altText"
                v-bind:title="mediaFile.name"
              />

              <img
                v-else
                src="@/assets/page_white.png"
                v-bind:alt="mediaFile.altText"
                style="padding: 40px 50px"
              />
            </router-link>
          </div>

          <div class="mediaObjectInfo">
            <input
              type="checkbox"
              name="idSelections"
              v-model="mediaFile.selected"
              v-bind:value="mediaFile.id"
            />

            {{ mediaFile.name | str_limit(47) }}

            <span style="float: right">
              <button
                type="submit"
                v-on:click="copyToClipboard(mediaFile)"
                alt="Copy URL to clipboard"
                title="Copy URL to clipboard"
              >
                <img
                  src="@/assets/copy_to_clipboard.png"
                  border="0"
                  alt="icon"
                />
              </button>
            </span>
          </div>
        </li>
      </ul>
    </div>

    <div style="clear: left"></div>

    <div class="control clearfix" style="margin-top: 15px">
      <span style="padding-left: 7px">
        <router-link
          :to="{
            name: 'mediaFileEdit',
            params: {
              weblogId,
              folderId: currentFolderId,
            },
          }"
        >
          <button type="button">
            <img src="@/assets/image_add.png" border="0" alt="icon" />
            {{ $t("mediaFiles.add") }}
          </button>
        </router-link>

        <span v-show="mediaFiles.length > 0">
          <button
            type="button"
            v-bind:disabled="filesSelectedCount == 0"
            v-on:click="moveFiles()"
            v-show="mediaDirectories.length > 1"
          >
            {{ $t("mediaFiles.moveSelected") }}
          </button>

          <select
            id="moveTargetMenu"
            size="1"
            required
            v-model="targetFolderId"
            v-show="mediaDirectories.length > 1"
          >
            <option v-for="dir in moveToFolders" :value="dir.id" :key="dir.id">
              {{ dir.name }}
            </option>
          </select>
        </span>
      </span>

      <span style="float: right">
        <button
          type="button"
          v-bind:disabled="filesSelectedCount == 0"
          v-on:click="deleteFiles()"
        >
          {{ $t("common.deleteSelected") }}
        </button>

        <button
          type="button"
          v-on:click="deleteFolder()"
          v-show="mediaDirectories.length > 1"
        >
          {{ $t("mediaFiles.deleteFolder") }}
        </button>
      </span>
    </div>

    <div class="menu-tr sidebarFade">
      <div class="sidebarInner">
        <div>
          <img src="@/assets/folder_add.png" border="0" alt="icon" />{{
            $t("mediaFiles.addFolder")
          }}
          <div style="padding-left: 2em; padding-top: 1em">
            {{ $t("common.name") }}:
            <input
              type="text"
              v-model="newFolderName"
              size="10"
              maxlength="25"
            />
            <button
              type="button"
              v-on:click="addFolder()"
              v-bind:disabled="newFolderName == ''"
            >
              {{ $t("mediaFiles.create") }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    weblogId: {
      required: true,
      type: String,
    },
    folderId: {
      required: false,
      type: String,
    },
  },
  data() {
    return {
      mediaDirectories: [],
      mediaFiles: [],
      allFilesSelected: false,
      newFolderName: null,
      currentFolderId: null,
      targetFolderId: null,
      successMessage: null,
      modalMessage: null,
      errorObj: {},
    };
  },
  computed: {
    moveToFolders: function () {
      // adding extra var to trigger cache refresh
      const compareVal = this.currentFolderId;
      return this.mediaDirectories.filter(function (item) {
        return item.id !== compareVal;
      });
    },
    filesSelectedCount: function () {
      return this.mediaFiles.filter((file) => file.selected).length;
    },
    targetFolder: function () {
      const compareVal = this.targetFolderId;
      return this.mediaDirectories.filter(function (item) {
        return item.id === compareVal;
      });
    },
  },
  methods: {
    getFolderName: function (folderId) {
      for (
        let i = 0;
        this.mediaDirectories && i < this.mediaDirectories.length;
        i++
      ) {
        if (this.mediaDirectories[i].id === folderId) {
          return this.mediaDirectories[i].name;
        }
      }
      return null;
    },
    copyToClipboard: function (mediaFile) {
      const textarea = document.createElement("textarea");
      document.body.appendChild(textarea);
      let anchorTag;

      if (mediaFile.imageFile === true) {
        anchorTag =
          (mediaFile.anchor ? '<a href="' + mediaFile.anchor + '">' : "") +
          '<img src="' +
          mediaFile.permalink +
          '"' +
          ' alt="' +
          (mediaFile.altText ? mediaFile.altText : mediaFile.name) +
          '"' +
          (mediaFile.titleText ? ' title="' + mediaFile.titleText + '"' : "") +
          ">" +
          (mediaFile.anchor ? "</a>" : "");
      } else {
        anchorTag =
          '<a href="' +
          mediaFile.permalink +
          '"' +
          (mediaFile.titleText ? ' title="' + mediaFile.titleText + '"' : "") +
          ">" +
          (mediaFile.altText ? mediaFile.altText : mediaFile.name) +
          "</a>";
      }

      textarea.value = anchorTag;
      textarea.select();
      document.execCommand("copy");
      textarea.remove();
    },
    toggleCheckboxes: function (checkAll) {
      this.mediaFiles.forEach((file) => {
        // Vue.set to force model refreshing
        this.$set(file, "selected", checkAll);
      });
    },
    addFolder: function () {
      if (!this.newFolderName) {
        return;
      }
      this.messageClear();
      const newFolder = {
        name: this.newFolderName,
      };
      this.axios
        .put(
          "/tb-ui/authoring/rest/weblog/" + this.weblogId + "/mediadirectories",
          newFolder
        )
        .then((response) => {
          this.currentFolderId = response.data;
          this.newFolderName = "";
          this.loadMediaFolders();
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    loadMediaFolders: function () {
      this.axios
        .get(
          "/tb-ui/authoring/rest/weblog/" + this.weblogId + "/mediadirectories"
        )
        .then((response) => {
          this.mediaDirectories = response.data;
          if (this.mediaDirectories && this.mediaDirectories.length > 0) {
            this.loadMediaFiles();
          }
        });
    },
    loadMediaFiles: function () {
      if (!this.currentFolderId) {
        this.currentFolderId = this.mediaDirectories[0].id;
      }
      this.axios
        .get(
          "/tb-ui/authoring/rest/mediadirectories/" +
            this.currentFolderId +
            "/files"
        )
        .then((response) => {
          this.mediaFiles = response.data;
          this.allFilesSelected = false;
        });
    },
    deleteFolder: function () {
      this.messageClear();
      const h = this.$createElement;
      const messageVNode = h("div", {
        domProps: {
          innerHTML: this.$t("mediaFiles.confirmDeleteFolderTmpl", {
            folderName: this.getFolderName(this.currentFolderId),
            count: this.mediaFiles.length,
          }),
        },
      });
      this.$bvModal
        .msgBoxConfirm([messageVNode], {
          title: this.$t("common.confirmDelete"),
          okTitle: this.$t("common.confirm"),
          cancelTitle: this.$t("common.cancel"),
          centered: true,
        })
        .then((value) => {
          if (value) {
            this.axios
              .delete(
                "/tb-ui/authoring/rest/mediadirectory/" + this.currentFolderId
              )
              .then((response) => {
                this.successMessage = this.$t("mediaFiles.deleteFolderSuccess");
                this.currentFolderId = null;
                this.loadMediaFolders();
              })
              .catch((error) => this.commonErrorResponse(error));
          }
        });
    },
    deleteFiles: function () {
      this.messageClear();
      const h = this.$createElement;
      const messageVNode = h("div", {
        domProps: {
          innerHTML: this.$t("mediaFiles.confirmDeleteFilesTmpl", {
            count: this.filesSelectedCount,
          }),
        },
      });
      this.$bvModal
        .msgBoxConfirm([messageVNode], {
          title: this.$t("common.confirmDelete"),
          okTitle: this.$t("common.confirm"),
          cancelTitle: this.$t("common.cancel"),
          centered: true,
        })
        .then((value) => {
          if (value) {
            const filesToDelete = [];
            this.mediaFiles.forEach((mediaFile) => {
              if (mediaFile.selected) filesToDelete.push(mediaFile.id);
            });

            this.axios
              .post(
                "/tb-ui/authoring/rest/mediafiles/weblog/" + this.weblogId,
                filesToDelete
              )
              .then((response) => {
                this.successMessage = this.$t("mediaFiles.deleteFileSuccess");
                this.loadMediaFiles();
              })
              .catch((error) => this.commonErrorResponse(error));
          }
        });
    },
    moveFiles: function () {
      this.messageClear();
      if (!this.targetFolder) {
        return;
      }
      const h = this.$createElement;
      const messageVNode = h("div", {
        domProps: {
          innerHTML: this.$t("mediaFiles.confirmMoveFilesTmpl", {
            count: this.filesSelectedCount,
            targetFolderName: this.targetFolder.name,
          }),
        },
      });
      this.$bvModal
        .msgBoxConfirm([messageVNode], {
          title: this.$t("common.confirmDelete"),
          okTitle: this.$t("common.confirm"),
          cancelTitle: this.$t("common.cancel"),
          centered: true,
        })
        .then((value) => {
          if (value) {
            const filesToMove = [];
            this.mediaFiles.forEach((mediaFile) => {
              if (mediaFile.selected) filesToMove.push(mediaFile.id);
            });

            this.axios
              .post(
                "/tb-ui/authoring/rest/mediafiles/weblog/" +
                  this.weblogId +
                  "/todirectory/" +
                  this.targetFolderId,
                filesToMove
              )
              .then(() => {
                this.successMessage = this.$t("mediaFiles.moveSuccess");
                this.loadMediaFiles();
              })
              .catch((error) => this.commonErrorResponse(error));
          }
        });
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = "/tb-ui/app/login";
      } else {
        this.errorObj = error.response.data;
      }
    },
    messageClear: function () {
      this.successMessage = null;
      this.errorObj = {};
    },
  },
  mounted: function () {
    this.currentFolderId = this.folderId;
    this.loadMediaFolders();
  },
};
</script>

<style></style>
