<template>
  <div style="text-align: left; padding: 20px">
    <AppUserNav />
    <AppErrorListMessageBox
      :in-error-obj="errorObj"
      @close-box="errorObj.errors = null"
    ></AppErrorListMessageBox>
    <h2>{{ $t("comments.title") }}</h2>
    <p class="pagetip">
      {{ $t("comments.tip") }}
    </p>
    <div class="sidebarFade">
      <div class="menu-tr">
        <div class="menu-tl">
          <div class="sidebarInner">
            <h3>{{ $t("comments.sidebarTitle") }}</h3>
            <hr size="1" noshade="noshade" />

            <p>{{ $t("comments.sidebarDescription") }}</p>

            <div class="sideformrow">
              <label for="searchText" class="sideformrow"
                >{{ $t("comments.searchString") }}:</label
              >
              <input
                id="searchText"
                type="text"
                v-model="searchParams.searchText"
                size="30"
              />
            </div>
            <br />
            <br />

            <div class="sideformrow">
              <label for="startDateString" class="sideformrow"
                >{{ $t("comments.label.startDate") }}:</label
              >
              <b-form-datepicker
                id="start-date-picker"
                v-model="searchParams.startDateString"
                class="mb-2"
                reset-button
              ></b-form-datepicker>
            </div>

            <div class="sideformrow">
              <label for="endDateString" class="sideformrow"
                >{{ $t("comments.label.endDate") }}:</label
              >
              <b-form-datepicker
                id="end-date-picker"
                v-model="searchParams.endDateString"
                class="mb-2"
                reset-button
              ></b-form-datepicker>
            </div>
            <br /><br />

            <div class="sideformrow">
              <label for="status" class="sideformrow">
                {{ $t("comments.pendingStatus") }}:
              </label>
              <div>
                <select
                  id="status"
                  v-model="searchParams.status"
                  size="1"
                  required
                >
                  <option
                    v-for="(value, key) in lookupVals.commentApprovalStatuses"
                    :key="key"
                    :value="key"
                  >
                    {{ $t(value) }}
                  </option>
                </select>
              </div>
            </div>
            <br /><br />
            <button type="button" v-on:click="loadComments()">
              {{ $t("entries.buttonFilter") }}
            </button>
            <br />
          </div>
        </div>
      </div>
    </div>

    <div v-if="commentData.comments.length == 0">
      {{ $t("comments.noCommentsFound") }}
    </div>

    <div v-if="commentData.comments.length > 0">
      <!-- =============================================================
         Number of comments and date message, next/prev links
         ============================================================= -->

      <div class="tablenav">
        <div style="float: left" v-html="nowShowingMsg"></div>

        <span v-if="commentData.comments.length > 0">
          <div style="float: right">
            {{ commentData.comments[0].postTime | standard_datetime }}
            ---
            {{
              commentData.comments[commentData.comments.length - 1].postTime
                | standard_datetime
            }}
          </div>
        </span>
        <br /><br />

        <span v-if="pageNum > 0 || commentData.hasMore" v-cloak>
          <center>
            &laquo;
            <button
              type="button"
              v-bind:disabled="pageNum <= 0"
              v-on:click="previousPage()"
            >
              {{ $t("weblogEntryQuery.prev") }}
            </button>
            |
            <button
              type="button"
              v-bind:disabled="!commentData.hasMore"
              v-on:click="nextPage()"
            >
              {{ $t("weblogEntryQuery.next") }}
            </button>
            &raquo;
          </center>
        </span>
      </div>

      <table class="table table-sm table-bordered table-hover" width="100%">
        <!-- =============================================================
         List of comments
         ============================================================= -->

        <thead class="thead-light">
          <tr>
            <th width="8%">{{ $t("comments.showHide") }}</th>
            <th width="8%">{{ $t("common.delete") }}</th>
            <th>
              {{ $t("comments.columnComment") }} -
              <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
              {{ $t("comments.pending") }}&nbsp;&nbsp;
              <span class="spamCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
              {{ $t("comments.spam") }}&nbsp;&nbsp;
            </th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="comment in commentData.comments" :key="comment.id">
            <td>
              <button
                type="button"
                v-if="
                  comment.status == 'SPAM' ||
                  comment.status == 'DISAPPROVED' ||
                  comment.status == 'PENDING'
                "
                v-on:click="approveComment(comment)"
              >
                {{ $t("comments.approve") }}
              </button>
              <button
                type="button"
                v-if="comment.status == 'APPROVED'"
                v-on:click="hideComment(comment)"
              >
                {{ $t("comments.hide") }}
              </button>
            </td>
            <td>
              <button type="button" v-on:click="deleteComment(comment)">
                {{ $t("common.delete") }}
              </button>
            </td>

            <td v-bind:class="commentStatusClass(comment.status)">
              <table class="innertable">
                <tr>
                  <td class="viewbody">
                    <div class="viewdetails bot">
                      <div class="details">
                        {{ $t("comments.entryTitled") }}:&nbsp;
                        <a
                          v-bind:href="comment.weblogEntry.permalink"
                          target="_blank"
                          >{{ comment.weblogEntry.title }}</a
                        >
                      </div>
                      <div class="details">
                        {{ $t("comments.commentBy") }}:&nbsp;
                        <span v-html="getCommentHeader(comment)"></span>
                      </div>
                      <span v-if="comment.url">
                        <div class="details">
                          {{ $t("comments.commentByURL") }}:&nbsp;
                          <a v-href="comment.url">
                            {{ comment.url }}
                          </a>
                        </div>
                      </span>
                      <div class="details">
                        {{ $t("comments.postTime") }}:
                        {{ comment.postTime | standard_datetime }}
                      </div>
                    </div>
                    <div class="viewdetails bot">
                      <div class="details bot">
                        <textarea
                          style="width: 100%"
                          rows="10"
                          v-model="comment.content"
                          v-bind:readonly="!comment.editable"
                        ></textarea>
                      </div>
                      <div class="details" v-show="!comment.editable">
                        <a v-on:click="editComment(comment)">{{
                          $t("common.edit")
                        }}</a>
                      </div>
                      <div class="details" v-show="comment.editable">
                        <a v-on:click="saveComment(comment)">{{
                          $t("common.save")
                        }}</a>
                        &nbsp;|&nbsp;
                        <a v-on:click="editCommentCancel(comment)">{{
                          $t("common.cancel")
                        }}</a>
                      </div>
                    </div>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </tbody>
      </table>
      <br />
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
    entryId: {
      required: false,
      type: String,
    },
  },
  data() {
    return {
      searchParams: {
        status: "",
      },
      commentData: {
        comments: [],
      },
      entryTitleMsg: null,
      nowShowingMsg: null,
      errorObj: {},
      selectedCommentId: null,
      pageNum: 0,
      urlRoot: "/tb-ui/authoring/rest/comments/",
    };
  },
  computed: {
    ...mapState("startupConfig", {
      lookupVals: (state) => state.lookupValues,
    }),
  },
  methods: {
    ...mapActions({
      loadLookupValues: "startupConfig/loadLookupValues",
    }),
    loadComments: function () {
      let urlToUse = this.urlRoot + this.weblogId + "/page/" + this.pageNum;
      if (this.entryId) {
        urlToUse += "?entryId=" + this.entryId;
        this.entryTitleMsg = " ";
      }

      const queryParams = { ...this.searchParams };
      queryParams.startDate = this.dateToSeconds(
        this.searchParams.startDateString,
        false
      );
      queryParams.endDate = this.dateToSeconds(
        this.searchParams.endDateString,
        true
      );

      if (queryParams.status === "") {
        queryParams.status = null;
      }

      this.axios
        .post(urlToUse, queryParams)
        .then((response) => {
          this.commentData = response.data;
          if (this.entryId) {
            this.entryTitleMsg = this.$t("comments.entrySubtitle", {
              entryTitle: this.commentData.entryTitle,
            });
          }
          this.nowShowingMsg = this.$t("comments.nowShowing", {
            count: this.commentData.comments.length,
          });
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    previousPage: function () {
      this.pageNum--;
      this.loadComments();
    },
    nextPage: function () {
      this.pageNum++;
      this.loadComments();
    },
    editComment: function (comment) {
      this.messageClear();
      comment.editable = true;
      comment.originalContent = comment.content;
    },
    editCommentCancel: function (comment) {
      this.messageClear();
      comment.editable = false;
      comment.content = comment.originalContent;
    },
    saveComment: function (comment) {
      this.messageClear();
      if (!comment.editable) {
        return;
      }
      comment.editable = false;

      this.axios
        .put(this.urlRoot + comment.id + "/content", comment.content, {
          headers: { "Content-Type": "application/json" },
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    approveComment: function (comment) {
      this.messageClear();
      this.axios
        .post(this.urlRoot + comment.id + "/approve")
        .then((response) => {
          comment.status = "APPROVED";
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    hideComment: function (comment) {
      this.messageClear();
      this.axios
        .post(this.urlRoot + comment.id + "/hide")
        .then((response) => {
          comment.status = "DISAPPROVED";
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    deleteComment: function (comment) {
      this.messageClear();
      this.axios
        .delete(this.urlRoot + comment.id)
        .then((response) => {
          this.loadComments();
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    getCommentHeader: function (comment) {
      return this.$t("comments.commentHeader", {
        name: comment.name,
        email: comment.email,
        remoteHost: comment.remoteHost,
      });
    },
    commentStatusClass: function (status) {
      if (status === "SPAM") {
        return "spamcomment";
      } else if (status === "PENDING" || status === "DISAPPROVED") {
        return "pendingcomment";
      } else {
        return null;
      }
    },
    dateToSeconds: function (dateStr, addOne) {
      if (dateStr) {
        return (
          Math.floor(Date.parse(dateStr) / 1000) + (addOne ? 1440 * 60 - 1 : 0)
        );
      } else {
        return null;
      }
    },
    messageClear: function () {
      this.errorObj = {};
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = "/tb-ui/app/login";
      } else {
        this.errorMsg = error.response.data;
      }
    },
  },
  mounted: function () {
    this.loadLookupValues();
    this.loadComments();
  },
};
</script>

<style></style>
