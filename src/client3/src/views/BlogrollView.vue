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
      <div>
        <AppErrorListMessageBox
          :in-error-obj="errorObj"
          @close-box="errorObj.errors = []"
        ></AppErrorListMessageBox>
      </div>
      <h2>{{ $t('blogroll.title') }}</h2>
      <p class="pagetip">{{ $t('blogroll.rootPrompt') }}</p>

      <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
          <tr>
            <th width="5%">
              <input
                type="checkbox"
                :disabled="items.length == 0"
                @input="toggleCheckboxes(($event.target as HTMLInputElement).checked)"
                :title="$t('blogroll.selectAllLabel')"
              />
            </th>
            <th width="25%">
              {{ $t('blogroll.linkLabel') }}
            </th>
            <th width="25%">
              {{ $t('blogroll.urlHeader') }}
            </th>
            <th width="35%">
              {{ $t('common.description') }}
            </th>
            <th width="10%">{{ $t('common.edit') }}</th>
          </tr>
        </thead>
        <tbody id="tableBody" v-cloak>
          <tr v-for="item in orderedItems" :key="item.id">
            <td class="center" style="vertical-align: middle">
              <input
                type="checkbox"
                name="idSelections"
                :title="$t('blogroll.checkboxTitle', { itemName: item.name })"
                v-model="item.selected"
                :value="item.id"
              />
            </td>
            <td>{{ item.name }}</td>
            <td>
              <a target="_blank" :href="item.url">{{ item.url }}</a>
            </td>
            <td>{{ item.description }}</td>
            <td class="buttontd">
              <button class="btn btn-warning" v-on:click="showUpsertModal(item)">
                {{ $t('common.edit') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="control clearfix">
        <button type="button" v-on:click="showUpsertModal(null)">
          {{ $t('blogroll.addLink') }}
        </button>

        <span v-if="items.length > 0">
          <button @click="showDeleteModal()" v-bind:disabled="!itemsSelected">
            {{ $t('common.deleteSelected') }}
          </button>
        </span>
      </div>
    </div>
  </div>
  <!-- Upsert bookmark modal -->
  <Teleport to="#modal-div">
    <div v-if="upsertDialog.isRevealed.value" class="vueuse-modal-layout">
      <div class="vueuse-modal">
        <div class="modal-header">
          <h5 class="modal-title">{{ upsertModalTitle }}</h5>
          <button
            @click="upsertDialog.cancel"
            type="button"
            class="btn-close ms-auto"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <p class="pagetip">
            {{ $t('blogroll.requiredFields') }}
          </p>
          <form>
            <div class="form-group">
              <label for="name" class="col-form-label">{{ $t('blogroll.linkLabel') }}</label>
              <input type="text" class="form-control" v-model="itemToEdit!.name" maxlength="80" />
            </div>
            <div class="form-group">
              <label for="url" class="col-form-label">{{ $t('common.url') }}</label>
              <input type="text" class="form-control" v-model="itemToEdit!.url" maxlength="128" />
            </div>
            <div class="form-group">
              <label for="description" class="col-form-label">{{ $t('common.description') }}</label>
              <input
                type="text"
                class="form-control"
                v-model="itemToEdit!.description"
                maxlength="128"
              />
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button @click="updateItem()">{{ $t('common.save') }}</button>
          <button @click="upsertDialog.cancel">{{ $t('common.cancel') }}</button>
        </div>
      </div>
    </div>
  </Teleport>
  <!-- Delete bookmark modal -->
  <Teleport to="#modal-div">
    <div v-if="confirmDeleteDialog.isRevealed.value" class="vueuse-modal-layout">
      <div class="vueuse-modal">
        <div class="modal-header">
          <h5 v-html="$t('blogroll.deleteWarning')"></h5>
          <button
            @click="confirmDeleteDialog.cancel"
            type="button"
            class="btn-close ms-auto"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-footer">
          <button @click="deleteSelected()">{{ $t('common.confirm') }}</button>
          <button @click="confirmDeleteDialog.cancel">
            {{ $t('common.cancel') }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script lang="ts">
import type { ErrorItem, ErrorObj, WeblogBookmark } from '@/types'
import * as api from '@/api'
import { AxiosError } from 'axios'
import { useConfirmDialog } from '@vueuse/core'

const confirmDeleteDialogObj = useConfirmDialog()
const upsertDialogObj = useConfirmDialog()

export default {
  props: {
    weblogId: {
      required: true,
      type: String
    }
  },
  data() {
    return {
      items: [] as WeblogBookmark[],
      itemToEdit: {} as null | WeblogBookmark,
      upsertModalTitle: '',
      errorObj: {
        errors: []
      } as ErrorObj,
      isFetching: true
    }
  },
  computed: {
    orderedItems: function () {
      return this.items.concat().sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
    },
    itemsSelected: function () {
      return this.items.filter((item) => item.selected).length > 0
    },
    confirmDeleteDialog() {
      return confirmDeleteDialogObj
    },
    upsertDialog() {
      return upsertDialogObj
    }
  },
  methods: {
    toggleCheckboxes: function (checkAll: boolean) {
      this.items.forEach((item) => {
        item.selected = checkAll
        this.$forceUpdate()
      })
    },
    loadItems: function () {
      api
        .loadBookmarks(this.weblogId)
        .then((response) => {
          this.items = response.data
        })
        .catch((error: AxiosError<ErrorObj>) => {
          this.commonErrorResponse(error)
        })
    },
    showDeleteModal: function () {
      this.messageClear()
      this.confirmDeleteDialog.reveal()
    },
    deleteSelected: function () {
      this.messageClear()
      const selectedLinkIds = [] as string[]

      this.items.forEach((item) => {
        if (item.selected) selectedLinkIds.push(item.id!)
      })

      api
        .deleteBookmarks(selectedLinkIds)
        .then(() => {
          this.loadItems()
        })
        .catch((error: AxiosError<ErrorObj>) => {
          this.commonErrorResponse(error)
        })
      this.confirmDeleteDialog.cancel()
    },
    showUpsertModal: function (item: WeblogBookmark | null) {
      this.upsertModalTitle = this.$t(item == null ? 'blogroll.addLink' : 'blogroll.editLink')
      this.messageClear()
      this.itemToEdit = {
        id: undefined,
        name: '',
        description: '',
        url: '',
        position: undefined,
        selected: false
      }
      if (item !== null) {
        Object.assign(this.itemToEdit, item)
      }
      this.upsertDialog.reveal()
    },
    updateItem: function () {
      this.messageClear()
      if (!this.itemToEdit || !this.itemToEdit.name || !this.itemToEdit.url) return

      const apiCall = this.itemToEdit.id
        ? api.updateBookmark(this.itemToEdit)
        : api.insertBookmark(this.weblogId, this.itemToEdit)

      apiCall
        .then(() => {
          this.itemToEdit = null
          this.loadItems()
        })
        .catch((error: AxiosError<ErrorObj>) => this.commonErrorResponse(error))
      this.upsertDialog.cancel()
    },
    messageClear: function () {
      this.errorObj = {
        errors: []
      }
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
    }
  },
  async created() {
    await this.loadItems()
    this.isFetching = false
  }
}
</script>

<style></style>
