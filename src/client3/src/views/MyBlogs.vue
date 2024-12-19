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
    <div style="text-align: left; padding: 20px">
      <h1>{{ $t('myBlogs.title') }}</h1>
      <span v-show="userWeblogRoles.length == 0">
        <p>{{ $t('myBlogs.noblogs') }}</p>
      </span>

      <div v-for="role in userWeblogRoles" :key="role.weblog.id">
        <span class="mm_weblog_name"
          ><img src="@/assets/folder.png" />&nbsp;{{ role.weblog.name }}</span
        >

        <table class="mm_table" width="100%" cellpadding="0" cellspacing="0">
          <tr>
            <td valign="top">
              <table cellpadding="0" cellspacing="0">
                <tr>
                  <td class="mm_subtable_label">
                    {{ $t('myBlogs.weblogLink') }}
                  </td>
                  <td>
                    <a v-bind:href="role.weblog.absoluteURL" target="_blank">{{
                      role.weblog.absoluteURL
                    }}</a>
                  </td>
                </tr>

                <tr>
                  <td class="mm_subtable_label">
                    {{ $t('common.description') }}
                  </td>
                  <td>{{ role.weblog.about }}</td>
                </tr>

                <tr>
                  <td class="mm_subtable_label">{{ $t('common.role') }}</td>
                  <td>{{ getRoleText(role.weblogRole) }}</td>
                </tr>

                <tr>
                  <td class="mm_subtable_label">{{ $t('myBlogs.emailComments') }}</td>
                  <td>
                    <input
                      type="checkbox"
                      v-model="role.emailComments"
                      v-on:change="setReceiveCommentsForWeblog(role)"
                    />
                  </td>
                </tr>

                <tr>
                  <td class="mm_subtable_label">
                    {{ $t('myBlogs.todaysHits') }}
                  </td>
                  <td>{{ role.weblog.hitsToday }}</td>
                </tr>
              </table>
            </td>

            <td class="mm_table_actions" width="20%">
              <img src="@/assets/table_edit.png" />
              <router-link
                :to="{
                  name: 'entryEdit',
                  params: {
                    weblogId: role.weblog.id
                  }
                }"
                >{{ $t('common.newEntry') }}</router-link
              >
              <br />

              <img src="@/assets/table_multiple.png" />
              <router-link :to="{ name: 'entries', params: { weblogId: role.weblog.id } }">{{
                $t('common.entries')
              }}</router-link>
              <br />

              <img src="@/assets/page_white_edit.png" />
              <!--router-link :to="{ name: 'comments', params: { weblogId: role.weblog.id } }">{{
                $t('common.comments')
              }}</router-link-->
              <span v-if="role.weblog.unapprovedComments > 0">
                {{
                  $t('weblogConfig.deleteConfirm', {
                    count: role.weblog.unapprovedComments
                  })
                }}
              </span>
              <br />

              <!-- Only admins get access to theme and config settings -->
              <!--span v-if="role.weblogRole == 'OWNER'"-->
              <!-- And only show theme option if custom themes are enabled -->
              <span v-if="webloggerProperties.usersCustomizeThemes">
                <img src="@/assets/layout.png" />
                <!--router-link
                    :to="{
                      name: 'templates',
                      params: {
                        weblogId: role.weblog.id
                      }
                    }"
                    >{{ $t('myBlogs.theme') }}</router-link
                  -->
                <br />
              </span>

              <img src="@/assets/cog.png" />
              <router-link
                :to="{
                  name: 'weblogConfig',
                  params: { weblogId: role.weblog.id }
                }"
                >{{ $t('weblogConfig.updateTitle') }}</router-link
              >
              <br />
              <!--/span-->

              <!-- disallow owners from resigning from blog -->
              <span v-if="role.weblogRole !== 'OWNER'">
                <button type="button" v-on:click="confirmResignDialog.reveal">
                  <img src="@/assets/delete.png" /> {{ $t('myBlogs.resign') }}
                </button>
              </span>
            </td>
          </tr>
        </table>
        <Teleport to="#modal-div">
          <div v-if="confirmResignDialog.isRevealed.value" class="vueuse-modal-layout">
            <div class="vueuse-modal">
              <div class="modal-header">
                <h5
                  class="modal-title"
                  v-html="
                    $t('myBlogs.confirmResignation', {
                      weblogName: role.weblog.name
                    })
                  "
                ></h5>
                <button
                  @click="confirmResignDialog.cancel"
                  type="button"
                  class="btn-close"
                  aria-label="Close"
                ></button>
              </div>
              <div class="modal-footer">
                <button @click="detachUserFromWeblog(role.id)">{{ $t('common.confirm') }}</button>
                <button @click="confirmResignDialog.cancel">{{ $t('common.cancel') }}</button>
              </div>
            </div>
          </div>
        </Teleport>
      </div>

      <form v-if="sessionInfo.userCanCreateBlogs" @submit.stop.prevent="createWeblog()">
        <div class="control clearfix">
          <input type="submit" :value="$t('myBlogs.createWeblog')" />
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { useSessionInfoStore } from '../stores/sessionInfo'
import { useDynamicConfigStore } from '../stores/dynamicConfig'
import { mapState, mapActions } from 'pinia'
import { useConfirmDialog } from '@vueuse/core'
import api from '@/api'

const confirmResignDialogObj = useConfirmDialog()

export default {
  data: function () {
    return {
      roles: [],
      errorObj: {},
      isFetching: true
    }
  },
  computed: {
    ...mapState(useSessionInfoStore, ['sessionInfo', 'userWeblogRoles']),
    ...mapState(useDynamicConfigStore, ['webloggerProperties']),
    confirmResignDialog: function () {
      return confirmResignDialogObj
    }
  },
  methods: {
    ...mapActions(useSessionInfoStore, [
      'loadSessionInfo',
      'loadUserWeblogRoles',
      'setReceiveCommentsForWeblog',
      'detachUserFromWeblog'
    ]),
    ...mapActions(useDynamicConfigStore, ['loadWebloggerProperties']),
    createWeblog: function () {
      this.$router.push({ name: 'createWeblog' })
    },
    getRoleText: function (weblogRole: string) {
      if (weblogRole === 'POST') {
        return 'PUBLISHER'
      } // else 'OWNER'
      return weblogRole
    }
  },
  async created() {
    if (this.webloggerProperties === undefined || this.webloggerProperties.length === 0) {
      await this.loadWebloggerProperties()
    }
    if (!this.sessionInfo.authenticatedUser) {
      await this.loadSessionInfo()
    }
    if (this.userWeblogRoles === undefined || this.userWeblogRoles.length === 0) {
      await this.loadUserWeblogRoles()
    }
    this.isFetching = false
  }
}
</script>
