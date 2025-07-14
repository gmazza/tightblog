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
      <h2>{{ $t('templates.title') }}</h2>
      <p class="pagetip" v-html="$t('templates.tip')"></p>

      <div class="control clearfix">
        <span style="padding-left: 7px">
          {{ $t('templates.currentTheme') }}:
          <b>{{ weblog!!.theme }}</b>
        </span>

        <span style="float: right" v-show="switchToThemes.length > 0">
          <button type="button" v-on:click="confirmSwitchThemeDialog.reveal">
            {{ $t('templates.switchTheme') }}
          </button>

          <select id="switchThemeMenu" size="1" required v-model="targetThemeId">
            <option v-for="theme in switchToThemes" :key="theme.id" :value="theme.id">
              {{ theme.name }}
            </option>
          </select>
        </span>
      </div>

      <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
          <tr>
            <th width="4%">
              <input
                type="checkbox"
                v-model="allFilesSelected"
                v-on:input="toggleCheckboxes(($event.target as HTMLInputElement).checked)"
                title="$t('templates.selectAllLabel')"
              />
            </th>
            <th width="17%">
              {{ $t('common.name') }}
            </th>
            <th width="16%">
              {{ $t('common.role') }}
            </th>
            <th width="38%">
              {{ $t('common.description') }}
            </th>
            <th width="8%">
              {{ $t('templates.source') }}
              <AppHelpPopup :helpText="$t('templates.sourceTooltip')" />
            </th>
            <th width="13%">
              {{ $t('common.lastModified') }}
            </th>
            <th width="4%">
              {{ $t('common.view') }}
            </th>
          </tr>
        </thead>
        <tbody v-if="weblogTemplateData" v-cloak>
          <tr v-for="tpl in weblogTemplateData.templates" :key="tpl.id">
            <td class="center" style="vertical-align: middle">
              <span v-if="!isBuiltIn(tpl)">
                <input
                  type="checkbox"
                  name="idSelections"
                  v-model="tpl.selected"
                  v-bind:value="tpl.id"
                />
              </span>
            </td>

            <td style="vertical-align: middle">
              <span>
                <router-link
                  :to="{
                    name: 'templateEdit',
                    params: {
                      weblogId
                    },
                    query: {
                      templateId: tpl.id,
                      templateName: tpl.name
                    }
                  }"
                  >{{ tpl.name }}</router-link
                >
              </span>
            </td>

            <td style="vertical-align: middle">
              {{ tpl.role.readableName }}
            </td>

            <td style="vertical-align: middle">
              <span
                v-if="
                  tpl.role.singleton != true && tpl.description != null && tpl.description != ''
                "
              >
                {{ tpl.description }}
              </span>
            </td>

            <td style="vertical-align: middle">
              {{ tpl.derivation }}
            </td>

            <td>
              <span v-if="tpl.lastModified != null">
                {{ formatDateTime(tpl.lastModified) }}
              </span>
            </td>

            <td class="buttontd">
              <span v-if="tpl.role.accessibleViaUrl">
                <a
                  target="_blank"
                  v-bind:href="
                    sessionInfo.absoluteUrl + '/' + weblog!!.handle + '/page/' + tpl.name
                  "
                >
                  <img src="@/assets/world_go.png" class="no-border" alt="icon" />
                </a>
              </span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="control">
        <span style="padding-left: 7px">
          <button
            type="button"
            v-bind:disabled="templatesSelectedCount == 0"
            v-on:click="confirmDeleteTemplatesDialog.reveal"
          >
            {{ $t('common.deleteSelected') }}
          </button>
        </span>
      </div>

      <div class="menu-tr sidebarFade">
        <div class="sidebarInner">
          <form name="myform">
            <div>
              {{ $t('templates.createNewTemplate') }}
            </div>
            <table cellpadding="0" cellspacing="6">
              <tr>
                <td>
                  {{ $t('common.name') }}
                </td>
                <td>
                  <input type="text" v-model="newTemplateName" maxlength="40" required />
                </td>
              </tr>
              <tr>
                <td>
                  {{ $t('common.role') }}
                </td>
                <td>
                  <select v-model="newTemplateRole" size="1" required>
                    <option
                      v-for="templateRole in weblogTemplateData.availableTemplateRoles"
                      :value="templateRole.constant"
                      :key="templateRole.constant"
                    >
                      {{ templateRole.readableName }}
                    </option>
                  </select>
                </td>
              </tr>
              <tr>
                <td colspan="2" class="field">
                  <p v-if="selectedTemplateRole">
                    {{ $t(selectedTemplateRole.descriptionProperty) }}
                  </p>
                </td>
              </tr>
              <tr>
                <td>
                  <button type="button" v-on:click="addTemplate()">
                    {{ $t('common.add') }}
                  </button>
                </td>
              </tr>
            </table>
          </form>
        </div>
      </div>
    </div>
  </div>
  <Teleport to="#modal-div">
    <div v-if="confirmDeleteTemplatesDialog.isRevealed.value" class="vueuse-modal-layout">
      <div class="vueuse-modal">
        <div class="modal-header">
          <h5
            class="modal-title"
            v-html="
              $t('templates.confirmDeleteTemplate', {
                count: templatesSelectedCount
              })
            "
          ></h5>
          <button
            @click="confirmDeleteTemplatesDialog.cancel"
            type="button"
            class="btn-close ms-auto"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <p v-html="$t('templates.deleteWarning')" class="text-danger"></p>
        </div>
        <div class="modal-footer">
          <button @click="deleteTemplates()">{{ $t('common.confirm') }}</button>
          <button @click="confirmDeleteTemplatesDialog.cancel">{{ $t('common.cancel') }}</button>
        </div>
      </div>
    </div>
  </Teleport>
  <Teleport to="#modal-div">
    <div v-if="confirmSwitchThemeDialog.isRevealed.value" class="vueuse-modal-layout">
      <div class="vueuse-modal">
        <div class="modal-header">
          <h5
            class="modal-title"
            v-html="
              $t('templates.confirmSwitchTemplate', {
                targetThemeName
              })
            "
          ></h5>
          <button
            @click="confirmSwitchThemeDialog.cancel"
            type="button"
            class="btn-close ms-auto"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <p v-html="$t('templates.switchWarning')" class="text-danger"></p>
        </div>
        <div class="modal-footer">
          <button @click="switchTheme()">{{ $t('common.confirm') }}</button>
          <button @click="confirmSwitchThemeDialog.cancel">{{ $t('common.cancel') }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script lang="ts">
import type { ErrorItem, ErrorObj, Template, Weblog, WeblogTemplateData } from '@/types'
import { mapState, mapActions } from 'pinia'
import { useSessionInfoStore } from '../stores/sessionInfo'
import { formatDateTime } from '../helpers'
import * as api from '@/api'
import { AxiosError } from 'axios'
import { useConfirmDialog } from '@vueuse/core'

const confirmDeleteTemplatesDialogObj = useConfirmDialog()
const confirmSwitchThemeDialogObj = useConfirmDialog()

export default {
  props: {
    weblogId: {
      required: true,
      type: String
    }
  },
  data() {
    return {
      weblog: null as Weblog | null,
      weblogTemplateData: {
        themes: [],
        templates: [],
        availableTemplateRoles: []
      } as WeblogTemplateData,
      allFilesSelected: false,
      newTemplateName: '',
      newTemplateRole: null as string | null,
      deleteDialogTitle: null,
      switchThemeTitle: null,
      targetThemeId: null as string | null,
      successMessage: null as string | null,
      errorObj: {
        errors: []
      } as ErrorObj,
      isFetching: true
    }
  },
  computed: {
    ...mapState(useSessionInfoStore, ['sessionInfo']),
    selectedTemplateRole: function () {
      const tempRoleCopy = this.newTemplateRole
      if (!tempRoleCopy) {
        return null
      }
      return this.weblogTemplateData.availableTemplateRoles.filter(function (atr) {
        return atr.constant === tempRoleCopy
      })[0]
    },
    switchToThemes: function () {
      const weblog = this.weblog
      return this.weblogTemplateData.themes.filter(function (theme) {
        return theme.id !== weblog!!.theme
      })
    },
    targetThemeName: function () {
      // extra variable to activate value refreshing
      const themeIdToCompare = this.targetThemeId
      const targetTheme = this.weblogTemplateData.themes.filter(function (theme) {
        return theme.id === themeIdToCompare
      })
      return targetTheme[0].name
    },
    templatesSelectedCount: function () {
      return this.weblogTemplateData.templates.filter((tmpl) => tmpl.selected).length
    },
    confirmDeleteTemplatesDialog() {
      return confirmDeleteTemplatesDialogObj
    },
    confirmSwitchThemeDialog() {
      return confirmSwitchThemeDialogObj
    }
  },
  methods: {
    ...mapActions(useSessionInfoStore, ['fetchWeblog', 'refreshWeblog', 'upsertWeblog']),
    formatDateTime(date: Date) {
      return formatDateTime(date)
    },
    loadTemplateData: function () {
      api.loadTemplateData(this.weblogId).then((response) => {
        this.weblogTemplateData = response
        this.allFilesSelected = false
        this.targetThemeId = this.switchToThemes[0].id
      })
    },
    addTemplate: function () {
      this.messageClear()
      if (!this.newTemplateName) {
        return
      }
      api
        .addTemplate(this.weblogId, this.newTemplateName, this.newTemplateRole!!)
        .then(() => {
          this.successMessage = this.$t('common.changesSaved')
          this.loadTemplateData()
          this.resetAddTemplateData()
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    switchTheme: function () {
      this.messageClear()
      api
        .switchTheme(this.weblogId, this.targetThemeId!!)
        .then(() => {
          this.confirmSwitchThemeDialog.cancel()
          this.refreshWeblog(this.weblogId).then((refreshedWeblog) => {
            this.weblog = refreshedWeblog
          })
          this.successMessage = this.$t('common.changesSaved')
          this.loadTemplateData()
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    toggleCheckboxes: function (checkAll: boolean) {
      this.weblogTemplateData.templates
        .filter((tmpl) => !this.isBuiltIn(tmpl))
        .forEach((tmpl) => {
          tmpl.selected = checkAll
          this.$forceUpdate()
        })
    },
    messageClear: function () {
      this.successMessage = null
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
    },
    deleteTemplates: function () {
      this.messageClear()
      const selectedItems: Array<string> = []
      this.weblogTemplateData.templates.forEach((template) => {
        if (template.selected) selectedItems.push(template.id)
      })

      api
        .deleteTemplates(this.weblogId, selectedItems)
        .then(() => {
          this.successMessage = this.$t('common.changesSaved')
          this.confirmDeleteTemplatesDialog.cancel()
          this.loadTemplateData()
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    isBuiltIn: function (template: Template) {
      return template && template.derivation === 'SHARED'
    },
    resetAddTemplateData: function () {
      this.newTemplateName = ''
      this.newTemplateRole = 'CUSTOM_EXTERNAL'
    }
  },
  async created() {
    await this.fetchWeblog(this.weblogId).then((fetchedWeblog) => {
      if (fetchedWeblog) {
        this.weblog = { ...fetchedWeblog }
      }
    })
    await this.loadTemplateData()
    await this.resetAddTemplateData()
    this.isFetching = false
  }
}
</script>

<style>
.no-border {
  border: none;
}
</style>
