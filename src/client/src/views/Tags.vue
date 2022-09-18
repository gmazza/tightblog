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
      <h2>{{ $t("tags.title") }}</h2>
      <p class="pagetip">{{ $t("tags.prompt") }}</p>

      <div class="tablenav">
        <span
          style="text-align: center"
          v-if="pageNum > 0 || tagData.hasMore"
          v-cloak
        >
          &laquo;
          <button
            type="button"
            v-bind:disabled="pageNum <= 0"
            v-on:click="previousPage()"
          >
            {{ $t("common.prevPage") }}
          </button>
          |
          <button
            type="button"
            v-bind:disabled="!tagData.hasMore"
            v-on:click="nextPage()"
          >
            {{ $t("common.nextPage") }}
          </button>
          &raquo;
        </span>

        <br />
      </div>

      <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
          <tr>
            <th width="4%">
              <input
                type="checkbox"
                v-bind:disabled="tagData.tags.length == 0"
                v-on:input="toggleCheckboxes($event.target.checked)"
                title="$t('selectAll')"
              />
            </th>
            <th width="20%">{{ $t("tags.columnTag") }}</th>
            <th width="10%">
              {{ $t("common.column.numEntries") }}
            </th>
            <th width="10%">
              {{ $t("common.column.firstEntry") }}
            </th>
            <th width="10%">
              {{ $t("common.column.lastEntry") }}
            </th>
            <th width="15%">
              {{ $t("tags.viewPublished") }}
            </th>
            <th width="13%"></th>
            <th width="13%"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tag in tagData.tags" :key="tag.id" v-cloak>
            <td class="center" style="vertical-align: middle">
              <input
                type="checkbox"
                v-bind:title="'checkbox for ' + tag.name"
                v-model="tag.selected"
                v-bind:value="tag.name"
              />
            </td>
            <td>{{ tag.name }}</td>
            <td>{{ tag.total }}</td>
            <td>{{ tag.firstEntry }}</td>
            <td>{{ tag.lastEntry }}</td>

            <td>
              <a v-bind:href="tag.viewUrl" target="_blank">{{
                $t("tags.columnView")
              }}</a>
            </td>

            <td class="buttontd">
              <button
                class="btn btn-warning"
                v-on:click="showReplaceModal(tag)"
              >
                {{ $t("tags.replace") }}
              </button>
            </td>

            <td class="buttontd">
              <button class="btn btn-warning" v-on:click="showAddModal(tag)">
                {{ $t("common.add") }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="control" v-if="tagData.tags.length > 0">
        <span style="padding-left: 7px">
          <button @click="deleteSelected()" v-bind:disabled="!tagsSelected">
            {{ $t("common.deleteSelected") }}
          </button>
        </span>
      </div>
    </div>

    <!-- Replace/Add tag modal -->
    <div tabindex="-1" role="dialog">
      <b-modal id="modal-tagupdate" centered @ok="tagUpdate()">
        <template #modal-title>
          <span v-html="editModalTitle"> </span>
        </template>
        <label for="newTag">{{ $t("common.name") }}:</label>
        <input id="newTag" v-model="newTagName" type="text" />
        <template #modal-ok>
          {{ $t("common.save") }}
        </template>
        <template #modal-cancel>
          {{ $t("common.cancel") }}
        </template>
      </b-modal>
    </div>

    <span v-if="tagData.tags.length == 0">
      {{ $t("tags.noneFound") }}
    </span>
  </div>
</template>

<script>
import asyncDataStatus from "@/mixins/AsyncDataStatus";

export default {
  props: {
    weblogId: {
      required: true,
      type: String,
    },
  },
  data() {
    return {
      tagData: { tags: {} },
      editModalTitle: "",
      editModalAction: null,
      editModalCurrentTag: null,
      newTagName: null,
      pageNum: 0,
      urlRoot: "/tb-ui/authoring/rest/tags/",
      resultsMap: {},
      successMessage: "",
      errorObj: {},
    };
  },
  mixins: [asyncDataStatus],
  computed: {
    tagsSelected: function () {
      return this.tagData.tags.filter((tag) => tag.selected).length > 0;
    },
  },
  methods: {
    toggleCheckboxes: function (checkAll) {
      this.tagData.tags.forEach((tag) => {
        // https://www.telerik.com/blogs/so-what-actually-is-vue-set
        this.$set(tag, "selected", checkAll);
      });
    },
    loadTags: function () {
      this.axios
        .get(this.urlRoot + this.weblogId + "/page/" + this.pageNum)
        .then((response) => {
          this.tagData = response.data;
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    deleteSelected: function () {
      this.$bvModal
        .msgBoxConfirm(this.$t("tags.confirmDelete"), {
          okTitle: this.$t("common.confirm"),
          cancelTitle: this.$t("common.cancel"),
        })
        .then((value) => {
          if (value) {
            this.messageClear();

            const selectedTagNames = [];
            this.tagData.tags.forEach((tag) => {
              if (tag.selected) selectedTagNames.push(tag.name);
            });

            this.axios
              .post(
                "/tb-ui/authoring/rest/tags/weblog/" +
                  this.weblogId +
                  "/delete",
                selectedTagNames
              )
              .then(() => {
                this.successMessage =
                  selectedTagNames.length + " tag(s) deleted";
                this.loadTags();
              });
          }
        });
    },
    showReplaceModal: function (oldTag) {
      this.showTagUpdateModal("replace", "tags.replaceTitle", oldTag.name);
    },
    showAddModal: function (currentTag) {
      this.showTagUpdateModal("add", "tags.addTitle", currentTag.name);
    },
    showTagUpdateModal: function (modalAction, modalTitle, tagName) {
      this.messageClear();
      this.editModalAction = modalAction;
      this.editModalCurrentTag = tagName;
      this.editModalTitle = this.$t(modalTitle, {
        tagName: this.editModalCurrentTag,
      });
      this.newTagName = tagName;
      this.$bvModal.show("modal-tagupdate");
    },
    tagUpdate: function () {
      this.messageClear();
      if (this.editModalAction === "replace") {
        this.replaceTag(this.editModalCurrentTag, this.newTagName);
      } else if (this.editModalAction === "add") {
        this.addTag(this.editModalCurrentTag, this.newTagName);
      }
    },
    addTag: function (currentTag, newTag) {
      this.messageClear();
      this.axios
        .post(
          this.urlRoot +
            "weblog/" +
            this.weblogId +
            "/add/currenttag/" +
            currentTag +
            "/newtag/" +
            newTag
        )
        .then((response) => {
          this.resultsMap = response.data;
          this.successMessage =
            "Added [" +
            newTag +
            "] to " +
            this.resultsMap.updated +
            " entries having [" +
            currentTag +
            (this.resultsMap.unchanged > 0
              ? "] (" +
                this.resultsMap.unchanged +
                " already had [" +
                newTag +
                "])"
              : "]");
          this.loadTags();
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    replaceTag: function (currentTag, newTag) {
      this.messageClear();
      this.axios
        .post(
          this.urlRoot +
            "weblog/" +
            this.weblogId +
            "/replace/currenttag/" +
            currentTag +
            "/newtag/" +
            newTag
        )
        .then((response) => {
          this.resultsMap = response.data;
          this.successMessage =
            "Replaced [" +
            currentTag +
            "] with [" +
            newTag +
            "] in " +
            this.resultsMap.updated +
            " entries" +
            (this.resultsMap.unchanged > 0
              ? ", deleted [" +
                currentTag +
                "] from " +
                this.resultsMap.unchanged +
                " entries already having [" +
                newTag +
                "]"
              : "");
          this.loadTags();
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    previousPage: function () {
      this.messageClear();
      this.pageNum--;
      this.loadTags();
    },
    nextPage: function () {
      this.messageClear();
      this.pageNum++;
      this.loadTags();
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = "/tb-ui/app/login";
      } else {
        this.errorObj = error.response.data;
      }
    },
    messageClear: function () {
      this.successMessage = "";
      this.errorObj = {};
    },
  },
  async created() {
    await this.loadTags();
    this.asyncDataStatus_fetched();
  },
};
</script>

<style></style>
