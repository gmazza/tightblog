<template>
  <div v-if="!isFetching">
    <AppTitleBar />
    <AppUserNav />
    <div style="text-align: left; padding: 20px">
      <AppErrorMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = []"
      ></AppErrorMessageBox>
      <h2>{{ $t('comments.title') }}</h2>
      <p class="pagetip">
        {{ $t('comments.tip') }}
      </p>
      <div class="sidebarFade">
        <div class="menu-tr">
          <div class="menu-tl">
            <div class="sidebarInner">
              <h3>{{ $t('comments.sidebarTitle') }}</h3>
              <hr size="1" noshade="noshade" />

              <p>{{ $t('comments.sidebarDescription') }}</p>

              <div class="sideformrow">
                <label for="searchText" class="sideformrow"
                  >{{ $t('comments.searchString') }}:</label
                >
                <input id="searchText" type="text" v-model="searchParams.searchText" size="30" />
              </div>
              <br />
              <br />

              <div class="sideformrow">
                <label for="startDateString" class="sideformrow"
                  >{{ $t('comments.label.startDate') }}:</label
                >
                <VueDatePicker
                  v-model="searchParams.startDate"
                  :enable-time-picker="false"
                  :clear-button="true"
                  :reset-button="true"
                />
              </div>

              <div class="sideformrow">
                <label for="endDateString" class="sideformrow"
                  >{{ $t('comments.label.endDate') }}:</label
                >
                <VueDatePicker
                  v-model="searchParams.endDate"
                  :enable-time-picker="false"
                  :clear-button="true"
                  :reset-button="true"
                />
              </div>
              <br /><br />

              <div class="sideformrow">
                <label for="status" class="sideformrow">
                  {{ $t('comments.pendingStatus') }}:
                </label>
                <div>
                  <select id="status" v-model="searchParams.status" size="1" required>
                    <option
                      v-for="(value, key) in lookupValues.commentApprovalStatuses"
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
                {{ $t('entries.buttonFilter') }}
              </button>
              <br />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="commentData.comments.length == 0">
      {{ $t('comments.noCommentsFound') }}
    </div>

    <div v-if="commentData.comments.length > 0">
      <!-- =============================================================
         Number of comments and date message, next/prev links
         ============================================================= -->

      <div class="tablenav">
        <div style="float: left" v-html="nowShowingMsg"></div>

        <span v-if="commentData.comments.length > 0">
          <div style="float: right">
            {{ formatDateTime(commentData.comments[0].postTime) }}
            ---
            {{ formatDateTime(commentData.comments[commentData.comments.length - 1].postTime) }}
          </div>
        </span>
        <br /><br />

        <span v-if="pageNum > 0 || commentData.hasMore" v-cloak class="centered-buttons">
          &laquo;
          <button type="button" v-bind:disabled="pageNum <= 0" v-on:click="previousPage()">
            {{ $t('weblogEntryQuery.prev') }}
          </button>
          |
          <button type="button" v-bind:disabled="!commentData.hasMore" v-on:click="nextPage()">
            {{ $t('weblogEntryQuery.next') }}
          </button>
          &raquo;
        </span>
      </div>

      <table class="table table-sm table-bordered table-hover" width="100%">
        <!-- =============================================================
         List of comments
         ============================================================= -->

        <thead class="thead-light">
          <tr>
            <th width="8%">{{ $t('comments.showHide') }}</th>
            <th width="8%">{{ $t('common.delete') }}</th>
            <th>
              {{ $t('comments.columnComment') }} -
              <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
              {{ $t('comments.pending') }}&nbsp;&nbsp;
              <span class="spamCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
              {{ $t('comments.spam') }}&nbsp;&nbsp;
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
                {{ $t('comments.approve') }}
              </button>
              <button
                type="button"
                v-if="comment.status == 'APPROVED'"
                v-on:click="hideComment(comment)"
              >
                {{ $t('comments.hide') }}
              </button>
            </td>
            <td>
              <button type="button" v-on:click="deleteComment(comment)">
                {{ $t('common.delete') }}
              </button>
            </td>

            <td v-bind:class="commentStatusClass(comment.status)">
              <table class="innertable">
                <tr>
                  <td class="viewbody">
                    <div class="viewdetails bot">
                      <div class="details">
                        {{ $t('comments.entryTitled') }}:&nbsp;
                        <a v-bind:href="comment.weblogEntry.permalink" target="_blank">{{
                          comment.weblogEntry.title
                        }}</a>
                      </div>
                      <div class="details">
                        {{ $t('comments.commentBy') }}:&nbsp;
                        <span v-html="getCommentHeader(comment)"></span>
                      </div>
                      <span v-if="comment.url">
                        <div class="details">
                          {{ $t('comments.commentByURL') }}:&nbsp;
                          <a v-bind:href="comment.url">
                            {{ comment.url }}
                          </a>
                        </div>
                      </span>
                      <div class="details">
                        {{ $t('comments.postTime') }}:
                        {{ formatDateTime(comment.postTime) }}
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
                        <a v-on:click.prevent="editComment(comment)">{{ $t('common.edit') }}</a>
                      </div>
                      <div class="details" v-show="comment.editable">
                        <a v-on:click.prevent="saveComment(comment)">{{ $t('common.save') }}</a>
                        &nbsp;|&nbsp;
                        <a v-on:click.prevent="editCommentCancel(comment)">{{
                          $t('common.cancel')
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
    </div>
  </div>
</template>

<script lang="ts">
import type {
  Comment,
  CommentStatus,
  CommentsData,
  EntryCommentsQueryParams,
  ErrorObj
} from '@/types/interfaces'
import { mapState, mapActions } from 'pinia'
import { AxiosError } from 'axios'
import { formatDateTime } from '../helpers'
import { useStartupConfigStore } from '../stores/startupConfig'
import api from '@/api'
import VueDatePicker from '@vuepic/vue-datepicker'
import '@vuepic/vue-datepicker/dist/main.css'

export default {
  components: {
    VueDatePicker
  },
  props: {
    weblogId: {
      required: true,
      type: String
    },
    entryId: {
      required: false,
      type: String
    }
  },
  data() {
    return {
      searchParams: {} as EntryCommentsQueryParams,
      commentData: {
        comments: [] as Comment[],
        hasMore: false
      } as CommentsData,
      entryTitleMsg: null as string | null,
      nowShowingMsg: null as string | null,
      errorObj: {
        errors: []
      } as ErrorObj,
      selectedCommentId: null,
      pageNum: 0,
      urlRoot: import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/comments/',
      isFetching: true
    }
  },
  computed: {
    ...mapState(useStartupConfigStore, ['startupConfig', 'lookupValues'])
  },
  methods: {
    ...mapActions(useStartupConfigStore, ['loadLookupValues']),
    formatDateTime(date: Date) {
      return formatDateTime(date)
    },
    async loadComments() {
      if (this.entryId) {
        this.entryTitleMsg = null
      }

      const queryParams = { ...this.searchParams }
      if (queryParams.startDate != undefined) {
        queryParams.startDate.setHours(0, 0, 0, 0)
      }

      if (queryParams.endDate != undefined) {
        queryParams.endDate.setHours(23, 59, 59, 999)
      }

      if (queryParams.status === '') {
        delete queryParams.status
      }

      await api
        .loadComments(this.weblogId, this.pageNum, queryParams, this.entryId)
        .then((response) => {
          this.commentData.comments = response.comments
          this.commentData.hasMore = response.hasMore
          if (this.entryId) {
            this.entryTitleMsg = this.$t('comments.entrySubtitle', {
              entryTitle: this.commentData.entryTitle
            })
          }
          this.nowShowingMsg = this.$t('comments.nowShowing', {
            count: this.commentData.comments.length
          })
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    previousPage: function () {
      this.pageNum--
      this.loadComments()
    },
    nextPage: function () {
      this.pageNum++
      this.loadComments()
    },
    editComment: function (comment: Comment) {
      this.messageClear()
      comment.editable = true
      comment.originalContent = comment.content
    },
    editCommentCancel: function (comment: Comment) {
      this.messageClear()
      comment.editable = false
      comment.content = comment.originalContent
    },
    saveComment(comment: Comment) {
      this.messageClear()
      if (!comment.editable) {
        return
      }
      comment.editable = false

      api.saveComment(comment).catch((error) => this.commonErrorResponse(error))
    },
    approveComment: function (comment: Comment) {
      this.messageClear()
      try {
        api.approveComment(comment.id)
        comment.status = 'APPROVED'
      } catch (error) {
        this.commonErrorResponse(error)
      }
    },
    hideComment: function (comment: Comment) {
      this.messageClear()
      try {
        api.hideComment(comment.id)
        comment.status = 'DISAPPROVED'
      } catch (error) {
        this.commonErrorResponse(error)
      }
    },
    deleteComment: function (comment: Comment) {
      this.messageClear()
      try {
        api.deleteComment(comment.id)
        this.loadComments()
      } catch (error) {
        this.commonErrorResponse(error)
      }
    },
    getCommentHeader: function (comment: Comment) {
      return this.$t('comments.commentHeader', {
        name: comment.name,
        email: comment.email,
        remoteHost: comment.remoteHost
      })
    },
    commentStatusClass: function (status: CommentStatus) {
      if (status === 'SPAM') {
        return 'spamcomment'
      } else if (status === 'PENDING' || status === 'DISAPPROVED') {
        return 'pendingcomment'
      } else {
        return null
      }
    },
    messageClear: function () {
      this.errorObj.errors = []
    },
    commonErrorResponse: function (error: unknown) {
      if (error instanceof AxiosError && error.response?.status === 401) {
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/login'
      } else {
        if (error instanceof AxiosError) {
          this.errorObj = error.response?.data
        } else {
          this.errorObj = { errors: ['An unknown error occurred'] }
        }
        window.scrollTo(0, 0)
      }
    }
  },
  async created() {
    await this.loadLookupValues()
    await this.loadComments()
    this.isFetching = false
  }
}
</script>

<style scoped>
.dp__input_icon {
  vertical-align: middle;
  border: none;
}
</style>
