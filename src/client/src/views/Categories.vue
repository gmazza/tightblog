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
      <AppErrorListMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = null"
      ></AppErrorListMessageBox>
      <h2>{{ $t("categories.title") }}</h2>
      <p class="pagetip">{{ $t("categories.rootPrompt") }}</p>

      <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
          <tr>
            <th width="20%">{{ $t("common.category") }}</th>
            <th width="20%">{{ $t("common.column.numEntries") }}</th>
            <th width="20%">{{ $t("common.column.firstEntry") }}</th>
            <th width="20%">{{ $t("common.column.lastEntry") }}</th>
            <th width="10%">{{ $t("common.rename") }}</th>
            <th width="10%">{{ $t("common.delete") }}</th>
          </tr>
        </thead>
        <tbody v-cloak>
          <tr v-for="item in orderedItems" :key="item.id">
            <td>{{ item.name }}</td>
            <td>{{ item.numEntries }}</td>
            <td>{{ item.firstEntry }}</td>
            <td>{{ item.lastEntry }}</td>
            <td class="buttontd">
              <button
                class="btn btn-warning"
                v-on:click="showUpsertModal(item)"
              >
                {{ $t("common.rename") }}
              </button>
            </td>
            <td class="buttontd">
              <span v-if="items.length > 1">
                <button
                  v-b-modal.modal-delete
                  @click="showDeleteModal(item)"
                  class="btn btn-danger"
                >
                  {{ $t("common.delete") }}
                </button>
              </span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="control clearfix">
        <button type="button" v-on:click="showUpsertModal(null)">
          {{ $t("categories.addCategory") }}
        </button>
      </div>

      <!-- Add/Edit Category modal -->
      <div tabindex="-1" role="dialog">
        <b-modal id="modal-upsert" centered @ok="upsertItem()">
          <template #modal-title>
            {{ upsertModalTitle }}
          </template>

          <span v-if="showUpdateErrorMessage">
            {{ $t("categories.errorDuplicateName") }}<br />
          </span>
          <label for="category-name">{{ $t("common.name") }}:</label>
          <input
            id="category-name"
            v-model="itemToEdit.name"
            maxlength="80"
            size="40"
          />

          <template #modal-ok>
            {{ $t("common.save") }}
          </template>
          <template #modal-cancel>
            {{ $t("common.cancel") }}
          </template>
        </b-modal>
      </div>

      <!-- Delete category modal -->
      <div tabindex="-1" role="dialog">
        <b-modal
          id="modal-delete"
          centered
          ok-variant="danger"
          @ok="deleteItem()"
        >
          <template #modal-title>
            <h5
              v-html="
                $t('categories.deleteCategory', {
                  categoryName: categoryToDelete.name,
                })
              "
            ></h5>
          </template>
          <p>
            {{ $t("categories.deleteMoveToWhere") }}
            <select v-model="targetCategoryId" size="1" required>
              <option
                v-for="item in moveToCategories"
                :key="item.id"
                :value="item.id"
              >
                {{ item.name }}
              </option>
            </select>
          </p>
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
import asyncDataStatus from "@/mixins/AsyncDataStatus";

export default {
  mixins: [asyncDataStatus],
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
      errorObj: {},
      upsertModalTitle: "",
      showUpdateErrorMessage: false,
      categoryToDelete: null,
      targetCategoryId: null,
    };
  },
  computed: {
    orderedItems: function () {
      return this.items.concat().sort(this.globalSortBy("position"));
    },
    moveToCategories: function () {
      // adding extra var to trigger cache refresh
      const compareVal = this.categoryToDelete
        ? this.categoryToDelete.id
        : null;
      return this.items.filter(function (item) {
        return item.id !== compareVal;
      });
    },
  },
  methods: {
    loadItems: function () {
      this.axios
        .get(process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/categories?weblogId=" + this.weblogId)
        .then((response) => {
          this.items = response.data;
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    showUpsertModal: function (item) {
      this.itemToEdit = {};
      if (item == null) {
        this.upsertModalTitle = this.$t("categories.addCategory");
      } else {
        this.itemToEdit.id = item.id;
        this.itemToEdit.name = item.name;
        this.upsertModalTitle = this.$t("categories.updateCategory");
      }
      this.$bvModal.show("modal-upsert");
    },
    upsertItem: function (obj) {
      this.messageClear();
      const categoryId = this.itemToEdit.id;

      if (this.itemToEdit.name) {
        this.itemToEdit.name = this.itemToEdit.name.replace(/[,%"/]/g, "");
        if (this.itemToEdit.name) {
          this.axios
            .put(
              categoryId
                ? process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/weblog/" + this.weblogId + "/category"
                : process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/categories?weblogId=" + this.weblogId,
              this.itemToEdit
            )
            .then((response) => {
              this.itemToEdit = {};
              this.loadItems();
            })
            .catch((error) => this.commonErrorResponse(error));
        }
      }
    },
    showDeleteModal: function (category) {
      this.categoryToDelete = category;
      this.$bvModal.show("modal-delete");
    },
    deleteItem: function () {
      if (this.targetCategoryId) {
        this.messageClear();
        this.axios
          .delete(
            process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/weblog/" +
              this.weblogId +
              "/category/" +
              this.categoryToDelete.id +
              "?targetCategoryId=" +
              this.targetCategoryId
          )
          .then((response) => {
            this.targetCategoryId = null;
            this.loadItems();
          })
          .catch((error) => this.commonErrorResponse(error));
      }
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = process.env.VUE_APP_PUBLIC_PATH + "/app/login";
      } else if (error.response.status === 409) {
        this.showUpdateErrorMessage = true;
      } else {
        this.errorObj = error.response.data;
      }
    },
    messageClear: function () {
      this.showUpdateErrorMessage = false;
      this.errorObj = {};
    },
  },
  async created() {
    await this.loadItems();
    this.asyncDataStatus_fetched();
  },
};
</script>

<style></style>
