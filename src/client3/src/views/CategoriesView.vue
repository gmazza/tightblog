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
      <AppErrorListMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = []"
      ></AppErrorListMessageBox>
      <h2>{{ $t('categories.title') }}</h2>
      <p class="pagetip">{{ $t('categories.rootPrompt') }}</p>

      <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
          <tr>
            <th width="20%">{{ $t('common.category') }}</th>
            <th width="20%">{{ $t('common.column.numEntries') }}</th>
            <th width="20%">{{ $t('common.column.firstEntry') }}</th>
            <th width="20%">{{ $t('common.column.lastEntry') }}</th>
            <th width="10%">{{ $t('common.rename') }}</th>
            <th width="10%">{{ $t('common.delete') }}</th>
          </tr>
        </thead>
        <tbody v-cloak>
          <tr v-for="item in orderedItems" :key="item.id">
            <td>{{ item.name }}</td>
            <td>{{ item.numEntries }}</td>
            <td>{{ item.firstEntry }}</td>
            <td>{{ item.lastEntry }}</td>
            <td class="buttontd">
              <button class="btn btn-warning" v-on:click="showUpsertModal(item)">
                {{ $t('common.rename') }}
              </button>
            </td>
            <td class="buttontd">
              <span v-if="items.length > 1">
                <button class="btn btn-danger" @click="confirmDelete(item)">
                  {{ $t('common.delete') }}
                </button>
              </span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="control clearfix">
        <button type="button" v-on:click="showUpsertModal(null)">
          {{ $t('categories.addCategory') }}
        </button>
      </div>

      <!-- Add/Edit Category modal -->
      <Teleport to="#modal-div">
        <div v-if="upsertCategoryDialog.isRevealed.value" class="vueuse-modal-layout">
          <div class="vueuse-modal">
            <div class="modal-header">
              <h5 class="modal-title">{{ upsertModalTitle }}</h5>
              <button
                @click="upsertCategoryDialog.cancel"
                type="button"
                class="btn-close ms-auto"
                aria-label="Close"
              ></button>
            </div>
            <div class="modal-body">
              <span v-if="showUpdateErrorMessage">
                {{ $t('categories.errorDuplicateName') }}<br />
              </span>
              <label for="category-name">{{ $t('common.name') }}:</label>
              <input id="category-name" v-model="itemToEdit!.name" maxlength="80" size="40" />
            </div>
            <div class="modal-footer">
              <button @click="upsertItem()">{{ $t('common.confirm') }}</button>
              <button @click="upsertCategoryDialog.cancel">
                {{ $t('common.cancel') }}
              </button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- Delete category modal -->
      <Teleport to="#modal-div">
        <div v-if="confirmDeleteDialog.isRevealed.value" class="vueuse-modal-layout">
          <div class="vueuse-modal">
            <div class="modal-header">
              <h5
                v-html="
                  $t('categories.deleteCategory', {
                    categoryName: categoryToDelete!.name
                  })
                "
              ></h5>
              <button
                @click="confirmDeleteDialog.cancel"
                type="button"
                class="btn-close ms-auto"
                aria-label="Close"
              ></button>
            </div>
            <div class="modal-body">
              <p>
                {{ $t('categories.deleteMoveToWhere') }}
                <select v-model="targetCategoryId" size="1" required>
                  <option v-for="item in moveToCategories" :key="item.id" :value="item.id">
                    {{ item.name }}
                  </option>
                </select>
              </p>
            </div>
            <div class="modal-footer">
              <button @click="deleteItem()">{{ $t('common.delete') }}</button>
              <button @click="confirmDeleteDialog.cancel">
                {{ $t('common.cancel') }}
              </button>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </div>
</template>

<script lang="ts">
import type { ErrorItem, ErrorObj, WeblogCategory } from '@/types'
import * as api from '@/api'
import { AxiosError } from 'axios'
import { useConfirmDialog } from '@vueuse/core'

const confirmDeleteDialogObj = useConfirmDialog()
const upsertCategoryDialogObj = useConfirmDialog()

export default {
  props: {
    weblogId: {
      required: true,
      type: String
    }
  },
  data() {
    return {
      items: [] as WeblogCategory[],
      itemToEdit: {} as null | WeblogCategory,
      errorObj: {
        errors: []
      } as ErrorObj,
      upsertModalTitle: '',
      showUpdateErrorMessage: false,
      categoryToDelete: null as null | WeblogCategory,
      targetCategoryId: null,
      isFetching: true
    }
  },
  computed: {
    orderedItems: function () {
      return this.items.concat().sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
    },
    moveToCategories: function () {
      // adding extra var to trigger cache refresh
      const compareVal = this.categoryToDelete ? this.categoryToDelete.id : null
      return this.items.filter(function (item) {
        return item.id !== compareVal
      })
    },
    confirmDeleteDialog() {
      return confirmDeleteDialogObj
    },
    upsertCategoryDialog() {
      return upsertCategoryDialogObj
    }
  },
  methods: {
    loadItems: function () {
      api
        .loadCategories(this.weblogId)
        .then((response) => {
          this.items = response.data
        })
        .catch((error: AxiosError<ErrorObj>) => {
          this.commonErrorResponse(error)
        })
    },
    showUpsertModal: function (item: WeblogCategory | null) {
      this.upsertModalTitle = this.$t(
        item == null ? 'categories.addCategory' : 'categories.updateCategory'
      )
      this.itemToEdit = item ? { id: item.id, name: item.name } : ({ name: '' } as WeblogCategory)
      this.upsertCategoryDialog.reveal()
    },
    confirmDelete(category: WeblogCategory) {
      this.categoryToDelete = category
      this.confirmDeleteDialog.reveal()
    },
    upsertItem: function () {
      this.messageClear()
      const item = this.itemToEdit
      if (!item || !item.name) return

      // Clean the name
      item.name = item.name.replace(/[,%"/]/g, '')
      if (!item.name) return

      const apiCall = item.id
        ? api.updateCategory(this.weblogId, item)
        : api.insertCategory(this.weblogId, item)

      this.upsertCategoryDialog.cancel()

      apiCall
        .then(() => {
          this.itemToEdit = null
          this.loadItems()
        })
        .catch((error: AxiosError<ErrorObj>) => this.commonErrorResponse(error))
    },
    deleteItem: function () {
      if (!this.targetCategoryId) return
      this.messageClear()
      this.confirmDeleteDialog.cancel()
      api
        .deleteCategory(this.weblogId, this.categoryToDelete!.id!, this.targetCategoryId)
        .then(() => {
          this.targetCategoryId = null
          this.loadItems()
        })
        .catch((error: AxiosError<ErrorObj>) => this.commonErrorResponse(error))
    },
    commonErrorResponse: function (error: unknown) {
      if (error instanceof AxiosError && error.response?.status === 401) {
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/login'
      } else if (error instanceof AxiosError && error.response?.status === 409) {
        this.showUpdateErrorMessage = true
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
      this.showUpdateErrorMessage = false
      this.errorObj = {
        errors: []
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
