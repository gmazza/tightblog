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
      <div>
        <AppErrorListMessageBox
          :in-error-obj="errorObj"
          @close-box="errorObj.errors = null"
        ></AppErrorListMessageBox>
      </div>
      <h2>{{ $t("blogroll.title") }}</h2>
      <p class="pagetip">{{ $t("blogroll.rootPrompt") }}</p>

      <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
          <tr>
            <th width="5%">
              <input
                type="checkbox"
                :disabled="items.length == 0"
                @input="toggleCheckboxes($event.target.checked)"
                :title="$t('blogroll.selectAllLabel')"
              />
            </th>
            <th width="25%">
              {{ $t("blogroll.linkLabel") }}
            </th>
            <th width="25%">
              {{ $t("blogroll.urlHeader") }}
            </th>
            <th width="35%">
              {{ $t("common.description") }}
            </th>
            <th width="10%">{{ $t("common.edit") }}</th>
          </tr>
        </thead>
        <tbody id="tableBody" v-cloak>
          <tr v-for="item in orderedItems" :key="item.id">
            <td class="center" style="vertical-align: middle">
              <input
                type="checkbox"
                name="idSelections"
                :title="$t('blogroll.checkboxTitle', { itemName: item.name })"
                v-model="item.selected"
                :value="item.id"
              />
            </td>
            <td>{{ item.name }}</td>
            <td>
              <a target="_blank" :href="item.url">{{ item.url }}</a>
            </td>
            <td>{{ item.description }}</td>
            <td class="buttontd">
              <button
                class="btn btn-warning"
                v-on:click="showUpsertModal(item)"
              >
                {{ $t("common.edit") }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="control clearfix">
        <button type="button" v-on:click="showUpsertModal(null)">
          {{ $t("blogroll.addLink") }}
        </button>

        <span v-if="items.length > 0">
          <button @click="deleteSelected()" v-bind:disabled="!itemsSelected">
            {{ $t("common.deleteSelected") }}
          </button>
        </span>
      </div>

      <!-- Add/Edit Link modal -->
      <div tabindex="-1" role="dialog">
        <b-modal id="modal-upsert" centered @ok="updateItem()">
          <template #modal-title>
            {{ editModalTitle }}
          </template>
          <p class="pagetip">
            {{ $t("blogroll.requiredFields") }}
          </p>
          <form>
            <div class="form-group">
              <label for="name" class="col-form-label">{{
                $t("blogroll.linkLabel")
              }}</label>
              <input
                type="text"
                class="form-control"
                v-model="itemToEdit.name"
                maxlength="80"
              />
            </div>
            <div class="form-group">
              <label for="url" class="col-form-label">{{
                $t("common.url")
              }}</label>
              <input
                type="text"
                class="form-control"
                v-model="itemToEdit.url"
                maxlength="128"
              />
            </div>
            <div class="form-group">
              <label for="description" class="col-form-label">{{
                $t("common.description")
              }}</label>
              <input
                type="text"
                class="form-control"
                v-model="itemToEdit.description"
                maxlength="128"
              />
            </div>
          </form>
          <template #modal-ok>
            {{ $t("common.save") }}
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
      items: [],
      itemToEdit: {},
      editModalTitle: "",
      checkAll: false,
      errorObj: {},
    };
  },
  mixins: [asyncDataStatus],
  computed: {
    orderedItems: function () {
      return this.items.concat().sort(this.globalSortBy("position"));
    },
    itemsSelected: function () {
      return this.items.filter((item) => item.selected).length > 0;
    },
  },
  methods: {
    toggleCheckboxes: function (checkAll) {
      this.items.forEach((tag) => {
        // https://www.telerik.com/blogs/so-what-actually-is-vue-set
        this.$set(tag, "selected", checkAll);
      });
    },
    loadItems: function () {
      this.axios
        .get(import.meta.env.VITE_PUBLIC_PATH + "/authoring/rest/weblog/" + this.weblogId + "/bookmarks")
        .then((response) => {
          this.items = response.data;
        })
        .catch((error) => {
          self.commonErrorResponse(error, null);
        });
    },
    deleteSelected: function () {
      this.$bvModal
        .msgBoxConfirm(this.$t("blogroll.deleteWarning"), {
          okTitle: this.$t("common.confirm"),
          cancelTitle: this.$t("common.cancel"),
        })
        .then((value) => {
          if (value) {
            this.messageClear();
            const selectedLinkIds = [];

            this.items.forEach((item) => {
              if (item.selected) selectedLinkIds.push(item.id);
            });

            this.axios
              .post(import.meta.env.VITE_PUBLIC_PATH + "/authoring/rest/bookmarks/delete", selectedLinkIds)
              .then((response) => {
                this.loadItems();
              });
          }
        });
    },
    showUpsertModal: function (item) {
      this.messageClear();
      this.itemToEdit = {};
      if (item != null) {
        Object.assign(this.itemToEdit, item);
        this.editModalTitle = this.$t("blogroll.editLink");
      } else {
        this.editModalTitle = this.$t("blogroll.addLink");
      }
      this.$bvModal.show("modal-upsert");
    },
    updateItem: function () {
      this.messageClear();
      if (this.itemToEdit.name && this.itemToEdit.url) {
        this.axios
          .put(
            this.itemToEdit.id
              ? import.meta.env.VITE_PUBLIC_PATH + "/authoring/rest/bookmark/" + this.itemToEdit.id
              : import.meta.env.VITE_PUBLIC_PATH + "/authoring/rest/bookmarks?weblogId=" + this.weblogId,
            this.itemToEdit
          )
          .then((response) => {
            this.$bvModal.hide("modal-upsert");
            this.itemToEdit = {};
            this.loadItems();
          })
          .catch((error) => this.commonErrorResponse(error));
      }
    },
    messageClear: function () {
      this.errorObj = {};
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + "/app/login";
      } else {
        this.errorObj = error.response.data;
      }
    },
  },
  async created() {
    await this.loadItems();
    this.asyncDataStatus_fetched();
  },
};
</script>

<style></style>
