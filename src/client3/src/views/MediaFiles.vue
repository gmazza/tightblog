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
  <div v-if="!isFetching">
    <AppTitleBar />
    <AppUserNav />
    <div style="text-align: left; padding: 20px">
      <AppSuccessMessageBox :message="successMessage" @close-box="successMessage = null" />
      <AppErrorListMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = []"
      ></AppErrorListMessageBox>
      <h2>{{ $t('mediaFiles.title') }}</h2>
      <p class="pagetip" v-html="$t('mediaFiles.tip')"></p>
      <div class="control clearfix">
        <span style="padding-left: 7px">
          <input
            type="checkbox"
            v-bind:disabled="mediaFiles.length == 0"
            v-model="allFilesSelected"
            v-on:input="toggleCheckboxes(($event.target as HTMLInputElement).checked)"
            :title="$t('mediaFiles.selectAllLabel')"
          />
        </span>

        <span style="float: right">
          <!-- Folder list -->
          {{ $t('mediaFiles.viewFolder') }}
          <select v-model="currentFolderId" v-on:change="loadMediaFiles()" size="1" required>
            <option v-for="dir in mediaFolders" :key="dir.id" v-bind:value="dir.id">
              {{ dir.name }}
            </option>
          </select>
        </span>
      </div>
      <!-- Contents of media folder -->

      <div width="720px" height="500px" v-cloak>
        <ul id="myMenu">
          <li v-if="mediaFiles.length == 0" style="text-align: center; list-style-type: none">
            {{ $t('mediaFiles.folderEmpty') }}
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
                    weblogId
                  },
                  query: {
                    folderId: currentFolderId,
                    mediaFileId: mediaFile.id
                  }
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

              {{ strLimit(mediaFile.name, 47) }}

              <span style="float: right">
                <button
                  type="submit"
                  v-on:click="copyToClipboard(mediaFile)"
                  alt="Copy URL to clipboard"
                  title="Copy URL to clipboard"
                >
                  <img src="@/assets/copy_to_clipboard.png" class="icon" alt="icon" />
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
                weblogId
              },
              query: { folderId: currentFolderId }
            }"
          >
            <button type="button">
              <img src="@/assets/image_add.png" class="icon" alt="icon" />
              {{ $t('mediaFiles.addMediaFile') }}
            </button>
          </router-link>

          <span v-show="mediaFiles.length > 0">
            <button
              type="button"
              v-bind:disabled="filesSelectedCount == 0"
              v-on:click="confirmMoveFilesDialog.reveal"
              v-show="mediaFolders.length > 1"
            >
              {{ $t('mediaFiles.moveSelected') }}
            </button>

            <select
              id="moveTargetMenu"
              size="1"
              required
              v-model="targetFolderId"
              v-show="mediaFolders.length > 1"
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
            v-on:click="confirmDeleteFilesDialog.reveal"
          >
            {{ $t('common.deleteSelected') }}
          </button>

          <button
            type="button"
            v-on:click="confirmDeleteFolderDialog.reveal"
            v-show="mediaFolders.length > 1"
          >
            {{ $t('mediaFiles.deleteFolder') }}
          </button>
        </span>
      </div>

      <div class="menu-tr sidebarFade">
        <div class="sidebarInner">
          <div>
            <img src="@/assets/folder_add.png" class="icon" alt="icon" />{{
              $t('mediaFiles.addFolder')
            }}
            <div style="padding-left: 2em; padding-top: 1em">
              {{ $t('common.name') }}:
              <input type="text" v-model="newFolderName" size="10" maxlength="25" />
              <button type="button" v-on:click="addFolder()" v-bind:disabled="newFolderName == ''">
                {{ $t('mediaFiles.create') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Teleport to="#modal-div">
    <div v-if="confirmMoveFilesDialog.isRevealed.value" class="vueuse-modal-layout">
      <div class="vueuse-modal">
        <div class="modal-header">
          <h5
            class="modal-title"
            v-html="
              $t('mediaFiles.confirmMoveFilesTmpl', {
                count: filesSelectedCount,
                targetFolderName: targetFolder.name
              })
            "
          ></h5>
          <button
            @click="confirmMoveFilesDialog.cancel"
            type="button"
            class="btn-close ms-auto"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-footer">
          <button @click="moveFiles()">{{ $t('common.confirm') }}</button>
          <button @click="confirmMoveFilesDialog.cancel">{{ $t('common.cancel') }}</button>
        </div>
      </div>
    </div>
  </Teleport>
  <Teleport to="#modal-div">
    <div v-if="confirmDeleteFilesDialog.isRevealed.value" class="vueuse-modal-layout">
      <div class="vueuse-modal">
        <div class="modal-header">
          <h5
            class="modal-title"
            v-html="
              $t('mediaFiles.confirmDeleteFilesTmpl', {
                count: filesSelectedCount
              })
            "
          ></h5>
          <button
            @click="confirmDeleteFilesDialog.cancel"
            type="button"
            class="btn-close ms-auto"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-footer">
          <button @click="deleteFiles()">{{ $t('common.confirm') }}</button>
          <button @click="confirmDeleteFilesDialog.cancel">{{ $t('common.cancel') }}</button>
        </div>
      </div>
    </div>
  </Teleport>
  <Teleport to="#modal-div">
    <div v-if="confirmDeleteFolderDialog.isRevealed.value" class="vueuse-modal-layout">
      <div class="vueuse-modal">
        <div class="modal-header">
          <h5
            class="modal-title"
            v-if="currentFolderId"
            v-html="
              $t('mediaFiles.confirmDeleteFolderTmpl', {
                folderName: getFolderName(currentFolderId),
                count: mediaFiles.length
              })
            "
          ></h5>
          <button
            @click="confirmDeleteFolderDialog.cancel"
            type="button"
            class="btn-close ms-auto"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-footer">
          <button @click="deleteFolder()">{{ $t('common.confirm') }}</button>
          <button @click="confirmDeleteFolderDialog.cancel">{{ $t('common.cancel') }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script lang="ts">
import type { ErrorObj, MediaFile, MediaFolder, ErrorItem } from '../types'
import { AxiosError } from 'axios'
import { useConfirmDialog } from '@vueuse/core'
import * as api from '@/api'

const confirmMoveFilesDialogObj = useConfirmDialog()
const confirmDeleteFilesDialogObj = useConfirmDialog()
const confirmDeleteFolderDialogObj = useConfirmDialog()

export default {
  props: {
    weblogId: {
      required: true,
      type: String
    },
    folderId: {
      required: false,
      type: String
    }
  },
  data() {
    return {
      mediaFolders: [] as MediaFolder[],
      mediaFiles: [] as MediaFile[],
      allFilesSelected: false,
      newFolderName: null as string | null,
      currentFolderId: null as string | null,
      targetFolderId: null as string | null,
      successMessage: null as string | null,
      modalMessage: null,
      errorObj: {
        errors: []
      } as ErrorObj,
      isFetching: true
    }
  },
  computed: {
    moveToFolders: function () {
      // adding extra var to trigger cache refresh
      const compareVal = this.currentFolderId
      return this.mediaFolders.filter(function (item) {
        return item.id !== compareVal
      })
    },
    filesSelectedCount: function () {
      return this.mediaFiles.filter((file) => file.selected).length
    },
    targetFolder(): MediaFolder {
      const compareVal = this.targetFolderId
      return this.mediaFolders.find((item) => item.id === compareVal)!!
    },
    confirmMoveFilesDialog: function () {
      return confirmMoveFilesDialogObj
    },
    confirmDeleteFilesDialog: function () {
      return confirmDeleteFilesDialogObj
    },
    confirmDeleteFolderDialog: function () {
      return confirmDeleteFolderDialogObj
    }
  },
  methods: {
    strLimit: function (value: string, limit: number) {
      if (value.length > limit) {
        return value.substring(0, limit) + '...'
      }
      return value
    },
    getFolderName: function (folderId: string) {
      for (let i = 0; this.mediaFolders && i < this.mediaFolders.length; i++) {
        if (this.mediaFolders[i].id === folderId) {
          return this.mediaFolders[i].name
        }
      }
      return null
    },
    copyToClipboard: function (mediaFile: MediaFile) {
      const textarea = document.createElement('textarea')
      document.body.appendChild(textarea)
      let anchorTag

      if (mediaFile.imageFile === true) {
        anchorTag =
          (mediaFile.anchor ? '<a href="' + mediaFile.anchor + '">' : '') +
          '<img src="' +
          mediaFile.permalink +
          '"' +
          ' alt="' +
          (mediaFile.altText ? mediaFile.altText : mediaFile.name) +
          '"' +
          (mediaFile.titleText ? ' title="' + mediaFile.titleText + '"' : '') +
          '>' +
          (mediaFile.anchor ? '</a>' : '')
      } else {
        anchorTag =
          '<a href="' +
          mediaFile.permalink +
          '"' +
          (mediaFile.titleText ? ' title="' + mediaFile.titleText + '"' : '') +
          '>' +
          (mediaFile.altText ? mediaFile.altText : mediaFile.name) +
          '</a>'
      }

      textarea.value = anchorTag
      textarea.select()
      document.execCommand('copy')
      textarea.remove()
    },
    toggleCheckboxes: function (checkAll: boolean) {
      this.mediaFiles.forEach((file) => {
        // Vue.forceUpdate to force model refreshing
        file.selected = checkAll
        this.$forceUpdate()
      })
    },
    addFolder: async function () {
      if (!this.newFolderName) {
        return
      }
      this.messageClear()
      try {
        const response = await api.addFolder(this.weblogId, this.newFolderName)
        this.currentFolderId = response.data.id
        this.newFolderName = ''
        this.loadMediaFolders()
      } catch (error) {
        this.commonErrorResponse(error)
      }
    },
    loadMediaFolders: function () {
      api.loadMediaFolders(this.weblogId).then((response) => {
        this.mediaFolders = response.data
        if (this.mediaFolders && this.mediaFolders.length > 0) {
          this.loadMediaFiles()
        }
      })
    },
    loadMediaFiles: function () {
      if (!this.currentFolderId) {
        this.currentFolderId = this.mediaFolders[0].id
      }
      api.loadMediaFiles(this.currentFolderId).then((response) => {
        this.mediaFiles = response.data
        this.allFilesSelected = false
      })
    },
    deleteFolder: function () {
      if (!this.currentFolderId) {
        return
      }
      this.messageClear()
      api
        .deleteFolder(this.currentFolderId)
        .then(() => {
          this.confirmDeleteFolderDialog.cancel()
          this.successMessage = this.$t('mediaFiles.deleteFolderSuccess')
          this.currentFolderId = null
          this.loadMediaFolders()
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    deleteFiles: function () {
      this.messageClear()
      const filesToDelete = [] as string[]
      this.mediaFiles.forEach((mediaFile) => {
        if (mediaFile.selected) filesToDelete.push(mediaFile.id)
      })
      api
        .deleteMediaFiles(this.weblogId, filesToDelete)
        .then(() => {
          this.confirmDeleteFilesDialog.cancel()
          this.successMessage = this.$t('mediaFiles.deleteFileSuccess')
          this.loadMediaFiles()
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    moveFiles: function () {
      if (!this.targetFolderId) {
        return
      }
      this.messageClear()
      const filesToMove = [] as string[]
      this.mediaFiles.forEach((mediaFile) => {
        if (mediaFile.selected) filesToMove.push(mediaFile.id)
      })
      api
        .moveFiles(this.weblogId, this.targetFolderId, filesToMove)
        .then(() => {
          this.confirmMoveFilesDialog.cancel()
          this.successMessage = this.$t('mediaFiles.moveSuccess')
          this.loadMediaFiles()
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    commonErrorResponse: function (error: unknown) {
      if (error instanceof AxiosError && error.response?.status === 401) {
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/login'
      } else {
        if (error instanceof AxiosError) {
          this.errorObj.errors.push(
            ...Object.values(error.response?.data.errors as Record<string, ErrorItem>)
              .map((errorItem: ErrorItem) => errorItem.message)
              .filter((message) => message !== null)
          )
        } else {
          this.errorObj = { errors: ['An unknown error occurred'] }
        }
        window.scrollTo(0, 0)
      }
    },
    messageClear: function () {
      this.successMessage = null
      this.errorObj = { errors: [] }
    }
  },
  async created() {
    if (this.folderId) {
      this.currentFolderId = this.folderId
    }
    await this.loadMediaFolders()
    this.isFetching = false
  }
}
</script>

<style>
.clipboardIcon {
  border: 0;
}
</style>
