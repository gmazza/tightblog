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
  <div style="text-align: left; padding: 20px">
    <AppSuccessMessageBox :message="successMessage" @close-box="successMessage = null" />
    <AppErrorMessageBox :message="errorMessage" @close-box="errorMessage = null" />

    <p class="subtitle">{{ $t("globalConfig.subtitle") }}</p>

    <p>{{ $t("globalConfig.prompt") }}</p>

    <table class="formtable">

        <tr>
            <td colspan="3"><h2>{{ $t("globalConfig.siteSettings") }}</h2></td>
        </tr>
        <tr>
            <td class="label">{{ $t("globalConfig.frontpageWeblogHandle") }}</td>
            <td class="field">
                <select v-model="webloggerProps.mainBlogId" size="1">
                    <option v-for="(value, key) in metadata.weblogList" :key="key" :value="key">{{value}}</option>
                    <option value="">{{ $t("globalConfig.none") }}</option>
                </select>
            </td>
            <td class="description">{{ $t("globalConfig.tip.frontpageWeblogHandle") }}</td>
        </tr>
        <tr>
            <td class="label">{{ $t("globalConfig.requiredRegistrationProcess") }}</td>
            <td class="field">
                <select v-model="webloggerProps.registrationPolicy" size="1" required>
                    <option v-for="(value, key) in metadata.registrationOptions" :key="key" :value="key">{{value}}</option>
                </select>
            </td>
            <td class="description">{{ $t("globalConfig.tip.requiredRegistrationProcess") }}</td>
        </tr>
        <tr>
            <td class="label">{{ $t("globalConfig.newUsersCreateBlogs") }}</td>
            <td class="field"><input type="checkbox" v-model="webloggerProps.usersCreateBlogs"></td>
            <td class="description">{{ $t("globalConfig.tip.newUsersCreateBlogs") }}</td>
        </tr>
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        <tr>
            <td colspan="3"><h2>{{ $t("globalConfig.weblogSettings") }}</h2></td>
        </tr>
            <tr>
                  <td class="label">{{ $t("globalConfig.htmlWhitelistLevel") }}</td>
                  <td class="field">
                      <select v-model="webloggerProps.blogHtmlPolicy" size="1" required>
                          <option v-for="(value, key) in metadata.blogHtmlLevels" :key="key" :value="key">{{value}}</option>
                      </select>
                  </td>
                  <td class="description">{{ $t("globalConfig.tip.htmlWhitelistLevel") }}</td>
            </tr>
            <tr>
                <td class="label">{{ $t("globalConfig.allowCustomTheme") }}</td>
                <td class="field"><input type="checkbox" v-model="webloggerProps.usersCustomizeThemes"></td>
                <td class="description">{{ $t("globalConfig.tip.allowCustomTheme") }}</td>
            </tr>
            <tr v-if="metadata.showMediaFileTab">
                <td class="label">{{ $t("globalConfig.maxMediaFileAllocationMb") }}</td>
                <td class="field"><input type="number" v-model="webloggerProps.maxFileUploadsSizeMb" size='35'></td>
                <td class="description">{{ $t("globalConfig.tip.maxMediaFileAllocationMb") }}</td>
            </tr>
            <tr>
                <td class="label">{{ $t("globalConfig.defaultAnalyticsTrackingCode") }}</td>
                <td class="field"><textarea rows="10" cols="70" v-model="webloggerProps.defaultAnalyticsCode"></textarea></td>
                <td class="description">{{ $t("globalConfig.tip.defaultAnalyticsTrackingCode") }}</td>
            </tr>
            <tr>
                <td class="label">{{ $t("globalConfig.allowAnalyticsCodeOverride") }}</td>
                <td class="field"><input type="checkbox" v-model="webloggerProps.usersOverrideAnalyticsCode"></td>
                <td class="description">{{ $t("globalConfig.tip.allowAnalyticsCodeOverride") }}</td>
            </tr>
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        <tr>
            <td colspan="3"><h2>{{ $t("globalConfig.commentSettings") }}</h2></td>
        </tr>
            <tr>
                <td class="label">{{ $t("globalConfig.enableComments") }}</td>
                <td class="field">
                    <select v-model="webloggerProps.commentPolicy" size="1" required>
                        <option v-for="(value, key) in metadata.commentOptions" :key="key" :value="key">{{value}}</option>
                    </select>
                </td>
                <td class="description"></td>
            </tr>
            <tr v-show="webloggerProps.commentPolicy != 'NONE'">
                <td class="label">{{ $t("globalConfig.commentHtmlWhitelistLevel") }}</td>
                <td class="field">
                    <select v-model="webloggerProps.commentHtmlPolicy" size="1" required>
                        <option v-for="(value, key) in metadata.commentHtmlLevels" :key="key" :value="key">{{value}}</option>
                    </select>
                </td>
                <td class="description">{{ $t("globalConfig.tip.commentHtmlWhitelistLevel") }}</td>
            </tr>
            <tr v-show="webloggerProps.commentPolicy != 'NONE'">
                <td class="label">{{ $t("globalConfig.spamPolicy") }}</td>
                <td class="field">
                    <select v-model="webloggerProps.spamPolicy" size="1" required>
                        <option v-for="(value, key) in metadata.spamOptions" :key="key" :value="key">{{value}}</option>
                    </select>
                </td>
                <td class="description">{{ $t("globalConfig.tip.spamPolicy") }}</td>
            </tr>
            <tr v-show="webloggerProps.commentPolicy != 'NONE'">
                <td class="label">{{ $t("globalConfig.emailComments") }}</td>
                <td class="field"><input type="checkbox" v-model="webloggerProps.usersCommentNotifications"></td>
                <td class="description">{{ $t("globalConfig.tip.emailComments") }}</td>
            </tr>
            <tr v-show="webloggerProps.commentPolicy != 'NONE'">
                <td class="label">{{ $t("globalConfig.ignoreUrls") }}</td>
                <td class="field"><textarea rows="7" cols="80" v-model="webloggerProps.globalSpamFilter"></textarea></td>
                <td class="description">{{ $t("globalConfig.tip.ignoreUrls") }}</td>
            </tr>
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
    </table>

    <div class="control">
        <button type="button" class="buttonBox" v-on:click="updateProperties()">
            {{ $t("common.save") }}
        </button>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";

export default {
  data() {
    return {
      urlRoot: "/tb-ui/admin/rest/server/",
      successMessage: null,
      webloggerProps: {},
      errorMessage: null
    };
  },
  computed: {
    ...mapState("globalConfig", {
      storeWebloggerProps: state => state.items,
      metadata: state => state.metadata
    })
  },
  methods: {
    ...mapActions({
      loadGlobalConfig: "globalConfig/loadGlobalConfig",
      saveGlobalConfig: "globalConfig/saveGlobalConfig",
      loadMetadata: "globalConfig/loadMetadata"
    }),
    loadWebloggerProperties: function() {
      this.loadGlobalConfig().then(
        () => {
          this.webloggerProps = this.storeWebloggerProps;
        },
        error => this.commonErrorResponse(error, null)
      );
    },
    loadGlobalMetadata: function() {
      this.loadMetadata().then(
        () => {},
        error => this.commonErrorResponse(error, null)
      );
    },
    updateProperties: function() {
      this.saveGlobalConfig(this.webloggerProps).then(
        () => {
          this.webloggerProps = this.storeWebloggerProps;
          this.errorObj = {};
          this.successMessage = this.$t("common.changesSaved");
          window.scrollTo(0, 0);
        },
        error => this.commonErrorResponse(error, null)
      );
    },
    commonErrorResponse: function(error, errorMsg) {
      if (errorMsg) {
        this.errorMessage = errorMsg;
      } else if (error && error.response && error.response.status == 401) {
        console.log("Redirecting...");
        window.location.href = "/tb-ui/app/login";
      } else if (error && error.response) {
        this.errorMessage = error.response.data.error;
      } else if (error) {
        this.errorMessage = error;
      } else {
        this.errorMessage = "System error.";
      }
    }
  },
  mounted: function() {
    this.loadGlobalMetadata();
    this.loadWebloggerProperties();
  }
}
</script>
