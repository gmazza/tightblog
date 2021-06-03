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
    <AppSuccessMessageBox
      :message="successMessage"
      @close-box="successMessage = null"
    />
    <AppErrorMessageBox :message="errorMessage" @close-box="errorMessage = null" />

    <p class="subtitle">{{ $t("cachedData.subtitle") }}</p>

    <p>{{ $t("cachedData.explanation") }}</p>

    <!-- needed? -->
    <br style="clear:left" />

    <table class="table table-sm table-bordered table-striped">
      <thead class="thead-light">
        <tr>
          <th style="width:10%">{{ $t("common.name") }}</th>
          <th style="width:9%">{{ $t("cachedData.maxEntries") }}</th>
          <th style="width:9%">{{ $t("cachedData.currentSize") }}</th>
          <th style="width:9%">{{ $t("cachedData.incoming") }}</th>
          <th style="width:9%">{{ $t("cachedData.handledBy304") }}</th>
          <th style="width:9%">{{ $t("cachedData.cacheHits") }}</th>
          <th style="width:9%">{{ $t("cachedData.cacheMisses") }}</th>
          <th style="width:9%">{{ $t("cachedData.304Efficiency") }}</th>
          <th style="width:9%">{{ $t("cachedData.cacheEfficiency") }}</th>
          <th style="width:9%">{{ $t("cachedData.totalEfficiency") }}</th>
          <th style="width:9%"></th>
        </tr>
      </thead>
      <tbody id="tableBody" v-cloak>
        <tr v-for="item in cacheData" :key="item.cacheHandlerId">
          <td>{{ item.cacheHandlerId }}</td>
          <td>{{ item.maxEntries }}</td>
          <td>{{ item.estimatedSize }}</td>
          <td>{{ item.incomingRequests }}</td>
          <td>{{ item.requestsHandledBy304 }}</td>
          <td>{{ item.cacheHitCount }}</td>
          <td>{{ item.cacheMissCount }}</td>
          <td>
            {{
              item.incomingRequests > 0
                ? (item.requestsHandledBy304 / item.incomingRequests).toFixed(3)
                : ""
            }}
          </td>
          <td>
            {{ item.cacheRequestCount > 0 ? item.cacheHitRate.toFixed(3) : "" }}
          </td>
          <td>
            {{
              item.incomingRequests > 0
                ? (
                    (item.requestsHandledBy304 + item.cacheHitCount) /
                    item.incomingRequests
                  ).toFixed(3)
                : ""
            }}
          </td>
          <td class="buttontd">
            <button
              type="button"
              v-if="item.maxEntries > 0"
              v-on:click="clearCache(item.cacheHandlerId)"
            >
              {{ $t("cachedData.clear") }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="control clearfix">
      <button type="button" v-on:click="loadCacheData()">
        {{ $t("common.refresh") }}
      </button>
    </div>

    <div v-if="metadata.weblogList">
      <br /><br />
      {{ $t("cachedData.promptReset") }}:
      <br />
      <button type="button" v-on:click="resetHitCounts()">
        {{ $t("cachedData.buttonReset") }}
      </button>
      <br /><br />

      <div v-if="searchEnabled" class="abcd">
        {{ $t("cachedData.promptIndex") }}:
        <br />
        <select v-model="weblogToReindex" size="1" required>
          <option
            v-for="value in metadata.weblogList"
            :value="value"
            :key="value"
            >{{ value }}</option
          >
        </select>
        <button type="button" v-on:click="reindexWeblog()">
          {{ $t("cachedData.buttonIndex") }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";

export default {
  data() {
    return {
      urlRoot: "/tb-ui/admin/rest/server/",
      metadata: { weblogList: [] },
      weblogToReindex: null,
      successMessage: null,
      errorMessage: null,
      searchEnabled: false
    };
  },
  computed: {
    ...mapState("caches", {
      cacheData: state => state.items
    })
  },
  methods: {
    ...mapActions({
      loadCaches: "caches/loadCaches",
      clearCacheEntry: "caches/clearCacheEntry"
    }),
    messageClear: function() {
      this.successMessage = null;
      this.errorObj = {};
    },
    loadCacheData: function() {
      // https://stackoverflow.com/a/49284879
      this.loadCaches().then(
        () => {},
        error => this.commonErrorResponse(error, null)
      );
    },
    clearCache: function(cacheItem) {
      this.messageClear();

      this.clearCacheEntry(cacheItem).then(
        () => {
          this.successMessage = this.$t("cachedData.cacheCleared", {
            name: cacheItem
          });
          this.loadCacheData();
        },
        error => this.commonErrorResponse(error, null)
      );
    },
    getSearchEnabled: function() {
      this.axios
        .get(this.urlRoot + "searchenabled")
        .then(response => {
          this.searchEnabled = response.data;
        })
        .catch(error => this.commonErrorResponse(error, null));
    },
    loadWeblogList: function() {
      this.axios
        .get(this.urlRoot + "visibleWeblogHandles")
        .then(response => {
          this.metadata.weblogList = response.data;
          if (this.metadata.weblogList && this.metadata.weblogList.length > 0) {
            this.weblogToReindex = this.metadata.weblogList[0];
          }
        })
        .catch(error =>
          this.commonErrorResponse(error, "Weblog list cannot be retrieved.")
        );
    },
    resetHitCounts: function() {
      this.axios
        .post(this.urlRoot + "resethitcount")
        .then((this.successMessage = this.$t("cachedData.hitCountReset")))
        .catch(error => this.commonErrorResponse(error, null));
    },
    reindexWeblog: function() {
      if (this.weblogToReindex) {
        var handle = this.weblogToReindex;
        this.messageClear();
        this.axios
          .post(
            this.urlRoot + "weblog/" + this.weblogToReindex + "/rebuildindex"
          )
          .then(
            (this.successMessage = this.$t("cachedData.indexingStarted", {
              handle
            }))
          )
          .catch(error => this.commonErrorResponse(error, null));
      }
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
  mounted() {
    this.getSearchEnabled();
    this.loadWeblogList();
    this.loadCacheData();
  }
};
</script>
