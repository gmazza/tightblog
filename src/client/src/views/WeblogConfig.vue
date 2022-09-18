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
  <div v-if="asyncDataStatus_ready">
    <AppTitleBar />
    <div v-if="weblogId">
      <AppUserNav />
    </div>
    <div style="text-align: left; padding: 20px">
      <AppSuccessMessageBox
        :message="successMessage"
        @close-box="successMessage = null"
      />
      <AppErrorListMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = null"
      ></AppErrorListMessageBox>

      <h2 v-if="!weblogId">{{ $t(varText.pageTitleKey) }}</h2>
      <p class="subtitle">{{ $t(varText.subtitleKey) }}</p>
      <table class="formtable">
        <tbody>
          <tr>
            <td colspan="3">
              <h2>{{ $t("weblogConfig.generalSettings") }}</h2>
            </td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.websiteTitle") }}*</td>
            <td class="field">
              <input
                type="text"
                v-model="weblog.name"
                size="40"
                maxlength="255"
              />
            </td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.tagline") }}</td>
            <td class="field">
              <input
                type="text"
                v-model="weblog.tagline"
                size="40"
                maxlength="255"
              />
            </td>
            <td class="description">{{ $t("weblogConfig.tip.tagline") }}</td>
          </tr>

          <tr>
            <td class="label">
              <label for="handle">{{ $t("weblogConfig.handle") }}*</label>
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
              <span style="text-size: 70%">
                {{ $t("weblogConfig.weblogUrl") }}:&nbsp;
                {{ startupConfig.absoluteSiteURL }}/<span style="color: red">{{
                  weblog.handle
                }}</span>
              </span>
            </td>
            <td class="description">{{ $t("weblogConfig.tip.handle") }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.about") }}</td>
            <td class="field">
              <textarea
                v-model="weblog.about"
                rows="3"
                cols="40"
                maxlength="255"
              ></textarea>
            </td>
            <td class="description">{{ $t("weblogConfig.tip.about") }}</td>
          </tr>

          <tr v-if="!weblogId">
            <td class="label">
              <label for="theme">{{ $t("weblogConfig.theme") }}*</label>
            </td>
            <td class="field">
              <select id="theme" v-model="weblog.theme" size="1">
                <option
                  v-for="(theme, key) in lookupVals.sharedThemeMap"
                  :value="key"
                  :key="key"
                >
                  {{ theme.name }}
                </option>
              </select>
              <div v-if="false" style="height: 400px">
                <p>{{ lookupVals.sharedThemeMap[weblog.theme].description }}</p>
                <img
                  v-bind:src="
                    startupConfig.absoluteSiteURL +
                    lookupVals.sharedThemeMap[weblog.theme].previewPath
                  "
                />
              </div>
            </td>
            <td class="description">{{ $t("weblogConfig.tip.theme") }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.editFormat") }}</td>
            <td class="field">
              <select v-model="weblog.editFormat" size="1" required>
                <option
                  v-for="(value, key) in lookupVals.editFormats"
                  :value="key"
                  :key="key"
                >
                  {{ $t(value) }}
                </option>
              </select>
            </td>
            <td class="description">{{ $t("weblogConfig.tip.editFormat") }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.visible") }}</td>
            <td class="field">
              <input type="checkbox" v-model="weblog.visible" />
            </td>
            <td class="description">{{ $t("weblogConfig.tip.visible") }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.entriesPerPage") }}</td>
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
            <td class="label">{{ $t("weblogConfig.locale") }}*</td>
            <td class="field">
              <select v-model="weblog.locale" size="1">
                <option
                  v-for="(value, key) in lookupVals.locales"
                  :key="key"
                  :value="key"
                >
                  {{ value }}
                </option>
              </select>
            </td>
            <td class="description">{{ $t("weblogConfig.tip.locale") }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.timezone") }}*</td>
            <td class="field">
              <select v-model="weblog.timeZone" size="1">
                <option
                  v-for="(value, key) in lookupVals.timezones"
                  :key="key"
                  :value="key"
                >
                  {{ value }}
                </option>
              </select>
            </td>
            <td class="description">{{ $t("weblogConfig.tip.timezone") }}</td>
          </tr>

          <tr v-if="webloggerProps.usersOverrideAnalyticsCode">
            <td class="label">
              {{ $t("weblogConfig.analyticsTrackingCode") }}
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
              {{ $t("weblogConfig.tip.analyticsTrackingCode") }}
            </td>
          </tr>
        </tbody>

        <tbody v-if="webloggerProps.commentPolicy != 'NONE'">
          <tr>
            <td colspan="3">
              <h2>{{ $t("weblogConfig.commentSettings") }}</h2>
            </td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.allowComments") }}</td>
            <td class="field">
              <select v-model="weblog.allowComments" size="1">
                <option
                  v-for="commentoption in filteredCommentOptions"
                  :value="commentoption.constant"
                  :key="commentoption.constant"
                >
                  {{ $t(commentoption.label) }}
                </option>
              </select>
            </td>
          </tr>
        </tbody>

        <tbody v-show="commentingActive">
          <tr>
            <td class="label">{{ $t("weblogConfig.defaultCommentDays") }}</td>
            <td class="field">
              <select v-model="weblog.defaultCommentDays" size="1">
                <option
                  v-for="(value, key) in lookupVals.commentDayOptions"
                  :key="key"
                  :value="key"
                >
                  {{ $t(value) }}
                </option>
              </select>
            </td>
          </tr>

          <tr v-if="weblogId">
            <td class="label">{{ $t("weblogConfig.applyCommentDefaults") }}</td>
            <td class="field">
              <input type="checkbox" v-model="weblog.applyCommentDefaults" />
            </td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.spamPolicy") }}</td>
            <td class="field">
              <select v-model="weblog.spamPolicy" size="1">
                <option
                  v-for="spamoption in filteredSpamOptions"
                  :key="spamoption.constant"
                  :value="spamoption.constant"
                >
                  {{ $t(spamoption.label) }}
                </option>
              </select>
            </td>
            <td class="description">{{ $t("weblogConfig.tip.spamPolicy") }}</td>
          </tr>

          <tr>
            <td class="label">{{ $t("weblogConfig.ignoreUrls") }}</td>
            <td class="field">
              <textarea
                v-model="weblog.blacklist"
                rows="7"
                cols="70"
              ></textarea>
            </td>
            <td class="description">{{ $t("weblogConfig.tip.ignoreUrls") }}</td>
          </tr>
        </tbody>
      </table>

      <div class="control">
        <span>
          <button type="button" class="buttonBox" v-on:click="updateWeblog()">
            {{ $t(varText.saveButtonText) }}
          </button>
          <button type="button" class="buttonBox" v-on:click="cancelChanges()">
            {{ $t("common.cancel") }}
          </button>
        </span>

        <span v-if="weblogId" style="float: right">
          <button v-b-modal.modal-delete class="buttonbox">
            {{ $t("weblogConfig.deleteWeblogButton") }}
          </button>
        </span>
      </div>

      <!-- Delete weblog modal -->
      <div tabindex="-1" role="dialog">
        <b-modal
          id="modal-delete"
          centered
          ok-variant="danger"
          @ok="removeWeblog()"
        >
          <template #modal-title>
            <div
              v-html="
                $t('weblogConfig.deleteConfirm', {
                  handle: weblog.handle,
                })
              "
            ></div>
          </template>
          <div
            v-html="$t('weblogConfig.deleteWarning')"
            class="text-danger"
          ></div>
          <br />
          <div>
            {{
              $t("weblogConfig.deleteInstruction", {
                handle: weblog.handle,
              })
            }}
          </div>
          <div>
            <label for="newTag">{{ $t("weblogConfig.handle") }}:</label>
            <input v-model="deleteHandle" type="text" />
          </div>
          <template #modal-ok>
            {{ $t("common.delete") }}
          </template>
          <template #modal-cancel>
            {{ $t("common.cancel") }}
          </template>
        </b-modal>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";
import asyncDataStatus from "@/mixins/AsyncDataStatus";

export default {
  props: {
    weblogId: {
      required: false,
      type: String,
    },
  },
  data: function () {
    return {
      weblog: {
        theme: "rolling",
        locale: "en",
        timeZone: "America/New_York",
        editFormat: "HTML",
        allowComments: "NONE",
        spamPolicy: "NO_EMAIL",
        visible: true,
        entriesPerPage: 12,
        defaultCommentDays: -1,
      },
      deleteDialogTitle: null,
      deleteDialogWarning: null,
      deleteDialogInstruction: null,
      deleteHandle: "",
      successMessage: null,
      errorObj: {},
    };
  },
  mixins: [asyncDataStatus],
  computed: {
    ...mapState("sessionInfo", {
      sessionInfo: (state) => state.items,
    }),
    ...mapState("startupConfig", {
      startupConfig: (state) => state.startupConfig,
      lookupVals: (state) => state.lookupValues,
    }),
    ...mapState("dynamicConfig", {
      webloggerProps: (state) => state.webloggerProperties,
    }),
    commentingActive: function () {
      return (
        this.webloggerProps.commentPolicy !== "NONE" &&
        this.weblog.allowComments !== "NONE"
      );
    },
    filteredSpamOptions: function () {
      if (this.lookupVals && this.lookupVals.spamOptionList) {
        return this.lookupVals.spamOptionList.filter(
          (item) => item.level >= this.webloggerProps.spamPolicyLevel
        );
      } else {
        return [];
      }
    },
    filteredCommentOptions: function () {
      if (this.lookupVals && this.lookupVals.commentOptionList) {
        return this.lookupVals.commentOptionList.filter(
          (item) => item.level <= this.webloggerProps.commentPolicyLevel
        );
      } else {
        return [];
      }
    },
    varText: function () {
      if (this.weblogId) {
        return {
          saveButtonText: "common.save",
          subtitlePrompt: "weblogConfig.updateWeblogPrompt",
          pageTitleKey: "weblogConfig.updateTitle",
        };
      } else {
        return {
          saveButtonText: "weblogConfig.createWeblogButton",
          subtitlePrompt: "weblogConfig.createWeblogPrompt",
          pageTitleKey: "weblogConfig.createTitle",
        };
      }
    },
  },
  methods: {
    ...mapActions({
      loadStartupConfig: "startupConfig/loadStartupConfig",
      loadLookupValues: "startupConfig/loadLookupValues",
      loadWebloggerProperties: "dynamicConfig/loadWebloggerProperties",
      fetchWeblog: "sessionInfo/fetchWeblog",
      upsertWeblog: "sessionInfo/upsertWeblog",
      deleteWeblog: "sessionInfo/deleteWeblog",
    }),
    updateWeblog: function () {
      this.messageClear();
      const isNew = !this.weblogId;
      this.upsertWeblog({ ...this.weblog })
        .then(() => {
          if (isNew) {
            this.$router.push({ name: "myBlogs" });
          } else {
            this.successMessage = this.$t("common.changesSaved");
            window.scrollTo(0, 0);
          }
        })
        .catch((error) => {
          if (error.response.status === 400) {
            this.errorObj = error.response.data;
            window.scrollTo(0, 0);
          } else {
            this.commonErrorResponse(error);
          }
        });
    },
    removeWeblog: function () {
      if (
        this.weblog.id &&
        this.weblog.handle.toUpperCase() ===
          this.deleteHandle.toUpperCase().trim()
      ) {
        this.deleteWeblog(this.weblogId)
          .then(() => {
            this.$router.push({ name: "myBlogs" });
          })
          .catch((error) => this.commonErrorResponse(error));
      }
    },
    cancelChanges: function () {
      this.messageClear();
      if (this.weblogId) {
        this.loadWeblog();
        window.scrollTo(0, 0);
      } else {
        this.$router.push({ name: "myBlogs" });
      }
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = "/tb-ui/app/login";
      } else {
        this.errorObj = error.response.data;
        window.scrollTo(0, 0);
      }
    },
    messageClear: function () {
      this.errorObj = {};
      this.successMessage = null;
    },
  },
  async created() {
    await this.loadWebloggerProperties();
    await this.loadStartupConfig();
    await this.loadLookupValues();
    if (this.weblogId) {
      await this.fetchWeblog(this.weblogId).then((fetchedWeblog) => {
        this.weblog = { ...fetchedWeblog };
      });
    }
    this.asyncDataStatus_fetched();
  },
};
</script>
