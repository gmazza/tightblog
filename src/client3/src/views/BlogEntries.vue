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
      <AppErrorMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = []"
      ></AppErrorMessageBox>
      <h2>{{ $t('entries.title') }}</h2>
      <p class="pagetip">{{ $t('entries.tip') }}</p>

      <div class="sidebarFade">
        <div class="menu-tr">
          <div class="menu-tl">
            <div class="sidebarInner">
              <h3>{{ $t('entries.sidebarTitle') }}</h3>
              <hr size="1" noshade="noshade" />

              <p>{{ $t('entries.sidebarDescription') }}</p>

              <div>
                <div class="sideformrow">
                  <label for="categoryId" class="sideformrow"> {{ $t('common.category') }}</label>
                  <select id="categoryId" v-model="searchParams.categoryName" size="1" required>
                    <option value="">
                      {{ $t('entries.label.allCategories') }}
                    </option>
                    <option v-for="cat in weblog.weblogCategoryNames" :value="cat" :key="cat">
                      {{ cat }}
                    </option>
                  </select>
                </div>
                <br /><br />

                <div class="sideformrow">
                  <label for="startDateString" class="sideformrow"
                    >{{ $t('entries.label.startDate') }}:</label
                  >
                  <VueDatePicker
                    v-model="searchParams.startDate"
                    :enable-time-picker="false"
                    :clear-button="true"
                    :reset-button="true"
                  />
                </div>

                <div class="sideformrow">
                  <label for="endDateString" class="sideformrow"
                    >{{ $t('entries.label.endDate') }}:</label
                  >
                  <VueDatePicker
                    v-model="searchParams.endDate"
                    :enable-time-picker="false"
                    :clear-button="true"
                    :reset-button="true"
                  />
                </div>
                <br /><br />

                <div class="sideformrow">
                  <label for="status" class="sideformrow">
                    {{ $t('entries.label.status') }}:
                  </label>
                  <div>
                    <select id="status" v-model="searchParams.status" size="1" required>
                      <option
                        v-for="(value, key) in lookupValues?.entryStatusOptions"
                        :value="key"
                        :key="key"
                      >
                        {{ $t(value) }}
                      </option>
                    </select>
                  </div>
                </div>

                <div class="sideformrow">
                  <label for="status" class="sideformrow">
                    {{ $t('entries.label.sortBy') }}: <br /><br />
                  </label>
                  <div>
                    <div v-for="(value, key) in lookupValues?.dateSortByOptions" :key="key">
                      <input
                        type="radio"
                        name="sortBy"
                        v-model="searchParams.sortBy"
                        v-bind:value="key"
                      />{{ $t(value) }}<br />
                    </div>
                  </div>
                </div>
                <br />
                <button type="button" v-on:click="loadEntries()">
                  {{ $t('entries.buttonFilter') }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 
      =============================================================
      Number of entries and date message, next/prev links
      ============================================================= 
    -->

    <div class="tablenav" v-cloak>
      <div style="float: left">
        {{ entriesData.entries.length }}
        {{ $t('entries.entriesReturned') }}
      </div>
      <span v-if="entriesData.entries.length > 0">
        <div style="float: right">
          <span v-if="entriesData.entries[0].pubTime != null">
            {{ formatDateTime(entriesData.entries[0].pubTime) }}
          </span>
          ---
          <span v-if="entriesData.entries[entriesData.entries.length - 1].pubTime != null">
            {{ formatDateTime(entriesData.entries[entriesData.entries.length - 1].pubTime) }}
          </span>
        </div>
      </span>
      <br /><br />

      <span v-if="pageNum > 0 || entriesData.hasMore" v-cloak class="centered-buttons">
        &laquo;
        <button type="button" v-bind:disabled="pageNum <= 0" v-on:click="previousPage()">
          {{ $t('common.previousPage') }}
        </button>
        |
        <button type="button" v-bind:disabled="!entriesData.hasMore" v-on:click="nextPage()">
          {{ $t('common.nextPage') }}
        </button>
        &raquo;
      </span>

      <br />
    </div>

    <!-- 
      =============================================================
        Entry table
      ============================================================= 
    -->

    <p>
      <span class="draftEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
      {{ $t('entries.draft') }}&nbsp;&nbsp;
      <span class="scheduledEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
      {{ $t('entries.scheduled') }}&nbsp;&nbsp;
    </p>

    <table class="table table-sm table-bordered table-hover" width="100%">
      <thead class="thead-light">
        <tr>
          <th width="15%">{{ $t('entries.pubTime') }}</th>
          <th width="15%">
            {{ $t('entries.updateTime') }}
          </th>
          <th width="8%">{{ $t('common.category') }}</th>
          <th>{{ $t('entries.entryTitle') }}</th>
          <th width="16%">{{ $t('common.tags') }}</th>
          <th width="5%"></th>
          <th width="5%"></th>
          <th width="5%"></th>
        </tr>
      </thead>

      <tbody>
        <tr
          v-for="entry in entriesData.entries"
          :class="entryStatusClass(entry.status)"
          :key="entry.id"
          v-cloak
        >
          <td>
            <span v-if="entry.pubTime != null">
              {{ formatDateTime(entry.pubTime) }}
            </span>
          </td>

          <td>
            <span v-if="entry.updateTime != null">
              {{ formatDateTime(entry.updateTime) }}
            </span>
          </td>

          <td>
            {{ entry.category.name }}
          </td>

          <td>
            {{ entry.title.substr(0, 80) }}
          </td>

          <td>
            {{ entry.tagsAsString }}
          </td>

          <td>
            <span v-if="entry.status == 'PUBLISHED'">
              <a v-bind:href="entry.permalink" target="_blank">{{ $t('entries.view') }}</a>
            </span>
          </td>

          <td>
            <router-link
              :to="{
                name: 'entryEdit',
                params: {
                  weblogId: weblogId
                },
                query: {
                  entryId: entry.id
                }
              }"
              >{{ $t('common.edit') }}</router-link
            >
          </td>

          <td class="buttontd">
            <button class="btn btn-danger" v-on:click="confirmDelete(entry)">
              {{ $t('common.delete') }}
            </button>
          </td>
        </tr>

        <Teleport to="#modal-div">
          <div v-if="confirmDeleteDialog.isRevealed.value" class="vueuse-modal-layout">
            <div class="vueuse-modal">
              <div class="modal-header">
                <h5
                  class="modal-title"
                  v-html="
                    $t('entries.confirmDeleteTemplate', {
                      title: entryToDelete?.title
                    })
                  "
                ></h5>
                <button
                  @click="confirmDeleteDialog.cancel"
                  type="button"
                  class="btn-close"
                  aria-label="Close"
                ></button>
              </div>
              <div class="modal-footer">
                <button @click="deleteWeblogEntry(entryToDelete!!)">
                  {{ $t('common.confirm') }}
                </button>
                <button @click="confirmDeleteDialog.cancel">{{ $t('common.cancel') }}</button>
              </div>
            </div>
          </div>
        </Teleport>
      </tbody>
    </table>

    <span v-if="entriesData.entries.length == 0">
      {{ $t('entries.noneFound') }}
    </span>
  </div>
</template>

<script lang="ts">
import type {
  ErrorObj,
  PublishStatus,
  Weblog,
  WeblogEntryQueryParams,
  WeblogEntry,
  WeblogEntriesData
} from '@/types'
import { mapState, mapActions } from 'pinia'
import { AxiosError } from 'axios'
import { useSessionInfoStore } from '../stores/sessionInfo'
import { useStartupConfigStore } from '../stores/startupConfig'
import { formatDateTime } from '../helpers'
import { useConfirmDialog } from '@vueuse/core'
import VueDatePicker from '@vuepic/vue-datepicker'
import '@vuepic/vue-datepicker/dist/main.css'
import api from '@/api'

const confirmDeleteDialogObj = useConfirmDialog()

export default {
  components: {
    VueDatePicker
  },
  props: {
    weblogId: {
      required: true,
      type: String
    }
  },
  data() {
    return {
      weblog: {} as Weblog,
      searchParams: {} as WeblogEntryQueryParams,
      entriesData: {
        entries: [] as WeblogEntry[],
        hasMore: false
      } as WeblogEntriesData,
      errorObj: {
        errors: []
      } as ErrorObj,
      pageNum: 0,
      urlRoot: import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblogentries/',
      entryToDelete: null as WeblogEntry | null,
      isFetching: true
    }
  },
  computed: {
    ...mapState(useStartupConfigStore, ['startupConfig', 'lookupValues']),
    confirmDeleteDialog: function () {
      return confirmDeleteDialogObj
    }
  },
  methods: {
    ...mapActions(useStartupConfigStore, ['loadLookupValues']),
    ...mapActions(useSessionInfoStore, ['fetchWeblog']),
    formatDateTime(date: Date) {
      return formatDateTime(date)
    },
    async loadEntries() {
      const queryParams: WeblogEntryQueryParams = { ...this.searchParams }

      if (queryParams.startDate != undefined) {
        queryParams.startDate.setHours(0, 0, 0, 0)
      }

      if (queryParams.endDate != undefined) {
        queryParams.endDate.setHours(23, 59, 59, 999)
      }

      await api
        .loadEntries(this.weblogId, this.pageNum, queryParams)
        .then((response) => {
          this.entriesData.entries = response.entries
          this.entriesData.hasMore = response.hasMore
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    entryStatusClass: function (status: PublishStatus) {
      if (status === 'DRAFT') {
        return 'draftEntryBox'
      } else if (status === 'SCHEDULED') {
        return 'scheduledEntryBox'
      } else {
        return null
      }
    },
    confirmDelete(entry: WeblogEntry) {
      this.entryToDelete = entry
      this.confirmDeleteDialog.reveal()
    },
    deleteWeblogEntry: function (entry: WeblogEntry) {
      api
        .deleteWeblogEntry(entry.id)
        .then(() => {
          this.confirmDeleteDialog.cancel()
          this.loadEntries()
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    previousPage: function () {
      this.pageNum--
      this.loadEntries()
    },
    nextPage: function () {
      this.pageNum++
      this.loadEntries()
    },
    commonErrorResponse: function (error: unknown) {
      if (error instanceof AxiosError && error.response?.status === 401) {
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/login'
      } else {
        if (error instanceof AxiosError) {
          this.errorObj = error.response?.data
        } else {
          this.errorObj = { errors: ['An unknown error occurred'] }
        }
        window.scrollTo(0, 0)
      }
    }
  },
  async created() {
    await this.loadLookupValues()
    await this.fetchWeblog(this.weblogId).then((fetchedWeblog) => {
      if (fetchedWeblog == null) {
        this.errorObj = { errors: ['Weblog not found'] }
        return
      }
      this.weblog = { ...fetchedWeblog }
    })
    await this.loadEntries()
    this.isFetching = false
  }
}
</script>

<style scoped>
.centered-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
