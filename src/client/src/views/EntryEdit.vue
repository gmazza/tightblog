<template>
  <div v-if="asyncDataStatus_ready">
    <AppTitleBar />
    <AppUserNav />
    <div id="sidebar_maincontent_wrap" style="text-align: left; padding: 20px">
      <AppErrorListMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = null"
      ></AppErrorListMessageBox>
      <h2>
        {{ entryId ? $t("entryEdit.editTitle") : $t("entryEdit.addTitle") }}
      </h2>

      <table
        class="entryEditTable"
        cellpadding="0"
        cellspacing="0"
        style="width: 100%"
      >
        <tr>
          <td class="entryEditFormLabel">
            <label for="title">{{ $t("entryEdit.entryTitle") }}</label>
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
            {{ $t("entryEdit.status") }}
          </td>
          <td v-cloak>
            <span
              v-show="entry.status == 'PUBLISHED'"
              style="color: green; font-weight: bold"
            >
              {{ $t("entryEdit.published") }} ({{ $t("entryEdit.updateTime") }}
              {{ entry.updateTime | standard_datetime }})
            </span>
            <span
              v-show="entry.status == 'DRAFT'"
              style="color: orange; font-weight: bold"
            >
              {{ $t("entryEdit.draft") }} ({{ $t("entryEdit.updateTime") }}
              {{ entry.updateTime | standard_datetime }})
            </span>
            <span
              v-show="entry.status == 'SCHEDULED'"
              style="color: orange; font-weight: bold"
            >
              {{ $t("entryEdit.scheduled") }} ({{ $t("entryEdit.updateTime") }}
              {{ entry.updateTime | standard_datetime }})
            </span>
            <span v-show="!entry.status" style="color: red; font-weight: bold">
              {{ $t("entryEdit.unsaved") }}
            </span>
          </td>
        </tr>

        <tr v-show="entry.id" v-cloak>
          <td class="entryEditFormLabel">
            <label for="permalink">{{ $t("entryEdit.permalink") }}</label>
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
            <label for="categoryId">{{ $t("common.category") }}</label>
          </td>
          <td v-show="entry" v-cloak>
            <select
              id="categoryId"
              v-model="entry.category.name"
              size="1"
              required
            >
              <option
                v-for="cat in weblog.weblogCategoryNames"
                :value="cat"
                :key="cat"
              >
                {{ cat }}
              </option>
            </select>
          </td>
        </tr>

        <tr>
          <td class="entryEditFormLabel">
            <label for="tags">{{ $t("common.tags") }}</label>
          </td>
          <td>
            <b-form-tags input-id="tags" v-model="entry.tags"></b-form-tags>
          </td>
        </tr>

        <tr v-cloak>
          <td class="entryEditFormLabel">
            <label for="title">{{ $t("entryEdit.editFormat") }}</label>
          </td>
          <td v-cloak>
            <select v-model="entry.editFormat" size="1" required>
              <option
                v-for="(value, key) in lookupVals.editFormats"
                :value="key"
                :key="key"
              >
                {{ $t(value) }}
              </option>
            </select>
          </td>
        </tr>
      </table>

      <!-- Weblog editor -->

      <p class="toplabel"></p>

      <div class="accordion" role="tablist">
        <b-card no-body class="mb-1">
          <b-card-header header-tag="header" class="p-1" role="tab">
            <b-button block v-b-toggle.accordion-1 variant="info">{{
              $t("entryEdit.content")
            }}</b-button>
          </b-card-header>
          <b-collapse
            id="accordion-1"
            visible
            accordion="my-accordion"
            role="tabpanel"
          >
            <b-card-body>
              <div>
                <textarea
                  id="edit_content"
                  cols="75"
                  rows="25"
                  style="width: 100%"
                  v-model="entry.text"
                ></textarea>
              </div>
            </b-card-body>
          </b-collapse>
        </b-card>

        <b-card no-body class="mb-1">
          <b-card-header header-tag="header" class="p-1" role="tab">
            <b-button block v-b-toggle.accordion-2 variant="info"
              >{{ $t("entryEdit.summary") }}
              <AppHelpPopup :helpText="$t('entryEdit.tooltip.summary')" />
            </b-button>
          </b-card-header>
          <b-collapse
            id="accordion-2"
            visible
            accordion="my-accordion"
            role="tabpanel"
          >
            <b-card-body>
              <div>
                <textarea
                  id="edit_summary"
                  cols="75"
                  rows="10"
                  style="width: 100%"
                  v-model="entry.summary"
                ></textarea>
              </div>
            </b-card-body>
          </b-collapse>
        </b-card>

        <b-card no-body class="mb-1">
          <b-card-header header-tag="header" class="p-1" role="tab">
            <b-button block v-b-toggle.accordion-3 variant="info"
              >{{ $t("entryEdit.notes") }}
              <AppHelpPopup :helpText="$t('entryEdit.tooltip.notes')" />
            </b-button>
          </b-card-header>
          <b-collapse
            id="accordion-3"
            visible
            accordion="my-accordion"
            role="tabpanel"
          >
            <b-card-body>
              <div>
                <div>
                  <textarea
                    id="edit_notes"
                    cols="75"
                    rows="10"
                    style="width: 100%"
                    v-model="entry.notes"
                  ></textarea>
                </div>
              </div>
            </b-card-body>
          </b-collapse>
        </b-card>
      </div>

      <!-- advanced settings  -->

      <div class="controlToggle">
        {{ $t("entryEdit.miscSettings") }}
      </div>

      <label for="link">{{ $t("entryEdit.specifyPubTime") }}:</label>
      <div>
        <input type="number" min="0" max="23" step="1" v-model="entry.hours" />
        :
        <input
          type="number"
          min="0"
          max="59"
          step="1"
          v-model="entry.minutes"
        />
        &nbsp;&nbsp;
        <b-form-datepicker
          id="publish-date-picker"
          v-model="entry.dateString"
          class="mb-2"
          reset-button
        ></b-form-datepicker>
        {{ weblog.timeZone }}
      </div>
      <br />

      <span v-show="commentingActive">
        {{ $t("entryEdit.allowComments") }} {{ $t("entryEdit.commentDays") }}
        <select
          id="commentDaysId"
          v-model="entry.commentDays"
          size="1"
          required
        >
          <option
            v-for="(value, key) in lookupVals.commentDayOptions"
            :value="key"
            :key="key"
          >
            {{ $t(value) }}
          </option>
        </select>
        <br />
      </span>

      <br />

      <table>
        <tr>
          <td>
            {{ $t("entryEdit.searchDescription") }}:<AppHelpPopup
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
            {{ $t("entryEdit.enclosureURL") }}:<AppHelpPopup
              :helpText="$t('entryEdit.tooltip.enclosureURL')"
            />
          </td>
          <td>
            <input
              type="text"
              style="width: 100%"
              maxlength="255"
              v-model="entry.enclosureUrl"
            />
          </td>
        </tr>
        <tr v-show="entryId">
          <td></td>
          <td>
            <span v-show="entry.enclosureType">
              {{ $t("entryEdit.enclosureType") }}:
              {{ entry.enclosureType }}
            </span>
            <span v-show="entry.enclosureLength">
              {{ $t("entryEdit.enclosureLength") }}:
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
            {{ $t("entryEdit.save") }}
          </button>
          <span v-show="entry.id">
            <button type="button" v-on:click="previewEntry()">
              {{ $t("entryEdit.fullPreviewMode") }}
            </button>
          </span>
          <span>
            <button type="button" v-on:click="saveEntry('PUBLISHED')">
              {{ $t("entryEdit.post") }}
            </button>
          </span>
        </span>

        <span style="float: right" v-show="entryId">
          <button type="button" v-on:click="deleteWeblogEntry(entry)">
            {{ $t("entryEdit.deleteEntry") }}
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
                <h3>{{ $t("entryEdit.comments") }}</h3>

                <div v-show="entry.commentCountIncludingUnapproved > 0">
                  <router-link
                    :to="{
                      name: 'comments',
                      params: {
                        weblogId: weblogId,
                      },
                      query: {
                        entryId: entryId,
                      },
                    }"
                    >{{ commentCountMsg }}</router-link
                  >
                </div>
                <div v-show="entry.commentCountIncludingUnapproved == 0">
                  {{ $t("common.none") }}
                </div>

                <div
                  v-show="recentEntries.DRAFT && recentEntries.DRAFT.length > 0"
                >
                  <hr size="1" noshade="noshade" />
                  <h3>
                    {{ $t("entryEdit.draftEntries") }}
                  </h3>

                  <span v-for="post in recentEntries.DRAFT" :key="post.id">
                    <span class="entryEditSidebarLink">
                      <img
                        src="@/assets/table_edit.png"
                        align="absmiddle"
                        border="0"
                        alt="icon"
                        title="Edit"
                      />
                      <router-link
                        :to="{
                          name: 'entryEdit',
                          params: {
                            weblogId: weblogId,
                          },
                          query: {
                            entryId: post.id,
                          },
                        }"
                        >{{ post.title }}</router-link
                      >
                    </span>
                    <br />
                  </span>
                </div>

                <div
                  v-show="
                    recentEntries.PUBLISHED &&
                    recentEntries.PUBLISHED.length > 0
                  "
                >
                  <hr size="1" noshade="noshade" />
                  <h3>
                    {{ $t("entryEdit.publishedEntries") }}
                  </h3>

                  <span v-for="post in recentEntries.PUBLISHED" :key="post.id">
                    <span class="entryEditSidebarLink">
                      <img
                        src="@/assets/table_edit.png"
                        align="absmiddle"
                        border="0"
                        alt="icon"
                        title="Edit"
                      />
                      <router-link
                        :to="{
                          name: 'entryEdit',
                          params: {
                            weblogId: weblogId,
                          },
                          query: {
                            entryId: post.id,
                          },
                        }"
                        >{{ post.title }}</router-link
                      >
                    </span>
                    <br />
                  </span>
                </div>

                <div
                  v-show="
                    recentEntries.SCHEDULED &&
                    recentEntries.SCHEDULED.length > 0
                  "
                >
                  <hr size="1" noshade="noshade" />
                  <h3>
                    {{ $t("entryEdit.scheduledEntries") }}
                  </h3>

                  <span v-for="post in recentEntries.SCHEDULED" :key="post.id">
                    <span class="entryEditSidebarLink">
                      <img
                        src="@/assets/table_edit.png"
                        align="absmiddle"
                        border="0"
                        alt="icon"
                        title="Edit"
                      />
                      <router-link
                        :to="{
                          name: 'entryEdit',
                          params: {
                            weblogId: weblogId,
                          },
                          query: {
                            entryId: post.id,
                          },
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
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";
import asyncDataStatus from "@/mixins/AsyncDataStatus";

export default {
  mixins: [asyncDataStatus],
  props: {
    weblogId: {
      required: true,
      type: String,
    },
    entryId: {
      required: false,
      type: String,
    },
  },
  data() {
    return {
      weblog: {},
      entry: {
        category: {},
      },
      originalEntry: {},
      errorObj: {
        errors: [],
      },
      commentCountMsg: null,
      recentEntries: {
        SCHEDULED: {},
        DRAFT: {},
        PUBLISHED: {},
      },
      urlRoot: import.meta.env.VITE_PUBLIC_PATH + "/authoring/rest/weblogentries/",
    };
  },
  computed: {
    /*    ...mapState("sessionInfo", {
      sessionInfo: (state) => state.items,
    }), */
    ...mapState("startupConfig", {
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
    formIsDirty: function () {
      return (
        this.originalEntry.title !== this.entry.title ||
        this.originalEntry.categoryName !== this.entry.category.name ||
        this.originalEntry.editFormat !== this.entry.editFormat ||
        this.originalEntry.content !== this.entry.content ||
        this.originalEntry.summary !== this.entry.summary ||
        this.originalEntry.notes !== this.entry.notes ||
        this.originalEntry.tags !== this.entry.tags ||
        this.originalEntry.minutes !== this.entry.minutes ||
        this.originalEntry.dateString !== this.entry.dateString ||
        this.originalEntry.commentDays !== this.entry.commentDays ||
        this.originalEntry.searchDescription !== this.entry.searchDescription ||
        this.originalEntry.enclosureUrl !== this.entry.enclosureUrl
      );
    },
  },
  watch: {
    // same-page swaps of the entry (say from most recent entries in sidebar)
    // don't cause lifecycle hooks to reactivate, so watching change of entryId
    // to load new entries as needed.
    async entryId(newValue, oldValue) {
      console.log("EntryId switched from: " + oldValue + " to: " + newValue);
      await this.loadRecentEntries();
      if (this.entryId) {
        this.getEntry();
      } else {
        this.initializeNewEntry();
      }
    },
  },
  methods: {
    ...mapActions({
      fetchWeblog: "sessionInfo/fetchWeblog",
      loadWebloggerProperties: "dynamicConfig/loadWebloggerProperties",
      loadLookupValues: "startupConfig/loadLookupValues",
    }),
    getEntry: function () {
      this.axios.get(this.urlRoot + this.entryId).then((response) => {
        this.entry = response.data;
        this.commentCountMsg = this.$t("entryEdit.hasComments", {
          commentCount: this.entry.commentCountIncludingUnapproved,
        });
        this.entry.commentDays = "" + this.entry.commentDays;
        this.originalEntry = {
          ...this.entry,
          categoryName: this.entry.category.name,
        };
      });
    },
    getRecentEntries: function (entryType) {
      this.axios
        .get(this.urlRoot + this.weblogId + "/recententries/" + entryType)
        .then((response) => {
          this.recentEntries[entryType] = response.data;
        });
    },
    saveEntry: function (saveType) {
      this.messageClear();
      const urlStem = this.weblogId + "/entries";

      const oldStatus = this.entry.status;
      this.entry.status = saveType;

      this.axios
        .post(this.urlRoot + urlStem, this.entry)
        .then((response) => {
          if (!this.entry.id) {
            this.$router.push({
              name: "entryEdit",
              params: { weblogId: this.weblogId },
              query: { entryId: response.data },
            });
          } else {
            this.getEntry();
            this.loadRecentEntries();
            window.scrollTo(0, 0);
          }
        })
        .catch((error) => {
          this.entry.status = oldStatus;
          if (error.response.status === 401) {
            this.errorObj = {
              errors: [
                {
                  message: this.$t("entryEdit.sessionTimedOut", {
                    loginUrl: import.meta.env.VITE_PUBLIC_PATH + "/app/login-redirect",
                  }),
                },
              ],
            };
            window.scrollTo(0, 0);
          } else {
            this.commonErrorResponse(error);
          }
        });
    },
    deleteWeblogEntry: function () {
      this.messageClear();
      const h = this.$createElement;
      const messageVNode = h("div", {
        domProps: {
          innerHTML: this.$t("entryEdit.confirmDeleteTmpl", {
            title: this.entry.title,
          }),
        },
      });
      this.$bvModal
        .msgBoxConfirm([messageVNode], {
          okTitle: this.$t("common.confirm"),
          cancelTitle: this.$t("common.cancel"),
          centered: true,
        })
        .then((value) => {
          if (value) {
            this.axios
              .delete(this.urlRoot + this.entryId)
              .then(() => {
                this.$router.push({
                  name: "entryEdit",
                  params: { weblogId: this.weblogId },
                });
              })
              .catch((error) => this.commonErrorResponse(error));
          }
        });
    },
    loadRecentEntries: function () {
      this.getRecentEntries("DRAFT");
      this.getRecentEntries("PUBLISHED");
      this.getRecentEntries("SCHEDULED");
    },
    previewEntry: function () {
      window.open(this.entry.previewUrl);
    },
    updateTags: function (tagsString) {
      this.entry.tagsAsString = tagsString;
    },
    updatePublishDate: function (date) {
      this.entry.dateString = date;
    },
    messageClear: function () {
      this.errorObj = {};
    },
    initializeNewEntry: function () {
      this.entry = {
        commentCountIncludingUnapproved: 0,
        commentDays: "" + this.weblog.defaultCommentDays,
        editFormat: this.weblog.editFormat,
        category: {},
      };
      this.entry.category.name = this.weblog.weblogCategoryNames[0];
      this.originalEntry = {
        ...this.entry,
        categoryName: this.entry.category.name,
      };
      window.scrollTo(0, 0);
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + "/app/login";
      } else {
        this.errorObj = error.response.data;
        window.scrollTo(0, 0);
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
    // indicate requests via Ajax calls, so auth problems return 401s vs. login redirects
    this.axios.defaults.headers.common["X-Requested-With"] = "XMLHttpRequest";
    await this.loadWebloggerProperties();
    await this.loadLookupValues();
    await this.fetchWeblog(this.weblogId).then((fetchedWeblog) => {
      this.weblog = { ...fetchedWeblog };
    });
    await this.loadRecentEntries();
    if (this.entryId) {
      await this.getEntry();
    } else {
      this.initializeNewEntry();
    }
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
    if (to.path != from.path || (from.query.entryId && (to.query.entryId != from.query.entryId))) {
        this.checkDirty(to, from, next);
    } else {
        next();
    }
  },
};
</script>

<style></style>
