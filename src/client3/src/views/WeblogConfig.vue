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
    <div v-if="weblogId">
      <AppUserNav />
    </div>
    <div id="weblogConfig" style="text-align: left; padding: 20px">
      <AppSuccessMessageBox :message="successMessage" @close-box="successMessage = ''" />
      <AppErrorListMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = []"
      ></AppErrorListMessageBox>

      <h2 v-if="!weblogId">{{ $t(varText.pageTitleKey) }}</h2>
      <p class="subtitle" v-html="getSubtitlePrompt(weblog.handle)"></p>
      <table class="formtable">
        <tbody>
          <tr>
            <td colspan="3">
              <h2>{{ $t('weblogConfig.generalSettings') }}</h2>
            </td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.websiteTitle') }}*</td>
            <td class="field">
              <input type="text" v-model="weblog.name" size="40" maxlength="255" />
            </td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.tagline') }}</td>
            <td class="field">
              <input type="text" v-model="weblog.tagline" size="40" maxlength="255" />
            </td>
            <td class="description">{{ $t('weblogConfig.tip.tagline') }}</td>
          </tr>

          <tr>
            <td class="label">
              <label for="handle">{{ $t('weblogConfig.handle') }}*</label>
            </td>
            <td class="field">
              <input
                id="handle"
                type="text"
                v-model="weblog.handle"
                size="30"
                maxlength="30"
                :required="weblogId == null ? true : false"
                :readonly="weblogId == null ? false : true"
              />
              <br />
              <span style="font-size: 70%">
                {{ $t('weblogConfig.weblogUrl') }}:&nbsp; {{ startupConfig?.absoluteSiteURL }}/<span
                  style="color: red"
                  >{{ weblog.handle }}</span
                >
              </span>
            </td>
            <td class="description">{{ $t('weblogConfig.tip.handle') }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.about') }}</td>
            <td class="field">
              <textarea v-model="weblog.about" rows="3" cols="40" maxlength="255"></textarea>
            </td>
            <td class="description">{{ $t('weblogConfig.tip.about') }}</td>
          </tr>

          <tr v-if="!weblogId">
            <td class="label">
              <label for="theme">{{ $t('weblogConfig.theme') }}*</label>
            </td>
            <td class="field">
              <select id="theme" v-model="weblog.theme" size="1">
                <option
                  v-for="(theme, key) in lookupValues?.sharedThemeMap"
                  :value="key"
                  :key="key"
                >
                  {{ theme.name }}
                </option>
              </select>
              <div v-if="startupConfig" style="height: 400px">
                <p>{{ lookupValues?.sharedThemeMap[weblog.theme].description }}</p>
                <img
                  v-bind:src="
                    startupConfig.absoluteSiteURL +
                    lookupValues?.sharedThemeMap[weblog.theme].previewImagePath
                  "
                />
              </div>
            </td>
            <td class="description">{{ $t('weblogConfig.tip.theme') }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.editFormat') }}</td>
            <td class="field">
              <select v-model="weblog.editFormat" size="1" required>
                <option v-for="(value, key) in lookupValues?.editFormats" :value="key" :key="key">
                  {{ $t(value) }}
                </option>
              </select>
            </td>
            <td class="description">{{ $t('weblogConfig.tip.editFormat') }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.visible') }}</td>
            <td class="field">
              <input type="checkbox" v-model="weblog.visible" />
            </td>
            <td class="description">{{ $t('weblogConfig.tip.visible') }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.entriesPerPage') }}</td>
            <td class="field">
              <input
                type="number"
                min="1"
                max="100"
                step="1"
                v-model="weblog.entriesPerPage"
                size="3"
              />
            </td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.locale') }}*</td>
            <td class="field">
              <select v-model="weblog.locale" size="1">
                <option v-for="(value, key) in lookupValues?.locales" :key="key" :value="key">
                  {{ value }}
                </option>
              </select>
            </td>
            <td class="description">{{ $t('weblogConfig.tip.locale') }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.timezone') }}*</td>
            <td class="field">
              <select v-model="weblog.timeZone" size="1">
                <option v-for="(value, key) in lookupValues?.timezones" :key="key" :value="key">
                  {{ value }}
                </option>
              </select>
            </td>
            <td class="description">{{ $t('weblogConfig.tip.timezone') }}</td>
          </tr>

          <tr v-if="webloggerProperties.usersOverrideAnalyticsCode">
            <td class="label">
              {{ $t('weblogConfig.analyticsTrackingCode') }}
            </td>
            <td class="field">
              <textarea
                v-model="weblog.analyticsCode"
                rows="10"
                cols="70"
                maxlength="1200"
              ></textarea>
            </td>
            <td class="description">
              {{ $t('weblogConfig.tip.analyticsTrackingCode') }}
            </td>
          </tr>
        </tbody>

        <tbody v-if="webloggerProperties.commentPolicy != 'NONE'">
          <tr>
            <td colspan="3">
              <h2>{{ $t('weblogConfig.commentSettings') }}</h2>
            </td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.allowComments') }}</td>
            <td class="field">
              <select v-model="weblog.allowComments" size="1">
                <option
                  v-for="commentoption in filteredCommentOptions"
                  :value="commentoption.level"
                  :key="commentoption.level"
                >
                  {{ $t(commentoption.label) }}
                </option>
              </select>
            </td>
          </tr>
        </tbody>

        <tbody v-show="commentingActive">
          <tr>
            <td class="label">{{ $t('weblogConfig.defaultCommentDays') }}</td>
            <td class="field">
              <select v-model="weblog.defaultCommentDays" size="1">
                <option
                  v-for="(value, key) in lookupValues?.commentDayOptions"
                  :key="key"
                  :value="key"
                >
                  {{ $t(String(value)) }}
                </option>
              </select>
            </td>
          </tr>

          <tr v-if="weblogId">
            <td class="label">{{ $t('weblogConfig.applyCommentDefaults') }}</td>
            <td class="field">
              <input type="checkbox" v-model="weblog.applyCommentDefaults" />
            </td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.spamPolicy') }}</td>
            <td class="field">
              <select v-model="weblog.spamPolicy" size="1">
                <option
                  v-for="spamoption in filteredSpamOptions"
                  :key="spamoption.level"
                  :value="spamoption.level"
                >
                  {{ $t(spamoption.label) }}
                </option>
              </select>
            </td>
            <td class="description">{{ $t('weblogConfig.tip.spamPolicy') }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t('weblogConfig.ignoreUrls') }}</td>
            <td class="field">
              <textarea v-model="weblog.blacklist" rows="7" cols="70"></textarea>
            </td>
            <td class="description">{{ $t('weblogConfig.tip.ignoreUrls') }}</td>
          </tr>
        </tbody>
      </table>

      <div class="control">
        <span>
          <button type="button" class="buttonBox" v-on:click="updateWeblog()">
            {{ $t(varText.saveButtonText) }}
          </button>
          <button type="button" class="buttonBox" v-on:click="cancelChanges()">
            {{ $t('common.cancel') }}
          </button>
        </span>

        <span v-if="weblogId" style="float: right">
          <button @click="deleteDialog.reveal">
            {{ $t('weblogConfig.deleteWeblogButton') }}
          </button>
        </span>
      </div>
    </div>

    <!-- Delete weblog modal -->
    <Teleport to="#modal-div">
      <div v-if="deleteDialog.isRevealed.value" class="vueuse-modal-layout">
        <div class="vueuse-modal">
          <div class="modal-header">
            <h5
              class="modal-title"
              v-html="$t('weblogConfig.deleteConfirm', { handle: weblog.handle })"
            ></h5>
            <button
              @click="deleteDialog.cancel"
              type="button"
              class="btn-close"
              aria-label="Close"
            ></button>
          </div>
          <div class="modal-body">
            <div v-html="$t('weblogConfig.deleteWarning')" class="text-danger"></div>
            <br />
            <div>
              {{
                $t('weblogConfig.deleteInstruction', {
                  handle: weblog.handle
                })
              }}
            </div>
            <div>
              <label for="newTag">{{ $t('weblogConfig.handle') }}:</label>
              <input v-model="deleteHandle" type="text" />
            </div>
          </div>
          <div class="modal-footer">
            <button @click="removeWeblog()">{{ $t('common.delete') }}</button>
            <button @click="deleteDialog.cancel">{{ $t('common.cancel') }}</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script lang="ts">
import type { Weblog, ErrorObj, ErrorItem } from '@/types'
import { useSessionInfoStore } from '../stores/sessionInfo'
import { useStartupConfigStore } from '../stores/startupConfig'
import { useDynamicConfigStore } from '../stores/dynamicConfig'
import { mapState, mapActions } from 'pinia'
import { AxiosError } from 'axios'
import { useConfirmDialog } from '@vueuse/core'

const deleteDialogObj = useConfirmDialog()

export default {
  props: {
    weblogId: {
      required: false,
      type: String
    }
  },
  data: function () {
    return {
      weblog: {
        name: '',
        tagline: '',
        about: '',
        handle: '',
        applyCommentDefaults: false,
        theme: 'rolling',
        locale: 'en',
        timeZone: 'America/New_York',
        editFormat: 'HTML',
        allowComments: 'NONE',
        spamPolicy: 'NO_EMAIL',
        visible: true,
        entriesPerPage: 12,
        defaultCommentDays: -1
      } as Weblog,
      deleteDialogTitle: null,
      deleteDialogWarning: null,
      deleteDialogInstruction: null,
      deleteHandle: '',
      successMessage: '',
      isFetching: true,
      errorObj: {
        errors: []
      } as ErrorObj
    }
  },
  computed: {
    ...mapState(useSessionInfoStore, ['sessionInfo']),
    ...mapState(useStartupConfigStore, ['startupConfig', 'lookupValues']),
    ...mapState(useDynamicConfigStore, ['webloggerProperties']),
    deleteDialog: function () {
      return deleteDialogObj
    },
    commentingActive: function () {
      return (
        this.webloggerProperties.commentPolicy !== 'NONE' && this.weblog.allowComments !== 'NONE'
      )
    },
    filteredSpamOptions: function () {
      if (this.lookupValues && this.lookupValues.spamOptionList) {
        return this.lookupValues.spamOptionList.filter(
          (item: { level: number }) => item.level >= this.webloggerProperties.spamPolicyLevel
        )
      } else {
        return []
      }
    },
    filteredCommentOptions: function () {
      if (this.lookupValues && this.lookupValues.commentOptionList) {
        return this.lookupValues.commentOptionList.filter(
          (item: { level: number }) => item.level <= this.webloggerProperties.commentPolicyLevel
        )
      } else {
        return []
      }
    },
    varText: function () {
      if (this.weblogId) {
        return {
          saveButtonText: 'common.save',
          subtitlePrompt: 'weblogConfig.updateWeblogPrompt',
          pageTitleKey: 'weblogConfig.updateTitle'
        }
      } else {
        return {
          saveButtonText: 'weblogConfig.createWeblogButton',
          subtitlePrompt: 'weblogConfig.createWeblogPrompt',
          pageTitleKey: 'weblogConfig.createTitle'
        }
      }
    }
  },
  methods: {
    ...mapActions(useSessionInfoStore, ['fetchWeblog', 'upsertWeblog', 'deleteWeblog']),
    ...mapActions(useDynamicConfigStore, ['loadWebloggerProperties']),
    ...mapActions(useStartupConfigStore, ['loadStartupConfig', 'loadLookupValues']),
    getSubtitlePrompt(param: string): string {
      // Replace the placeholder with the actual parameter
      return this.$t(this.varText.subtitlePrompt, { handle: param })
    },
    updateWeblog: function () {
      this.messageClear()
      const isNew = !this.weblogId
      this.upsertWeblog({ ...this.weblog })
        .then(() => {
          if (isNew) {
            this.$router.push({ name: 'myBlogs' })
          } else {
            this.successMessage = this.$t('common.changesSaved')
            window.scrollTo(0, 0)
          }
        })
        .catch((error) => {
          if (error.response.status === 400) {
            this.errorObj = error.response.data
            window.scrollTo(0, 0)
          } else {
            this.commonErrorResponse(error)
          }
        })
    },
    removeWeblog: function () {
      if (
        this.weblog.id &&
        this.weblog.handle.toUpperCase() === this.deleteHandle.toUpperCase().trim()
      ) {
        this.deleteWeblog(this.weblog.id)
          .then(() => {
            this.$router.push({ name: 'myBlogs' })
          })
          .catch((error) => this.commonErrorResponse(error))
      }
    },
    loadWeblog: function () {
      if (this.weblogId) {
        this.fetchWeblog(this.weblogId).then((fetchedWeblog) => {
          if (fetchedWeblog == null) {
            this.errorObj = { errors: ['Weblog not found'] }
            return
          }
          this.weblog = { ...fetchedWeblog }
        })
      }
    },
    cancelChanges: function () {
      this.messageClear()
      if (this.weblogId) {
        this.loadWeblog()
        window.scrollTo(0, 0)
      } else {
        this.$router.push({ name: 'myBlogs' })
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
    },
    messageClear: function () {
      this.errorObj = { errors: [] }
      this.successMessage = ''
    }
  },
  async created() {
    await this.loadWebloggerProperties()
    await this.loadStartupConfig()
    await this.loadLookupValues()
    if (this.weblogId) {
      this.loadWeblog()
    }
    this.isFetching = false
  }
}
</script>
