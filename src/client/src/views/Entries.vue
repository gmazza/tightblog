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
    <AppUserNav />
    <AppErrorListMessageBox
      :in-error-obj="errorObj"
      @close-box="errorObj.errors = null"
    ></AppErrorListMessageBox>
    <h2>{{ $t("entries.title") }}</h2>
    <p class="pagetip">{{ $t("entries.tip") }}</p>

    <div class="sidebarFade">
      <div class="menu-tr">
        <div class="menu-tl">
          <div class="sidebarInner">
            <h3>{{ $t("entries.sidebarTitle") }}</h3>
            <hr size="1" noshade="noshade" />

            <p>{{ $t("entries.sidebarDescription") }}</p>

            <div>
              <div class="sideformrow">
                <label for="categoryId" class="sideformrow">
                  {{ $t("common.category") }}</label
                >
                <select
                  id="categoryId"
                  v-model="searchParams.categoryName"
                  size="1"
                  required
                >
                  <option
                    v-for="(value, key) in lookupFields.categories"
                    :value="key"
                    :key="key"
                  >
                    {{ value }}
                  </option>
                </select>
              </div>
              <br /><br />

              <div class="sideformrow">
                <label for="startDateString" class="sideformrow"
                  >{{ $t("entries.label.startDate") }}:</label
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
                  >{{ $t("entries.label.endDate") }}:</label
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
                  {{ $t("entries.label.status") }}:
                </label>
                <div>
                  <select
                    id="status"
                    v-model="searchParams.status"
                    size="1"
                    required
                  >
                    <option
                      v-for="(value, key) in lookupFields.statusOptions"
                      :value="key"
                      :key="key"
                    >
                      {{ value }}
                    </option>
                  </select>
                </div>
              </div>

              <div class="sideformrow">
                <label for="status" class="sideformrow">
                  {{ $t("entries.label.sortBy") }}: <br /><br />
                </label>
                <div>
                  <div
                    v-for="(value, key) in lookupFields.sortByOptions"
                    :key="key"
                  >
                    <input
                      type="radio"
                      name="sortBy"
                      v-model="searchParams.sortBy"
                      v-bind:value="key"
                    />{{ value }}<br />
                  </div>
                </div>
              </div>
              <br />
              <button type="button" v-on:click="loadEntries()">
                {{ $t("entries.buttonFilter") }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 
      =============================================================
      Number of entries and date message, next/prev links
      ============================================================= 
    -->

    <div class="tablenav" v-cloak>
      <div style="float: left">
        {{ entriesData.entries.length }}
        {{ $t("entries.entriesReturned") }}
      </div>
      <span v-if="entriesData.entries.length > 0">
        <div style="float: right">
          <span v-if="entriesData.entries[0].pubTime != null">
            {{ entriesData.entries[0].pubTime | standard_datetime }}
          </span>
          ---
          <span
            v-if="
              entriesData.entries[entriesData.entries.length - 1].pubTime !=
              null
            "
          >
            {{
              entriesData.entries[entriesData.entries.length - 1].pubTime
                | standard_datetime
            }}
          </span>
        </div>
      </span>
      <br /><br />

      <span v-if="pageNum > 0 || entriesData.hasMore">
        <center>
          &laquo;
          <button
            type="button"
            v-bind:disabled="pageNum <= 0"
            v-on:click="previousPage()"
          >
            {{ $t("common.previousPage") }}
          </button>
          |
          <button
            type="button"
            v-bind:disabled="!entriesData.hasMore"
            v-on:click="nextPage()"
          >
            {{ $t("common.nextPage") }}
          </button>
          &raquo;
        </center>
      </span>

      <br />
    </div>

    <!-- 
      =============================================================
        Entry table
      ============================================================= 
    -->

    <p>
      <span class="draftEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
      {{ $t("entries.draft") }}&nbsp;&nbsp;
      <span class="pendingEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
      {{ $t("entries.pending") }}&nbsp;&nbsp;
      <span class="scheduledEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
      {{ $t("entries.scheduled") }}&nbsp;&nbsp;
    </p>

    <table class="table table-sm table-bordered table-hover" width="100%">
      <thead class="thead-light">
        <tr>
          <th width="15%">{{ $t("entries.pubTime") }}</th>
          <th width="15%">
            {{ $t("entries.updateTime") }}
          </th>
          <th width="8%">{{ $t("common.category") }}</th>
          <th>{{ $t("entries.entryTitle") }}</th>
          <th width="16%">{{ $t("common.tags") }}</th>
          <th width="5%"></th>
          <th width="5%"></th>
          <th width="5%"></th>
        </tr>
      </thead>

      <tbody>
        <tr
          v-for="entry in entriesData.entries"
          :class="entryStatusClass(entry.status)"
          :key="entry.id"
          v-cloak
        >
          <td>
            <span v-if="entry.pubTime != null">
              {{ entry.pubTime | standard_datetime }}
            </span>
          </td>

          <td>
            <span v-if="entry.updateTime != null">
              {{ entry.updateTime | standard_datetime }}
            </span>
          </td>

          <td>
            {{ entry.category.name }}
          </td>

          <td>
            {{ entry.title.substr(0, 80) }}
          </td>

          <td>
            {{ entry.tagsAsString }}
          </td>

          <td>
            <span v-if="entry.status == 'PUBLISHED'">
              <a v-bind:href="entry.permalink" target="_blank">{{
                $t("entries.view")
              }}</a>
            </span>
          </td>

          <td>
            <a
              target="_blank"
              v-bind:href="
                '/tb-ui/app/authoring/entryEdit?weblogId=' +
                weblogId +
                '&entryId=' +
                entry.id
              "
            >
              {{ $t("common.edit") }}
            </a>
          </td>

          <td class="buttontd">
            <button
              class="btn btn-danger"
              v-on:click="deleteWeblogEntry(entry)"
            >
              {{ $t("common.delete") }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <span v-if="entriesData.entries.length == 0">
      {{ $t("entries.noneFound") }}
    </span>
  </div>
</template>

<script>
export default {
  props: {
    weblogId: {
      required: true,
      type: String,
    },
  },
  data() {
    return {
      lookupFields: {},
      searchParams: {
        categoryName: "",
        sortBy: "PUBLICATION_TIME",
        status: "",
      },
      entriesData: {
        entries: [],
      },
      errorObj: {},
      pageNum: 0,
      urlRoot: "/tb-ui/authoring/rest/weblogentries/",
    };
  },
  methods: {
    loadEntries: function () {
      const queryParams = { ...this.searchParams };

      queryParams.startDate = this.dateToSeconds(
        this.searchParams.startDateString,
        false
      );
      queryParams.endDate = this.dateToSeconds(
        this.searchParams.endDateString,
        true
      );

      if (queryParams.categoryName === "") {
        queryParams.categoryName = null;
      }
      if (queryParams.status === "") {
        queryParams.status = null;
      }

      this.axios
        .post(
          this.urlRoot + this.weblogId + "/page/" + this.pageNum,
          queryParams
        )
        .then((response) => {
          this.entriesData = response.data;
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    entryStatusClass: function (status) {
      if (status === "DRAFT") {
        return "draftEntryBox";
      } else if (status === "PENDING") {
        return "pendingEntryBox";
      } else if (status === "SCHEDULED") {
        return "scheduledEntryBox";
      } else {
        return null;
      }
    },
    deleteWeblogEntry: function (entry) {
      this.$bvModal
        .msgBoxConfirm(
          this.$t("entries.confirmDeleteTemplate", {
            title: entry.title,
          }),
          {
            okTitle: this.$t("common.confirm"),
            cancelTitle: this.$t("common.cancel"),
          }
        )
        .then((value) => {
          if (value) {
            this.axios
              .delete(this.urlRoot + entry.id)
              .then((response) => {
                this.loadEntries();
              })
              .catch((error) => this.commonErrorResponse(error));
          }
        });
    },
    loadLookupFields: function () {
      this.axios
        .get(this.urlRoot + this.weblogId + "/searchfields")
        .then((response) => {
          this.lookupFields = response.data;
        })
        .catch((error) => this.commonErrorResponse(error));
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
    previousPage: function () {
      this.pageNum--;
      this.loadEntries();
    },
    nextPage: function () {
      this.pageNum++;
      this.loadEntries();
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
    this.loadLookupFields();
    this.loadEntries();
  },
};
</script>

<style></style>
