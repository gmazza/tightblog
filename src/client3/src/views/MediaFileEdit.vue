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
  <div v-if="!isFetching" style="text-align: left; padding: 20px">
    <AppTitleBar />
    <AppErrorListMessageBox
      v-bind:inErrorObj="errorObj"
      @close-box="errorObj.errors = []"
    ></AppErrorListMessageBox>
    <h2>
      {{ $t(mediaFileId == null ? 'mediaFileAdd.title' : 'mediaFileEdit.title') }}
    </h2>
    <p
      class="pagetip"
      v-html="$t(mediaFileId == null ? 'mediaFileAdd.pageTip' : 'mediaFileEdit.pageTip')"
    ></p>
    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">
      <tr>
        <td class="entryEditFormLabel">
          <label for="fileControl">{{ $t('mediaFileEdit.fileLocation') }}</label>
        </td>
        <td>
          <input
            id="fileControl"
            type="file"
            ref="myMediaFile"
            size="30"
            v-on:change="handleFileUpload()"
            v-bind:required="myMediaFile == null"
          />
        </td>
      </tr>

      <tr>
        <td class="entryEditFormLabel">
          <label for="name">{{ $t('common.name') }}</label>
        </td>
        <td>
          <input
            id="name"
            type="text"
            v-model="mediaFileData.name"
            size="40"
            maxlength="255"
            required
          />
        </td>
      </tr>

      <tr>
        <td class="entryEditFormLabel">
          <label for="altText"
            >{{ $t('mediaFileEdit.altText') }}
            <AppHelpPopup :helpText="$t('mediaFileEdit.tooltip.altText')"
          /></label>
        </td>
        <td>
          <input
            id="altText"
            type="text"
            v-model="mediaFileData.altText"
            size="80"
            maxlength="255"
          />
        </td>
      </tr>

      <tr>
        <td class="entryEditFormLabel">
          <label for="titleText"
            >{{ $t('mediaFileEdit.titleText') }}
            <AppHelpPopup :helpText="$t('mediaFileEdit.tooltip.titleText')"
          /></label>
        </td>
        <td>
          <input
            id="titleText"
            type="text"
            v-model="mediaFileData.titleText"
            size="80"
            maxlength="255"
          />
        </td>
      </tr>

      <tr>
        <td class="entryEditFormLabel">
          <label for="anchor"
            >{{ $t('mediaFileEdit.anchor') }}
            <AppHelpPopup :helpText="$t('mediaFileEdit.tooltip.anchor')"
          /></label>
        </td>
        <td>
          <input id="anchor" type="text" v-model="mediaFileData.anchor" size="80" maxlength="255" />
        </td>
      </tr>

      <tr>
        <td class="entryEditFormLabel">
          <label for="notes">{{ $t('common.notes') }}</label>
        </td>
        <td>
          <input id="notes" type="text" v-model="mediaFileData.notes" size="80" maxlength="255" />
        </td>
      </tr>
      <tr v-if="mediaFileId != null">
        <td class="entryEditFormLabel">
          {{ $t('mediaFileEdit.fileInfo') }}
        </td>
        <td>
          <b>{{ $t('mediaFileEdit.fileType') }}</b
          >:
          {{ mediaFileData.contentType }}
          <b>{{ $t('mediaFileEdit.fileSize') }}</b
          >:
          {{ mediaFileData.length }}
          <b>{{ $t('mediaFileEdit.fileDimensions') }}</b
          >: {{ mediaFileData.width }} x {{ mediaFileData.height }} pixels
        </td>
      </tr>

      <tr v-if="mediaFileId != null">
        <td class="entryEditFormLabel">
          <label for="permalink">{{ $t('mediaFileEdit.permalink') }}</label>
        </td>
        <td>
          <input
            id="permalink"
            type="text"
            size="80"
            v-bind:value="mediaFileData.permalink"
            readonly
          />
        </td>
      </tr>

      <tr v-if="mediaFileId != null">
        <td class="entryEditFormLabel">
          <label for="directoryId">{{ $t('mediaFileEdit.folder') }}</label>
        </td>
        <td>
          <input
            id="directoryId"
            type="text"
            size="30"
            v-bind:value="mediaFileData.directory?.name"
            readonly
          />
        </td>
      </tr>
    </table>

    <br />
    <div class="control">
      <button type="button" v-on:click="saveMediaFile()">
        {{ $t('common.save') }}
      </button>
      <button type="button" @click="returnToMediaFileFolder()">
        {{ $t('common.cancel') }}
      </button>
    </div>
  </div>
</template>

<script lang="ts">
import type { MediaFile, ErrorObj, ErrorItem } from '@/types'
import * as api from '@/api'

export default {
  props: {
    weblogId: {
      required: true,
      type: String
    },
    folderId: {
      required: true,
      type: String
    },
    mediaFileId: {
      required: false,
      type: String
    }
  },
  data() {
    return {
      mediaFileData: {
        directory: {
          id: this.folderId
        }
      } as MediaFile,
      myMediaFile: null as File | null,
      errorObj: {
        errors: []
      } as ErrorObj,
      isFetching: true
    }
  },
  methods: {
    loadMediaFile: function () {
      api.loadMediaFile(this.mediaFileId!!).then((response) => {
        this.mediaFileData = response.data
      })
    },
    handleFileUpload: function () {
      this.myMediaFile = (this.$refs.myMediaFile as HTMLInputElement).files![0]
    },
    saveMediaFile: function () {
      const fd = new FormData()
      fd.append(
        'mediaFileData',
        new Blob([JSON.stringify(this.mediaFileData)], {
          type: 'application/json'
        })
      )
      if (this.myMediaFile) {
        fd.append('uploadFile', this.myMediaFile)
      }
      api
        .saveMediaFile(fd)
        .then(() => {
          this.returnToMediaFileFolder()
        })
        .catch((error) => {
          if (error.response?.status === 401) {
            window.location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/login'
          } else {
            this.errorObj.errors.push(
              ...Object.values(error.response?.data.errors as Record<string, ErrorItem>)
                .map((errorItem: ErrorItem) => errorItem.message)
                .filter((message) => message !== null)
            )
          }
        })
    },
    messageClear: function () {
      this.errorObj = { errors: [] }
    },
    returnToMediaFileFolder() {
      this.$router.push({
        name: 'mediaFiles',
        params: { weblogId: this.weblogId },
        query: { folderId: this.folderId }
      })
    }
  },
  async created() {
    if (this.mediaFileId) {
      await this.loadMediaFile()
    }
    this.isFetching = false
  }
}
</script>

<style></style>
