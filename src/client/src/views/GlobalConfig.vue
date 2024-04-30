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
    <AppAdminNav />
    <div style="text-align: left; padding: 20px">
      <AppSuccessMessageBox
        :message="successMessage"
        @close-box="successMessage = null"
      />
      <AppErrorMessageBox
        :message="errorMessage"
        @close-box="errorMessage = null"
      />

      <p class="subtitle">{{ $t("globalConfig.subtitle") }}</p>

      <p>{{ $t("globalConfig.prompt") }}</p>

      <table v-if="webloggerProps != null" class="formtable">
        <thead>
          <tr>
            <td colspan="3">
              <h2>{{ $t("globalConfig.siteSettings") }}</h2>
            </td>
          </tr>
          <tr>
            <td class="label">
              {{ $t("globalConfig.frontpageWeblogHandle") }}
            </td>
            <td class="field">
              <select v-model="webloggerProps.mainBlogId" size="1">
                <option
                  v-for="(value, key) in visibleWeblogs"
                  :key="key"
                  :value="value.id"
                >
                  {{ weblogDescription(value) }}
                </option>
                <option value="">{{ $t("globalConfig.none") }}</option>
              </select>
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.frontpageWeblogHandle") }}
            </td>
          </tr>
          <tr>
            <td class="label">
              {{ $t("globalConfig.requiredRegistrationProcess") }}
            </td>
            <td class="field">
              <select
                v-model="webloggerProps.registrationPolicy"
                size="1"
                required
              >
                <option
                  v-for="(value, key) in lookupVals.registrationOptions"
                  :key="key"
                  :value="key"
                >
                  {{ $t(value) }}
                </option>
              </select>
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.requiredRegistrationProcess") }}
            </td>
          </tr>
          <tr>
            <td class="label">{{ $t("globalConfig.newUsersCreateBlogs") }}</td>
            <td class="field">
              <input
                type="checkbox"
                v-model="webloggerProps.usersCreateBlogs"
              />
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.newUsersCreateBlogs") }}
            </td>
          </tr>
          <tr>
            <td colspan="2">&nbsp;</td>
          </tr>
          <tr>
            <td colspan="3">
              <h2>{{ $t("globalConfig.weblogSettings") }}</h2>
            </td>
          </tr>
          <tr>
            <td class="label">{{ $t("globalConfig.htmlSafelistLevel") }}</td>
            <td class="field">
              <select v-model="webloggerProps.blogHtmlPolicy" size="1" required>
                <option
                  v-for="(value, key) in lookupVals.blogHtmlLevels"
                  :key="key"
                  :value="key"
                >
                  {{ $t(value) }}
                </option>
              </select>
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.htmlSafelistLevel") }}
            </td>
          </tr>
          <tr>
            <td class="label">{{ $t("globalConfig.allowCustomTheme") }}</td>
            <td class="field">
              <input
                type="checkbox"
                v-model="webloggerProps.usersCustomizeThemes"
              />
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.allowCustomTheme") }}
            </td>
          </tr>
          <tr v-if="startupConfig.showMediaFileTab">
            <td class="label">
              {{ $t("globalConfig.maxMediaFileAllocationMb") }}
            </td>
            <td class="field">
              <input
                type="number"
                v-model="webloggerProps.maxFileUploadsSizeMb"
                size="35"
              />
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.maxMediaFileAllocationMb") }}
            </td>
          </tr>
          <tr>
            <td class="label">
              {{ $t("globalConfig.defaultAnalyticsTrackingCode") }}
            </td>
            <td class="field">
              <textarea
                rows="10"
                cols="70"
                v-model="webloggerProps.defaultAnalyticsCode"
              ></textarea>
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.defaultAnalyticsTrackingCode") }}
            </td>
          </tr>
          <tr>
            <td class="label">
              {{ $t("globalConfig.allowAnalyticsCodeOverride") }}
            </td>
            <td class="field">
              <input
                type="checkbox"
                v-model="webloggerProps.usersOverrideAnalyticsCode"
              />
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.allowAnalyticsCodeOverride") }}
            </td>
          </tr>
          <tr>
            <td colspan="2">&nbsp;</td>
          </tr>
          <tr>
            <td colspan="3">
              <h2>{{ $t("globalConfig.commentSettings") }}</h2>
            </td>
          </tr>
          <tr>
            <td class="label">{{ $t("globalConfig.enableComments") }}</td>
            <td class="field">
              <select v-model="webloggerProps.commentPolicy" size="1" required>
                <option
                  v-for="commentoption in lookupVals.commentOptionList"
                  :value="commentoption.constant"
                  :key="commentoption.constant"
                >
                  {{ $t(commentoption.label) }}
                </option>
              </select>
            </td>
            <td class="description"></td>
          </tr>
        </thead>
        <thead v-show="webloggerProps.commentPolicy != 'NONE'">
          <tr>
            <td class="label">
              {{ $t("globalConfig.commentHtmlSafelistLevel") }}
            </td>
            <td class="field">
              <select
                v-model="webloggerProps.commentHtmlPolicy"
                size="1"
                required
              >
                <option
                  v-for="(value, key) in lookupVals.commentHtmlLevels"
                  :key="key"
                  :value="key"
                >
                  {{ $t(value) }}
                </option>
              </select>
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.commentHtmlSafelistLevel") }}
            </td>
          </tr>
          <tr>
            <td class="label">{{ $t("globalConfig.spamPolicy") }}</td>
            <td class="field">
              <select v-model="webloggerProps.spamPolicy" size="1" required>
                <option
                  v-for="spamoption in lookupVals.spamOptionList"
                  :key="spamoption.constant"
                  :value="spamoption.constant"
                >
                  {{ $t(spamoption.label) }}
                </option>
              </select>
            </td>
            <td class="description">{{ $t("globalConfig.tip.spamPolicy") }}</td>
          </tr>
          <tr>
            <td class="label">{{ $t("globalConfig.emailComments") }}</td>
            <td class="field">
              <input
                type="checkbox"
                v-model="webloggerProps.usersCommentNotifications"
              />
            </td>
            <td class="description">
              {{ $t("globalConfig.tip.emailComments") }}
            </td>
          </tr>
          <tr>
            <td class="label">{{ $t("globalConfig.ignoreUrls") }}</td>
            <td class="field">
              <textarea
                rows="7"
                cols="80"
                v-model="webloggerProps.globalSpamFilter"
              ></textarea>
            </td>
            <td class="description">{{ $t("globalConfig.tip.ignoreUrls") }}</td>
          </tr>
        </thead>
        <thead>
          <tr>
            <td colspan="2">&nbsp;</td>
          </tr>
        </thead>
      </table>

      <div class="control">
        <button type="button" class="buttonBox" v-on:click="updateProperties()">
          {{ $t("common.save") }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters } from "vuex";
import asyncDataStatus from "@/mixins/AsyncDataStatus";

export default {
  data() {
    return {
      successMessage: null,
      webloggerProps: null,
      errorMessage: null,
    };
  },
  mixins: [asyncDataStatus],
  computed: {
    ...mapState("dynamicConfig", {
      weblogList: (state) => state.weblogList,
      webloggerProperties: (state) => state.webloggerProperties,
    }),
    ...mapState("startupConfig", {
      startupConfig: (state) => state.startupConfig,
      lookupVals: (state) => state.lookupValues,
    }),
    visibleWeblogs() {
      return this.weblogList.filter((w) => w.visible === true);
    },
  },
  methods: {
    ...mapActions({
      loadWebloggerProperties: "dynamicConfig/loadWebloggerProperties",
      saveWebloggerProperties: "dynamicConfig/saveWebloggerProperties",
      loadWeblogList: "dynamicConfig/loadWeblogList",
      loadStartupConfig: "startupConfig/loadStartupConfig",
      loadLookupValues: "startupConfig/loadLookupValues",
    }),
    loadWebloggerProps: function () {
      this.webloggerProps = JSON.parse(
        JSON.stringify(this.webloggerProperties)
      );
    },
    loadMetadata: function () {
      this.loadStartupConfig().then(
        () => {},
        (error) => this.commonErrorResponse(error, null)
      );
      this.loadLookupValues().then(
        () => {},
        (error) => this.commonErrorResponse(error, null)
      );
    },
    updateProperties: function () {
      this.saveWebloggerProperties(this.webloggerProps).then(
        () => {
          this.loadWebloggerProperties();
          this.loadWebloggerProps();
          this.errorObj = {};
          this.successMessage = this.$t("common.changesSaved");
          window.scrollTo(0, 0);
        },
        (error) => this.commonErrorResponse(error, null)
      );
    },
    weblogDescription: function (weblogItem) {
      return weblogItem.name + " (" + weblogItem.handle + ")";
    },
    commonErrorResponse: function (error, errorMsg) {
      if (errorMsg) {
        this.errorMessage = errorMsg;
      } else if (error && error.response && error.response.status === 401) {
        console.log("Redirecting...");
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + "/app/login";
      } else if (error && error.response) {
        this.errorMessage = error.response.data.error;
      } else if (error) {
        this.errorMessage = error;
      } else {
        this.errorMessage = "System error.";
      }
    },
  },
  async created() {
    await this.loadWebloggerProperties();
    await this.loadStartupConfig();
    await this.loadWeblogList();
    await this.loadLookupValues();
    this.loadWebloggerProps();
    this.asyncDataStatus_fetched();
  },
};
</script>
