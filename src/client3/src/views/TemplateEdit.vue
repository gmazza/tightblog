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
<template v-if="asyncDataStatus_ready">
  <div v-if="!isFetching">
    <AppTitleBar />
    <div style="text-align: left; padding: 20px">
      <AppSuccessMessageBox :message="successMessage" @close-box="successMessage = null" />
      <AppErrorListMessageBox
        v-bind:inErrorObj="errorObj"
        @close-box="errorObj.errors = []"
      ></AppErrorListMessageBox>
      <h2>{{ $t('templateEdit.title') }}</h2>
      <p class="pagetip" v-html="$t('templateEdit.tip')"></p>

      <table cellspacing="5">
        <tr>
          <td class="label">{{ $t('common.name') }}</td>
          <td class="field">
            <input
              id="name"
              type="text"
              v-model="templateData.name"
              size="50"
              maxlength="255"
              :style="{
                background: templateData.derivation == 'SPECIFICBLOG' ? 'white' : 'lightgrey'
              }"
              :readonly="templateData.derivation != 'SPECIFICBLOG'"
            />
            <span v-if="templateLoaded && templateData.role.accessibleViaUrl">
              <br />
              <span v-if="lastSavedName != null">
                [<a id="launchLink" v-on:click="launchPage()">{{ $t('templateEdit.launch') }}</a
                >]
              </span>
            </span>
          </td>
        </tr>

        <tr v-if="templateLoaded">
          <td class="label">{{ $t('common.role') }}</td>
          <td class="field">
            <span>{{ templateData.role.readableName }}</span>
          </td>
        </tr>

        <tr v-if="templateLoaded && !templateData.role.singleton">
          <td class="label" valign="top" style="padding-top: 4px">
            {{ $t('common.description') }}
          </td>
          <td class="field">
            <textarea
              id="description"
              type="text"
              v-model="templateData.description"
              cols="50"
              rows="2"
            ></textarea>
          </td>
        </tr>
      </table>

      <textarea v-model="templateData.template" rows="20" style="width: 100%"></textarea>

      <table style="width: 100%">
        <tr>
          <td>
            <button type="button" @click="saveTemplate()">
              {{ $t('common.saveChanges') }}
            </button>
            <button type="button" @click="cancel()">
              {{ $t('templateEdit.returnToTemplates') }}
            </button>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import type { Template, ErrorObj, ErrorItem } from '@/types'
import { mapState } from 'pinia'
import { AxiosError } from 'axios'
import type { RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { useConfirmDialog } from '@vueuse/core'
import { useSessionInfoStore } from '../stores/sessionInfo'
import * as api from '@/api'

const confirmLeaveNoSaveDialogObj = useConfirmDialog()

export default {
  props: {
    weblogId: {
      required: true,
      type: String
    },
    templateId: {
      required: true,
      type: String
    },
    templateName: {
      required: false,
      type: String
    }
  },
  data() {
    return {
      templateData: {} as Template,
      originalTemplateData: {} as Template,
      lastSavedName: null as string | null,
      successMessage: null as string | null,
      errorObj: {
        errors: []
      } as ErrorObj,
      templateLoaded: false,
      workingTemplateId: null as string | null,
      isFetching: true
    }
  },
  computed: {
    ...mapState(useSessionInfoStore, ['sessionInfo']),
    sessionInfoStore() {
      return useSessionInfoStore()
    },
    formIsDirty: function () {
      return (
        this.originalTemplateData.description !== this.templateData.description ||
        this.originalTemplateData.template !== this.templateData.template
      )
    },
    confirmLeaveNoSaveDialog: function () {
      return confirmLeaveNoSaveDialogObj
    }
  },
  methods: {
    getWeblog(id: string) {
      // This assumes you have a getter named getWeblog in your Pinia store
      return this.sessionInfoStore.getWeblog(id)
    },
    launchPage: function () {
      const weblog = this.getWeblog(this.weblogId)
      if (weblog && weblog.handle) {
        window.open(
          this.sessionInfo.absoluteUrl + '/' + weblog.handle + '/page/' + this.lastSavedName,
          '_blank'
        )
      } else {
        // Optionally handle the error, e.g., show a message or log
        console.error('Weblog or weblog.handle is null')
      }
    },
    loadTemplate: function () {
      let promise: Promise<Template>
      if (this.workingTemplateId && this.workingTemplateId.indexOf(':') < 0) {
        promise = api.loadTemplateById(this.workingTemplateId)
      } else {
        promise = api.loadTemplateByName(this.weblogId, this.templateName!!)
      }
      promise
        .then((response) => {
          this.templateData = response
          this.lastSavedName = this.templateData.name
          this.originalTemplateData = {
            ...this.templateData
          }
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    saveTemplate: function () {
      this.messageClear()
      api
        .saveTemplate(this.weblogId, this.templateData)
        .then((response) => {
          this.workingTemplateId = response.data
          this.loadTemplate()
          this.successMessage = this.$t('common.changesSaved')
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    cancel() {
      this.$router.push({
        name: 'templates',
        params: { weblogId: this.weblogId }
      })
    },
    messageClear: function () {
      this.successMessage = null
      this.errorObj = { errors: [] }
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
    this.workingTemplateId = this.templateId
    await this.loadTemplate()
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
    if (to.path != from.path) {
      this.checkDirty(to, from, next)
    } else {
      next()
    }
  }
}
</script>

<style></style>
