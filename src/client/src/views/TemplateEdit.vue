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
  <div v-if="asyncDataStatus_ready">
    <AppTitleBar />

    <div style="text-align: left; padding: 20px">
      <AppSuccessMessageBox
        :message="successMessage"
        @close-box="successMessage = null"
      />
      <AppErrorListMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = null"
      ></AppErrorListMessageBox>
      <h2>{{ $t("templateEdit.title") }}</h2>
      <p class="pagetip" v-html="$t('templateEdit.tip')"></p>

      <table cellspacing="5">
        <tr>
          <td class="label">{{ $t("common.name") }}</td>
          <td class="field">
            <input
              id="name"
              type="text"
              v-model="templateData.name"
              size="50"
              maxlength="255"
              :style="{
                background:
                  templateData.derivation == 'Added' ? 'white' : 'lightgrey',
              }"
              :readonly="templateData.derivation != 'Added'"
            />
            <span v-if="templateLoaded && templateData.role.accessibleViaUrl">
              <br />
              <span v-if="lastSavedName != null">
                [<a id="launchLink" v-on:click="launchPage()">{{
                  $t("templateEdit.launch")
                }}</a
                >]
              </span>
            </span>
          </td>
        </tr>

        <tr v-if="templateLoaded">
          <td class="label">{{ $t("common.role") }}</td>
          <td class="field">
            <span>{{ templateData.role.readableName }}</span>
          </td>
        </tr>

        <tr v-if="templateLoaded && !templateData.role.singleton">
          <td class="label" valign="top" style="padding-top: 4px">
            {{ $t("common.description") }}
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

      <textarea
        v-model="templateData.template"
        rows="20"
        style="width: 100%"
      ></textarea>

      <table style="width: 100%">
        <tr>
          <td>
            <button type="button" @click="saveTemplate()">
              {{ $t("common.saveChanges") }}
            </button>
            <button type="button" @click="cancel()">
              {{ $t("templateEdit.returnToTemplates") }}
            </button>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters } from "vuex";
import asyncDataStatus from "@/mixins/AsyncDataStatus";

export default {
  props: {
    weblogId: {
      required: true,
      type: String,
    },
    templateId: {
      required: true,
      type: String,
    },
    templateName: {
      required: false,
      type: String,
    },
  },
  data() {
    return {
      templateData: {
        role: null,
      },
      originalTemplateData: {},
      lastSavedName: null,
      successMessage: null,
      errorObj: {},
      templateLoaded: false,
      workingTemplateId: null,
    };
  },
  mixins: [asyncDataStatus],
  computed: {
    ...mapState("sessionInfo", {
      sessionInfo: (state) => state.items,
    }),
    ...mapGetters({
      // weblog guaranteed loaded because this page entered
      // thru templates page which explicitly loads it
      getWeblog: "sessionInfo/weblog",
    }),
    formIsDirty: function () {
        return (
            this.originalTemplateData.description !== this.templateData.description ||
            this.originalTemplateData.template !== this.templateData.template
        );
    },
  },
  methods: {
    launchPage: function () {
      window.open(
        this.sessionInfo.absoluteUrl +
          "/" +
          this.getWeblog(this.weblogId).handle +
          "/page/" +
          this.lastSavedName,
        "_blank"
      );
    },
    loadTemplate: function () {
      let urlStem;
      if (this.workingTemplateId && this.workingTemplateId.indexOf(":") < 0) {
        urlStem = "/tb-ui/authoring/rest/template/" + this.workingTemplateId;
      } else {
        urlStem =
          "/tb-ui/authoring/rest/weblog/" +
          this.weblogId +
          "/templatename/" +
          this.templateName +
          "/";
      }
      this.axios.get(urlStem).then((response) => {
        this.templateData = response.data;
        this.lastSavedName = this.templateData.name;
        this.originalTemplateData = {
            ...this.templateData
        };
        this.templateLoaded = true;
      });
    },
    saveTemplate: function () {
      this.messageClear();
      const urlStem =
        "/tb-ui/authoring/rest/weblog/" + this.weblogId + "/templates";

      this.axios
        .post(urlStem, this.templateData)
        .then((response) => {
          this.workingTemplateId = response.data;
          this.loadTemplate();
          this.successMessage = this.$t("common.changesSaved");
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    cancel() {
      this.$router.push({
        name: "templates",
        params: { weblogId: this.weblogId },
      });
    },
    messageClear: function () {
      this.successMessage = null;
      this.errorObj = {};
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = "/tb-ui/app/login";
      } else {
        this.errorObj = error.response.data;
      }
  },
  checkDirty(to, from, next) {
      if (this.formIsDirty) {
        this.$bvModal
          .msgBoxConfirm(this.$t("common.confirmLeaveNoSave"), {
            okTitle: this.$t("common.confirm"),
            cancelTitle: this.$t("common.cancel"),
            centered: true,
          })
          .then((value) => {
            if (value) {
              next();
            }
          });
      } else {
        next();
      }
  },
  },
  async created() {
    this.workingTemplateId = this.templateId;
    await this.loadTemplate();
    this.asyncDataStatus_fetched();
  },
  beforeRouteLeave(to, from, next) {
    if (to.path != from.path) {
       this.checkDirty(to, from, next);
    } else {
       next();
    }
  },
  beforeRouteUpdate(to, from, next) {
    if (to.path != from.path) {
       this.checkDirty(to, from, next);
    } else {
       next();
    }
  },
};
</script>

<style></style>
