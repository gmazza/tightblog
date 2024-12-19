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
    <div id="sidebar_maincontent_wrap" style="text-align: left; padding: 20px">
      <AppErrorListMessageBox
        v-bind:inErrorObj="errorObj"
        @close-box="errorObj.errors = []"
      ></AppErrorListMessageBox>
      <h2>
        {{ entryId ? $t('entryEdit.editTitle') : $t('entryEdit.addTitle') }}
      </h2>

      <table class="entryEditTable" cellpadding="0" cellspacing="0" style="width: 100%">
        <tr>
          <td class="entryEditFormLabel">
            <label for="title">{{ $t('entryEdit.entryTitle') }}</label>
          </td>
          <td>
            <input
              id="title"
              type="text"
              v-model="entry.title"
              maxlength="255"
              style="width: 60%"
              autocomplete="off"
            />
          </td>
        </tr>

        <tr>
          <td class="entryEditFormLabel">
            {{ $t('entryEdit.status') }}
          </td>
          <td v-cloak>
            <span v-show="entry.status == 'PUBLISHED'" style="color: green; font-weight: bold">
              {{ $t('entryEdit.published') }} ({{ $t('entryEdit.updateTime') }}
              {{ formatDateTime(entry.updateTime) }})
            </span>
            <span v-show="entry.status == 'DRAFT'" style="color: orange; font-weight: bold">
              {{ $t('entryEdit.draft') }} ({{ $t('entryEdit.updateTime') }}
              {{ formatDateTime(entry.updateTime) }})
            </span>
            <span v-show="entry.status == 'SCHEDULED'" style="color: orange; font-weight: bold">
              {{ $t('entryEdit.scheduled') }} ({{ $t('entryEdit.updateTime') }}
              {{ formatDateTime(entry.updateTime) }})
            </span>
            <span v-show="!entry.status" style="color: red; font-weight: bold">
              {{ $t('entryEdit.unsaved') }}
            </span>
          </td>
        </tr>

        <tr v-show="entry.id" v-cloak>
          <td class="entryEditFormLabel">
            <label for="permalink">{{ $t('entryEdit.permalink') }}</label>
          </td>
          <td>
            <span v-show="entry.status == 'PUBLISHED'">
              <a id="permalink" v-bind:href="entry.permalink" target="_blank">{{
                entry.permalink
              }}</a>
              <img src="@/assets/launch-link.png" />
            </span>
            <span v-show="entry.status != 'PUBLISHED'">
              {{ entry.permalink }}
            </span>
          </td>
        </tr>

        <tr>
          <td class="entryEditFormLabel">
            <label for="categoryId">{{ $t('common.category') }}</label>
          </td>
          <td v-show="entry" v-cloak>
            <select id="categoryId" v-model="entry.category.name" size="1" required>
              <option v-for="cat in weblog.weblogCategoryNames" :value="cat" :key="cat">
                {{ cat }}
              </option>
            </select>
          </td>
        </tr>

        <tr>
          <td class="entryEditFormLabel">
            <label for="tags">{{ $t('common.tags') }}</label>
          </td>
          <td>
            <input id="tags" v-model="entry.tags" />
          </td>
        </tr>

        <tr v-cloak>
          <td class="entryEditFormLabel">
            <label for="title">{{ $t('entryEdit.editFormat') }}</label>
          </td>
          <td v-cloak>
            <select v-model="entry.editFormat" size="1" required>
              <option v-for="(value, key) in lookupValues.editFormats" :value="key" :key="key">
                {{ $t(value) }}
              </option>
            </select>
          </td>
        </tr>
      </table>

      <!-- Weblog editor -->

      <p class="toplabel"></p>

      <div class="accordion" id="accordionExample">
        <div class="accordion-item">
          <h2 class="accordion-header" id="headingOne">
            <button
              class="accordion-button"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#collapseOne"
              aria-expanded="true"
              aria-controls="collapseOne"
            >
              {{ $t('entryEdit.content') }}
            </button>
          </h2>
          <div
            id="collapseOne"
            class="accordion-collapse collapse show"
            aria-labelledby="headingOne"
            data-bs-parent="#accordionExample"
          >
            <div class="accordion-body">
              <textarea
                id="edit_content"
                cols="75"
                rows="25"
                style="width: 100%"
                v-model="entry.text"
              ></textarea>
            </div>
          </div>
        </div>
        <div class="accordion-item">
          <h2 class="accordion-header" id="headingTwo">
            <button
              class="accordion-button collapsed"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#collapseTwo"
              aria-expanded="false"
              aria-controls="collapseTwo"
            >
              {{ $t('entryEdit.summary') }}
              <AppHelpPopup :helpText="$t('entryEdit.tooltip.summary')" />
            </button>
          </h2>
          <div
            id="collapseTwo"
            class="accordion-collapse collapse"
            aria-labelledby="headingTwo"
            data-bs-parent="#accordionExample"
          >
            <div class="accordion-body">
              <textarea
                id="edit_summary"
                cols="75"
                rows="10"
                style="width: 100%"
                v-model="entry.summary"
              ></textarea>
            </div>
          </div>
        </div>
        <div class="accordion-item">
          <h2 class="accordion-header" id="headingThree">
            <button
              class="accordion-button collapsed"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#collapseThree"
              aria-expanded="false"
              aria-controls="collapseThree"
            >
              {{ $t('entryEdit.notes') }}
              <AppHelpPopup :helpText="$t('entryEdit.tooltip.notes')" />
            </button>
          </h2>
          <div
            id="collapseThree"
            class="accordion-collapse collapse"
            aria-labelledby="headingThree"
            data-bs-parent="#accordionExample"
          >
            <div class="accordion-body">
              <textarea
                id="edit_notes"
                cols="75"
                rows="10"
                style="width: 100%"
                v-model="entry.notes"
              ></textarea>
            </div>
          </div>
        </div>
      </div>

      <!-- advanced settings  -->

      <div class="controlToggle">
        {{ $t('entryEdit.miscSettings') }}
      </div>

      <label for="link">{{ $t('entryEdit.specifyPubTime') }}:</label>
      <div>
        <VueDatePicker
          v-model="entry.pubTime"
          :enable-time-picker="true"
          :format="formatDateToMinute"
          :clear-button="true"
          :reset-button="true"
        />
        {{ weblog.timeZone }}
      </div>
      <br />

      <span v-show="commentingActive">
        {{ $t('entryEdit.allowComments') }} {{ $t('entryEdit.commentDays') }}
        <select id="commentDaysId" v-model="entry.commentDays" size="1" required>
          <option v-for="(value, key) in lookupValues.commentDayOptions" :value="key" :key="key">
            {{ $t(value) }}
          </option>
        </select>
        <br />
      </span>

      <br />

      <table>
        <tr>
          <td>
            {{ $t('entryEdit.searchDescription') }}:<AppHelpPopup
              :helpText="$t('entryEdit.tooltip.searchDescription')"
            />
          </td>
          <td style="width: 75%">
            <input
              type="text"
              style="width: 100%"
              maxlength="255"
              v-model="entry.searchDescription"
            />
          </td>
        </tr>
        <tr>
          <td>
            {{ $t('entryEdit.enclosureURL') }}:<AppHelpPopup
              :helpText="$t('entryEdit.tooltip.enclosureURL')"
            />
          </td>
          <td>
            <input type="text" style="width: 100%" maxlength="255" v-model="entry.enclosureUrl" />
          </td>
        </tr>
        <tr v-show="entryId">
          <td></td>
          <td>
            <span v-show="entry.enclosureType">
              {{ $t('entryEdit.enclosureType') }}:
              {{ entry.enclosureType }}
            </span>
            <span v-show="entry.enclosureLength">
              {{ $t('entryEdit.enclosureLength') }}:
              {{ entry.enclosureLength }}
            </span>
          </td>
        </tr>
      </table>

      <!-- the button box -->

      <br />
      <div class="control">
        <span style="padding-left: 7px">
          <button type="button" v-on:click="saveEntry('DRAFT')">
            {{ $t('entryEdit.save') }}
          </button>
          <span v-show="entry.id">
            <button type="button" v-on:click="previewEntry()">
              {{ $t('entryEdit.fullPreviewMode') }}
            </button>
          </span>
          <span>
            <button type="button" v-on:click="saveEntry('PUBLISHED')">
              {{ $t('entryEdit.post') }}
            </button>
          </span>
        </span>

        <span style="float: right" v-show="entryId">
          <button type="button" v-on:click="confirmDeleteDialog.reveal">
            {{ $t('entryEdit.deleteEntry') }}
          </button>
        </span>
      </div>
    </div>

    <div id="sidebar_rightcontent_wrap">
      <div id="rightcontent">
        <div class="sidebarFade">
          <div class="menu-tr">
            <div class="menu-tl">
              <div class="sidebarInner" v-cloak>
                <h3>{{ $t('entryEdit.comments') }}</h3>

                <div v-show="entry.commentCountIncludingUnapproved > 0">
                  <!--router-link
                    :to="{
                      name: 'comments',
                      params: {
                        weblogId: weblogId
                      },
                      query: {
                        entryId: entryId
                      }
                    }"
                    >{{ commentCountMsg }}</router-link
                --></div>
                <div v-show="entry.commentCountIncludingUnapproved == 0">
                  {{ $t('common.none') }}
                </div>

                <div v-show="recentEntries.DRAFT && recentEntries.DRAFT.length > 0">
                  <hr size="1" noshade="noshade" />
                  <h3>
                    {{ $t('entryEdit.draftEntries') }}
                  </h3>

                  <span v-for="post in recentEntries.DRAFT" :key="post.id">
                    <span class="entryEditSidebarLink">
                      <img
                        src="@/assets/table_edit.png"
                        class="edit-icon"
                        alt="icon"
                        title="Edit"
                      />
                      <router-link
                        :to="{
                          name: 'entryEdit',
                          params: {
                            weblogId: weblogId
                          },
                          query: {
                            entryId: post.id
                          }
                        }"
                        >{{ post.title }}</router-link
                      >
                    </span>
                    <br />
                  </span>
                </div>

                <div v-show="recentEntries.PUBLISHED && recentEntries.PUBLISHED.length > 0">
                  <hr size="1" noshade="noshade" />
                  <h3>
                    {{ $t('entryEdit.publishedEntries') }}
                  </h3>

                  <span v-for="post in recentEntries.PUBLISHED" :key="post.id">
                    <span class="entryEditSidebarLink">
                      <img
                        src="@/assets/table_edit.png"
                        class="edit-icon"
                        alt="icon"
                        title="Edit"
                      />
                      <router-link
                        :to="{
                          name: 'entryEdit',
                          params: {
                            weblogId: weblogId
                          },
                          query: {
                            entryId: post.id
                          }
                        }"
                        >{{ post.title }}</router-link
                      >
                    </span>
                    <br />
                  </span>
                </div>

                <div v-show="recentEntries.SCHEDULED && recentEntries.SCHEDULED.length > 0">
                  <hr size="1" noshade="noshade" />
                  <h3>
                    {{ $t('entryEdit.scheduledEntries') }}
                  </h3>

                  <span v-for="post in recentEntries.SCHEDULED" :key="post.id">
                    <span class="entryEditSidebarLink">
                      <img
                        src="@/assets/table_edit.png"
                        class="edit-icon"
                        alt="icon"
                        title="Edit"
                      />
                      <router-link
                        :to="{
                          name: 'entryEdit',
                          params: {
                            weblogId: weblogId
                          },
                          query: {
                            entryId: post.id
                          }
                        }"
                        >{{ post.title }}</router-link
                      >
                    </span>
                    <br />
                  </span>
                </div>
                <br />
                <br />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <Teleport to="#modal-div">
      <div v-if="confirmDeleteDialog.isRevealed.value" class="vueuse-modal-layout">
        <div class="vueuse-modal">
          <div class="modal-header">
            <h5
              class="modal-title"
              v-html="
                $t('entryEdit.confirmDeleteTmpl', {
                  title: entry.title
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
            <button @click="deleteWeblogEntry()">{{ $t('common.confirm') }}</button>
            <button @click="confirmDeleteDialog.cancel">{{ $t('common.cancel') }}</button>
          </div>
        </div>
      </div>
    </Teleport>
    <Teleport to="#modal-div">
      <div v-if="confirmLeaveNoSaveDialog.isRevealed.value" class="vueuse-modal-layout">
        <div class="vueuse-modal">
          <div class="modal-header">
            <h5 class="modal-title" v-html="$t('common.confirmLeaveNoSave')"></h5>
            <button
              @click="confirmLeaveNoSaveDialog.cancel"
              type="button"
              class="btn-close"
              aria-label="Close"
            ></button>
          </div>
          <div class="modal-footer">
            <button @click="confirmLeaveNoSaveDialog.confirm">{{ $t('common.confirm') }}</button>
            <button @click="confirmLeaveNoSaveDialog.cancel">{{ $t('common.cancel') }}</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script lang="ts">
import type {
  PublishStatus,
  RecentWeblogEntry,
  Weblog,
  WeblogEntry,
  ErrorObj
} from '@/types/interfaces'
import { createDefaultWeblogEntry } from '../helpers/factories'
import { useSessionInfoStore } from '../stores/sessionInfo'
import { useStartupConfigStore } from '../stores/startupConfig'
import { useDynamicConfigStore } from '../stores/dynamicConfig'
import type { RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { AxiosError } from 'axios'
import { mapState, mapActions } from 'pinia'
import { formatDateToMinute, formatDateTime } from '../helpers'
import api from '@/api'
import { useConfirmDialog } from '@vueuse/core'
import VueDatePicker from '@vuepic/vue-datepicker'
import '@vuepic/vue-datepicker/dist/main.css'

const confirmLeaveNoSaveDialogObj = useConfirmDialog()
const confirmDeleteDialogObj = useConfirmDialog()

interface RecentEntries {
  SCHEDULED: RecentWeblogEntry[]
  DRAFT: RecentWeblogEntry[]
  PUBLISHED: RecentWeblogEntry[]
}

export default {
  components: {
    VueDatePicker
  },
  props: {
    weblogId: {
      required: true,
      type: String
    },
    entryId: {
      required: false,
      type: String
    }
  },
  data() {
    return {
      weblog: {} as Weblog,
      entry: {
        category: {}
      } as WeblogEntry,
      originalEntry: {} as WeblogEntry,
      errorObj: {
        errors: []
      } as ErrorObj,
      commentCountMsg: '',
      recentEntries: {
        SCHEDULED: {},
        DRAFT: {},
        PUBLISHED: {}
      } as RecentEntries,
      isFetching: true
    }
  },
  computed: {
    ...mapState(useStartupConfigStore, ['lookupValues']),
    ...mapState(useDynamicConfigStore, ['webloggerProperties']),
    commentingActive: function () {
      return (
        this.webloggerProperties.commentPolicy !== 'NONE' && this.weblog.allowComments !== 'NONE'
      )
    },
    formIsDirty: function () {
      return (
        this.originalEntry.title !== this.entry.title ||
        this.originalEntry.editFormat !== this.entry.editFormat ||
        this.originalEntry.text !== this.entry.text ||
        this.originalEntry.summary !== this.entry.summary ||
        this.originalEntry.notes !== this.entry.notes ||
        this.originalEntry.tags !== this.entry.tags ||
        this.originalEntry.pubTime !== this.entry.pubTime ||
        this.originalEntry.commentDays !== this.entry.commentDays ||
        this.originalEntry.searchDescription !== this.entry.searchDescription ||
        this.originalEntry.enclosureUrl !== this.entry.enclosureUrl
      )
    },
    confirmLeaveNoSaveDialog: function () {
      return confirmLeaveNoSaveDialogObj
    },
    confirmDeleteDialog: function () {
      return confirmDeleteDialogObj
    }
  },
  watch: {
    // same-page swaps of the entry (say from most recent entries in sidebar)
    // don't cause lifecycle hooks to reactivate, so watching change of entryId
    // to load new entries as needed.
    async entryId(newValue, oldValue) {
      console.log('EntryId switched from: ' + oldValue + ' to: ' + newValue)
      await this.loadRecentEntries()
      if (this.entryId) {
        this.getEntry()
      } else {
        this.initializeNewEntry()
      }
    }
  },
  methods: {
    ...mapActions(useDynamicConfigStore, ['loadWebloggerProperties']),
    ...mapActions(useSessionInfoStore, ['fetchWeblog']),
    ...mapActions(useStartupConfigStore, ['loadLookupValues']),
    getEntry: async function () {
      try {
        this.entry = await api.loadWeblogEntry(this.entryId!)
        this.commentCountMsg = this.$t('entryEdit.hasComments', {
          commentCount: this.entry.commentCountIncludingUnapproved
        })
        this.originalEntry = {
          ...this.entry
        }
      } catch (error) {
        this.commonErrorResponse(error as AxiosError)
      }
    },
    formatDateToMinute: function (date: Date) {
      return formatDateToMinute(date)
    },
    formatDateTime(date: Date) {
      return formatDateTime(date)
    },
    getRecentEntries: async function (entryType: PublishStatus) {
      try {
        console.log('weblogId:', this.weblogId)
        const response = await api.loadRecentEntries(this.weblogId, entryType)
        this.recentEntries[entryType] = response
      } catch (error) {
        this.commonErrorResponse(error as AxiosError)
      }
    },
    saveEntry: async function (saveType: PublishStatus) {
      this.messageClear()

      const oldStatus = this.entry.status
      this.entry.status = saveType

      try {
        const response = await api.saveWeblogEntry(this.weblogId, this.entry)
        if (!this.entry.id) {
          this.$router.push({
            name: 'entryEdit',
            params: { weblogId: this.weblogId },
            query: { entryId: response }
          })
        } else {
          this.getEntry()
          this.loadRecentEntries()
          window.scrollTo(0, 0)
        }
      } catch (error: unknown) {
        this.entry.status = oldStatus
        if (error instanceof AxiosError) {
          if (error.response?.status === 401) {
            this.errorObj = {
              errors: [
                this.$t('entryEdit.sessionTimedOut', {
                  loginUrl: import.meta.env.VITE_PUBLIC_PATH + '/app/login-redirect'
                })
              ]
            }
            window.scrollTo(0, 0)
          } else {
            this.commonErrorResponse(error)
          }
        }
      }
    },
    deleteWeblogEntry: function () {
      this.messageClear()
      api
        .deleteWeblogEntry(this.entryId!)
        .then(() => {
          this.$router.push({
            name: 'entryEdit',
            params: { weblogId: this.weblogId }
          })
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    loadRecentEntries: function () {
      this.getRecentEntries('DRAFT')
      this.getRecentEntries('PUBLISHED')
      this.getRecentEntries('SCHEDULED')
    },
    previewEntry: function () {
      window.open(this.entry.previewUrl)
    },
    updateTags: function (tagsString: string) {
      this.entry.tagsAsString = tagsString
    },
    messageClear: function () {
      this.errorObj = { errors: [] }
    },
    initializeNewEntry: function () {
      this.entry = createDefaultWeblogEntry()
      this.entry.commentCountIncludingUnapproved = 0
      this.entry.commentDays = this.weblog.defaultCommentDays
      this.entry.editFormat = this.weblog.editFormat
      this.entry.category = { id: '', name: '' }
      this.entry.category.name = this.weblog.weblogCategoryNames[0]
      this.originalEntry = {
        ...this.entry
      }
      window.scrollTo(0, 0)
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
    },
    checkDirty(
      to: RouteLocationNormalized,
      from: RouteLocationNormalized,
      next: NavigationGuardNext
    ) {
      if (this.formIsDirty) {
        confirmLeaveNoSaveDialogObj
          .reveal({
            title: this.$t('common.confirm'),
            message: this.$t('common.confirmLeaveNoSave'),
            confirmButtonText: 'Confirm',
            cancelButtonText: 'Cancel'
          })
          .then((confirmed) => {
            if (confirmed) {
              next()
            }
          })
          .catch(() => {
            // Handle the case where the user cancels the dialog
          })
      } else {
        next()
      }
    }
  },
  async created() {
    await this.loadWebloggerProperties()
    await this.loadLookupValues()
    await this.fetchWeblog(this.weblogId).then((fetchedWeblog) => {
      this.weblog = { ...fetchedWeblog }
    })
    await this.loadRecentEntries()
    if (this.entryId) {
      await this.getEntry()
    } else {
      this.initializeNewEntry()
    }
    this.isFetching = false
  },
  beforeRouteLeave(to, from, next) {
    if (to.path != from.path) {
      this.checkDirty(to, from, next)
    } else {
      next()
    }
  },
  beforeRouteUpdate(to, from, next) {
    if (to.path != from.path || (from.query.entryId && to.query.entryId != from.query.entryId)) {
      this.checkDirty(to, from, next)
    } else {
      next()
    }
  }
}
</script>

<style scoped>
.edit-icon {
  vertical-align: middle;
  border: none;
}
</style>
