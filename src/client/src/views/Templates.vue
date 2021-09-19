<template>
  <div>
    <AppUserNav />
    <div style="text-align: left; padding: 20px">
      <AppSuccessMessageBox
        :message="successMessage"
        @close-box="successMessage = null"
      />
      <AppErrorListMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = null"
      ></AppErrorListMessageBox>
      <h2>{{ $t("templates.title") }}</h2>
      <p class="pagetip" v-html="$t('templates.tip')"></p>

      <div class="control clearfix">
        <span style="padding-left: 7px">
          {{ $t("templates.currentTheme") }}:
          <b>{{ weblog.theme }}</b>
        </span>

        <span style="float: right" v-show="switchToThemes.length > 0">
          <button type="button" v-on:click="switchTheme()">
            {{ $t("templates.switchTheme") }}
          </button>

          <select
            id="switchThemeMenu"
            size="1"
            required
            v-model="targetThemeId"
          >
            <option
              v-for="theme in switchToThemes"
              :key="theme.id"
              :value="theme.id"
            >
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
                v-on:input="toggleCheckboxes($event.target.checked)"
                title="$t('templates.selectAllLabel')"
              />
            </th>
            <th width="17%">
              {{ $t("common.name") }}
            </th>
            <th width="16%">
              {{ $t("common.role") }}
            </th>
            <th width="38%">
              {{ $t("common.description") }}
            </th>
            <th width="8%">
              {{ $t("templates.source") }}
              <img
                src="@/assets/help.png"
                border="0"
                alt="icon"
                :title="$t('templates.sourceTooltip')"
              />
            </th>
            <th width="13%">
              {{ $t("common.lastModified") }}
            </th>
            <th width="4%">
              {{ $t("common.view") }}
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
                      weblogId,
                      templateId: tpl.id,
                      templateName: tpl.name,
                    },
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
                  tpl.role.singleton != true &&
                  tpl.description != null &&
                  tpl.description != ''
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
                {{ tpl.lastModified | standard_datetime }}
              </span>
            </td>

            <td class="buttontd">
              <span v-if="tpl.role.accessibleViaUrl">
                <a
                  target="_blank"
                  v-bind:href="
                    sessionInfo.absoluteUrl +
                    '/' +
                    weblog.handle +
                    '/page/' +
                    tpl.name
                  "
                >
                  <img src="@/assets/world_go.png" border="0" alt="icon" />
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
            v-on:click="deleteTemplates()"
          >
            {{ $t("common.deleteSelected") }}
          </button>
        </span>
      </div>

      <div class="menu-tr sidebarFade">
        <div class="sidebarInner">
          <form name="myform">
            <div>
              {{ $t("templates.createNewTemplate") }}
            </div>
            <table cellpadding="0" cellspacing="6">
              <tr>
                <td>
                  {{ $t("common.name") }}
                </td>
                <td>
                  <input
                    type="text"
                    v-model="newTemplateName"
                    maxlength="40"
                    required
                  />
                </td>
              </tr>
              <tr>
                <td>
                  {{ $t("common.role") }}
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
                  <p>
                    {{ $t(selectedTemplateRole.descriptionProperty) }}
                  </p>
                </td>
              </tr>
              <tr>
                <td>
                  <button type="button" v-on:click="addTemplate()">
                    {{ $t("common.add") }}
                  </button>
                </td>
              </tr>
            </table>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";

export default {
  props: {
    weblogId: {
      required: true,
      type: String,
    },
  },
  data() {
    return {
      weblog: {},
      weblogTemplateData: {
        themes: [],
        templates: [],
        availableTemplateRoles: [],
      },
      allFilesSelected: false,
      newTemplateName: "",
      newTemplateRole: null,
      deleteDialogTitle: null,
      switchThemeTitle: null,
      targetThemeId: null,
      successMessage: null,
      errorObj: {},
    };
  },
  computed: {
    ...mapState("sessionInfo", {
      sessionInfo: (state) => state.items,
    }),
    selectedTemplateRole: function () {
      const tempRoleCopy = this.newTemplateRole;
      if (!tempRoleCopy) {
        return null;
      }
      return this.weblogTemplateData.availableTemplateRoles.filter(function (
        atr
      ) {
        return atr.constant === tempRoleCopy;
      })[0];
    },
    switchToThemes: function () {
      const weblog = this.weblog;
      return this.weblogTemplateData.themes.filter(function (theme) {
        return theme.id !== weblog.theme;
      });
    },
    targetThemeName: function () {
      // extra variable to activate value refreshing
      const themeIdToCompare = this.targetThemeId;
      const targetTheme = this.weblogTemplateData.themes.filter(function (
        theme
      ) {
        return theme.id === themeIdToCompare;
      });
      return targetTheme[0].name;
    },
    templatesSelectedCount: function () {
      return this.weblogTemplateData.templates.filter((tmpl) => tmpl.selected)
        .length;
    },
  },
  methods: {
    ...mapActions({
      fetchWeblog: "sessionInfo/fetchWeblog",
      upsertWeblog: "sessionInfo/upsertWeblog",
      refreshWeblog: "sessionInfo/refreshWeblog",
    }),
    loadTemplateData: function () {
      this.axios
        .get("/tb-ui/authoring/rest/weblog/" + this.weblogId + "/templates")
        .then((response) => {
          this.weblogTemplateData = response.data;
          this.allFilesSelected = false;
          this.targetThemeId = this.switchToThemes[0].id;
        });
    },
    addTemplate: function () {
      this.messageClear();
      if (!this.newTemplateName) {
        return;
      }
      const newData = {
        name: this.newTemplateName,
        roleName: this.newTemplateRole,
        template: "",
      };
      this.axios
        .post(
          "/tb-ui/authoring/rest/weblog/" + this.weblogId + "/templates",
          newData
        )
        .then((response) => {
          this.successMessage = this.$t("common.changesSaved");
          this.loadTemplateData();
          this.resetAddTemplateData();
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    switchTheme: function () {
      this.messageClear();
      // https://bootstrap-vue.org/docs/components/modal#message-box-advanced-usage
      const h = this.$createElement;
      const messageVNode = h("div", {
        domProps: {
          innerHTML: this.$t("templates.switchWarning"),
        },
      });
      this.$bvModal
        .msgBoxConfirm([messageVNode], {
          title: this.$t("templates.confirmSwitchTemplate", {
            targetThemeName: this.targetThemeName,
          }),
          okTitle: this.$t("common.confirm"),
          cancelTitle: this.$t("common.cancel"),
          centered: true,
        })
        .then((value) => {
          if (value) {
            this.axios
              .post(
                "/tb-ui/authoring/rest/weblog/" +
                  this.weblogId +
                  "/switchtheme/" +
                  this.targetThemeId
              )
              .then((response) => {
                this.refreshWeblog(this.weblogId).then((refreshedWeblog) => {
                  this.weblog = { ...refreshedWeblog };
                });
                this.loadTemplateData();
                this.successMessage = this.$t("common.changesSaved");
              })
              .catch((error) => this.commonErrorResponse(error));
          }
        });
    },
    toggleCheckboxes: function (checkAll) {
      this.weblogTemplateData.templates
        .filter((tmpl) => !this.isBuiltIn(tmpl))
        .forEach((tmpl) => {
          this.$set(tmpl, "selected", checkAll);
        });
    },
    deleteTemplates: function () {
      this.messageClear();
      // https://bootstrap-vue.org/docs/components/modal#message-box-advanced-usage
      const h = this.$createElement;
      const messageVNode = h("div", {
        domProps: {
          innerHTML: this.$t("templates.deleteWarning"),
        },
      });
      this.$bvModal
        .msgBoxConfirm([messageVNode], {
          title: this.$t("templates.confirmDeleteTemplate", {
            count: this.templatesSelectedCount,
          }),
          okTitle: this.$t("common.confirm"),
          cancelTitle: this.$t("common.cancel"),
          centered: true,
        })
        .then((value) => {
          if (value) {
            const selectedItems = [];
            this.weblogTemplateData.templates.forEach((template) => {
              if (template.selected) selectedItems.push(template.id);
            });

            this.axios
              .post(
                "/tb-ui/authoring/rest/weblog/" +
                  this.weblogId +
                  "/templates/delete",
                selectedItems
              )
              .then((response) => {
                this.successMessage = this.$t("common.changesSaved");
                this.loadTemplateData();
              })
              .catch((error) => this.commonErrorResponse(error));
          }
        });
    },
    isBuiltIn: function (template) {
      return template && template.derivation === "Built-In";
    },
    resetAddTemplateData: function () {
      this.newTemplateName = "";
      this.newTemplateRole = "CUSTOM_EXTERNAL";
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = "/tb-ui/app/login";
      } else {
        this.errorObj = error.response.data;
      }
    },
    messageClear: function () {
      this.successMessage = null;
      this.errorObj = {};
    },
  },
  created: function () {
    this.fetchWeblog(this.weblogId).then((fetchedWeblog) => {
      this.weblog = { ...fetchedWeblog };
    });
    this.loadTemplateData();
    this.resetAddTemplateData();
  },
};
</script>

<style></style>
