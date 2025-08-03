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
      <h2>{{ $t('tags.title') }}</h2>
      <p class="pagetip">{{ $t('tags.prompt') }}</p>

      <div class="tablenav">
        <span style="text-align: center" v-if="pageNum > 0 || tagData.hasMore" v-cloak>
          &laquo;
          <button type="button" v-bind:disabled="pageNum <= 0" v-on:click="previousPage()">
            {{ $t('common.prevPage') }}
          </button>
          |
          <button type="button" v-bind:disabled="!tagData.hasMore" v-on:click="nextPage()">
            {{ $t('common.nextPage') }}
          </button>
          &raquo;
        </span>

        <br />
      </div>

      <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
          <tr>
            <th width="4%">
              <input
                type="checkbox"
                v-bind:disabled="!tagData.tags || tagData.tags.length === 0"
                v-on:input="toggleCheckboxes(($event.target as HTMLInputElement).checked)"
                title="$t('selectAll')"
              />
            </th>
            <th width="20%">{{ $t('tags.columnTag') }}</th>
            <th width="10%">
              {{ $t('common.column.numEntries') }}
            </th>
            <th width="10%">
              {{ $t('common.column.firstEntry') }}
            </th>
            <th width="10%">
              {{ $t('common.column.lastEntry') }}
            </th>
            <th width="15%">
              {{ $t('tags.viewPublished') }}
            </th>
            <th width="13%"></th>
            <th width="13%"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tag in tagData.tags" :key="tag.id" v-cloak>
            <td class="center" style="vertical-align: middle">
              <input
                type="checkbox"
                v-bind:title="'checkbox for ' + tag.name"
                v-model="tag.selected"
                v-bind:value="tag.name"
              />
            </td>
            <td>{{ tag.name }}</td>
            <td>{{ tag.total }}</td>
            <td>{{ tag.firstEntry }}</td>
            <td>{{ tag.lastEntry }}</td>

            <td>
              <a v-bind:href="tag.viewUrl" target="_blank">{{ $t('tags.columnView') }}</a>
            </td>

            <td class="buttontd">
              <button class="btn btn-warning" v-on:click="showReplaceModal(tag)">
                {{ $t('tags.replace') }}
              </button>
            </td>

            <td class="buttontd">
              <button class="btn btn-warning" v-on:click="showAddModal(tag)">
                {{ $t('common.add') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="control" v-if="tagData.tags && tagData.tags.length > 0">
        <span style="padding-left: 7px">
          <button @click="showDeleteModal()" v-bind:disabled="!tagsSelected">
            {{ $t('common.deleteSelected') }}
          </button>
        </span>
      </div>
    </div>

    <!-- Replace/Add tag modal -->
    <Teleport to="#modal-div">
      <div v-if="updateTagDialog.isRevealed.value" class="vueuse-modal-layout">
        <div class="vueuse-modal">
          <div class="modal-header">
            <h5 class="modal-title" v-html="editModalTitle"></h5>
            <button
              @click="updateTagDialog.cancel"
              type="button"
              class="btn-close ms-auto"
            ></button>
          </div>
          <div class="modal-body">
            <label for="newTag">{{ $t('common.name') }}:</label>
            <input id="newTag" v-model="newTagName" type="text" />
          </div>
          <div class="modal-footer">
            <button @click="tagUpdate()" class="btn btn-primary">
              {{ $t('common.save') }}
            </button>
            <button @click="updateTagDialog.cancel" class="btn btn-secondary">
              {{ $t('common.cancel') }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!--Delete tag modal -->
    <Teleport to="#modal-div">
      <div v-if="confirmDeleteDialog.isRevealed.value" class="vueuse-modal-layout">
        <div class="vueuse-modal">
          <div class="modal-header">
            <h5 class="modal-title">{{ $t('common.confirm') }}</h5>
            <button
              @click="confirmDeleteDialog.cancel"
              type="button"
              class="btn-close ms-auto"
            ></button>
          </div>
          <div class="modal-body">
            <p>{{ $t('tags.confirmDelete') }}</p>
          </div>
          <div class="modal-footer">
            <button @click="deleteSelected()" class="btn btn-danger">
              {{ $t('common.confirm') }}
            </button>
            <button @click="confirmDeleteDialog.cancel" class="btn btn-secondary">
              {{ $t('common.cancel') }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
    <span v-if="!tagData.tags || tagData.tags.length === 0">
      {{ $t('tags.noneFound') }}
    </span>
  </div>
</template>

<script lang="ts">
import type { ErrorObj, WeblogEntryTag, TagUpdateResults, WeblogTagSummaryData } from '@/types'
import * as api from '@/api'
import { AxiosError } from 'axios'
import { useConfirmDialog } from '@vueuse/core'

const confirmDeleteDialogObj = useConfirmDialog()
const updateTagDialogObj = useConfirmDialog()

export default {
  props: {
    weblogId: {
      required: true,
      type: String
    }
  },
  data() {
    return {
      tagData: {} as WeblogTagSummaryData,
      editModalTitle: '' as string,
      editModalAction: null as string | null,
      editModalCurrentTag: null as string | null,
      newTagName: null as string | null,
      pageNum: 0,
      urlRoot: import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/tags/',
      resultsMap: {} as TagUpdateResults,
      successMessage: '' as string | null,
      errorObj: {
        errors: []
      } as ErrorObj,
      isFetching: true
    }
  },
  computed: {
    tagsSelected: function () {
      return this.tagData.tags.filter((tag) => tag.selected).length > 0
    },
    confirmDeleteDialog() {
      return confirmDeleteDialogObj
    },
    updateTagDialog() {
      return updateTagDialogObj
    }
  },
  methods: {
    toggleCheckboxes: function (checkAll: boolean) {
      this.tagData.tags.forEach((tmpl) => {
        tmpl.selected = checkAll
        this.$forceUpdate()
      })
    },
    loadTags: function () {
      api
        .loadTags(this.weblogId, this.pageNum)
        .then((response) => {
          this.tagData = response.data
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    deleteSelected: function () {
      this.messageClear()

      const selectedTagNames = [] as string[]
      this.tagData.tags.forEach((tag) => {
        if (tag.selected) selectedTagNames.push(tag.name)
      })

      api
        .deleteTags(this.weblogId, selectedTagNames)
        .then(() => {
          this.successMessage = selectedTagNames.length + ' tag(s) deleted'
          this.loadTags()
        })
        .catch((error: AxiosError<ErrorObj>) => this.commonErrorResponse(error))
      this.confirmDeleteDialog.cancel()
    },
    showReplaceModal: function (oldTag: WeblogEntryTag) {
      this.showTagUpdateModal('replace', 'tags.replaceTitle', oldTag.name)
    },
    showAddModal: function (currentTag: WeblogEntryTag) {
      this.showTagUpdateModal('add', 'tags.addTitle', currentTag.name)
    },
    showDeleteModal: function () {
      this.messageClear()
      this.confirmDeleteDialog.reveal()
    },
    showTagUpdateModal: function (modalAction: string, modalTitle: string, tagName: string) {
      this.messageClear()
      this.editModalAction = modalAction
      this.editModalCurrentTag = tagName
      this.editModalTitle = this.$t(modalTitle, {
        tagName: this.editModalCurrentTag
      })
      this.newTagName = tagName
      this.updateTagDialog.reveal()
    },
    tagUpdate: function () {
      this.messageClear()
      this.newTagName = this.newTagName?.trim() ?? null
      if (!this.newTagName || this.editModalCurrentTag! === this.newTagName) {
        this.updateTagDialog.cancel()
        return
      }
      if (this.editModalAction === 'replace') {
        this.replaceTag(this.editModalCurrentTag!, this.newTagName!)
      } else if (this.editModalAction === 'add') {
        this.addTag(this.editModalCurrentTag!, this.newTagName!)
      }
      this.updateTagDialog.cancel()
    },
    addTag: function (currentTag: string, newTag: string) {
      this.messageClear()
      api
        .appendTag(this.weblogId, currentTag, newTag)
        .then((response) => {
          this.resultsMap = response.data
          const { updated, unchanged } = this.resultsMap
          this.successMessage =
            `Added [${newTag}] to ${updated} entries having [${currentTag}]` +
            (unchanged > 0 ? ` (${unchanged} already had [${newTag}])` : '')
          this.loadTags()
        })
        .catch((error: AxiosError<ErrorObj>) => this.commonErrorResponse(error))
    },
    replaceTag: function (currentTag: string, newTag: string) {
      this.messageClear()
      api
        .replaceTag(this.weblogId, currentTag, newTag)
        .then((response) => {
          this.resultsMap = response.data
          const { updated, unchanged } = this.resultsMap
          this.successMessage =
            `Replaced [${currentTag}] with [${newTag}] in ${updated} entries` +
            (unchanged > 0
              ? `, deleted [${currentTag}] from ${unchanged} entries already having [${newTag}]`
              : '')
          this.loadTags()
        })
        .catch((error: AxiosError<ErrorObj>) => this.commonErrorResponse(error))
    },
    previousPage: function () {
      this.messageClear()
      this.pageNum--
      this.loadTags()
    },
    nextPage: function () {
      this.messageClear()
      this.pageNum++
      this.loadTags()
    },
    commonErrorResponse: function (error: unknown) {
      if (error instanceof AxiosError && error.response?.status === 401) {
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/login'
      } else if (error instanceof AxiosError && error.response?.data) {
        this.errorObj = error.response.data
      } else {
        this.errorObj = {
          errors: ['An unexpected error occurred. Please try again later.']
        }
      }
      window.scrollTo(0, 0)
    },
    messageClear: function () {
      this.successMessage = ''
      this.errorObj = {
        errors: []
      }
    }
  },
  async created() {
    await this.loadTags()
    this.isFetching = false
  }
}
</script>

<style></style>
