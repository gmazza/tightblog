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
    <AppAdminNav />
    <div style="text-align: left; padding: 20px">
      <AppSuccessMessageBox
        :message="successMessage"
        @close-box="successMessage = null"
      ></AppSuccessMessageBox>
      <AppErrorListMessageBox
        :in-error-obj="errorObj"
        @close-box="errorObj.errors = null"
      ></AppErrorListMessageBox>

      <div id="pendingList" v-cloak>
        <span v-for="item in pendingList" style="color: red" :key="item.id"
          >New registration request: {{ item.screenName }} ({{
            item.emailAddress
          }}):
          <button type="button" v-on:click="approveUser(item.id)">
            {{ $t("userAdmin.accept") }}
          </button>
          <button type="button" v-on:click="declineUser(item.id)">
            {{ $t("userAdmin.decline") }}
          </button>
          <br />
        </span>
      </div>

      <p class="subtitle">{{ $t("userAdmin.subtitle") }}</p>
      <span id="userEdit" v-cloak>
        <select v-model="userToEdit" size="1" v-on:change="loadUser()">
          <option v-for="(value, key) in userList" :value="key" :key="key">
            {{ value }}
          </option>
        </select>
      </span>

      <table class="formtable" v-if="userBeingEdited" v-cloak>
        <tr>
          <td class="label">{{ $t("userEdit.username") }}</td>
          <td class="field">
            <input
              type="text"
              size="30"
              maxlength="30"
              v-model="userBeingEdited.userName"
              readonly
              cssStyle="background: #e5e5e5"
            />
          </td>
          <td class="description">
            {{ $t("userEdit.tip.username") }}
          </td>
        </tr>

        <tr>
          <td class="label">{{ $t("userEdit.accountCreateDate") }}</td>
          <td class="field">
            {{ userBeingEdited.dateCreated | standard_datetime }}
          </td>
          <td class="description"></td>
        </tr>

        <tr>
          <td class="label">{{ $t("userEdit.lastLogin") }}</td>
          <td class="field">
            {{ userBeingEdited.lastLogin | standard_datetime }}
          </td>
          <td class="description"></td>
        </tr>

        <tr>
          <td class="label">
            <label for="screenName">{{ $t("userEdit.screenname") }}</label>
          </td>
          <td class="field">
            <input
              id="screenName"
              type="text"
              size="30"
              v-model="userBeingEdited.screenName"
              minlength="3"
              maxlength="30"
              required
            />
          </td>
          <td class="description">{{ $t("userAdmin.tip.screenName") }}</td>
        </tr>

        <tr>
          <td class="label">
            <label for="emailAddress">{{ $t("userEdit.email") }}</label>
          </td>
          <td class="field">
            <input
              id="emailAddress"
              type="email"
              size="40"
              v-model="userBeingEdited.emailAddress"
              maxlength="40"
              required
            />
          </td>
          <td class="description">{{ $t("userEdit.tip.email") }}</td>
        </tr>

        <tr v-if="userBeingEdited.status == 'ENABLED'">
          <td class="label">
            <label for="passwordText">{{ $t("userEdit.password") }}</label>
          </td>
          <td class="field">
            <input
              id="passwordText"
              type="password"
              size="20"
              v-model="userCredentials.passwordText"
              minlength="8"
              maxlength="20"
            />
          </td>

          <td class="description">{{ $t("userAdmin.tip.password") }}</td>
        </tr>
        <tr v-if="userBeingEdited.status == 'ENABLED'">
          <td class="label">
            <label for="passwordConfirm">{{
              $t("userEdit.passwordConfirm")
            }}</label>
          </td>
          <td class="field">
            <input
              id="passwordConfirm"
              type="password"
              size="20"
              v-model="userCredentials.passwordConfirm"
              minlength="8"
              maxlength="20"
              autocomplete="new-password"
            />
          </td>

          <td class="description">{{ $t("userAdmin.tip.passwordConfirm") }}</td>
        </tr>

        <tr>
          <td class="label">
            <label for="userStatus">{{ $t("userAdmin.userStatus") }}</label>
          </td>
          <td class="field">
            <select id="userStatus" v-model="userBeingEdited.status" size="1">
              <option
                v-for="(value, key) in lookupVals.userStatuses"
                :value="key"
                :key="key"
              >
                {{ value }}
              </option>
            </select>
          </td>
          <td class="description">{{ $t("userAdmin.tip.userStatus") }}</td>
        </tr>

        <tr>
          <td class="label">
            <label for="globalRole">{{ $t("userAdmin.globalRole") }}</label>
          </td>
          <td class="field">
            <select
              id="globalRole"
              v-model="userBeingEdited.globalRole"
              size="1"
            >
              <option
                v-for="(value, key) in lookupVals.globalRoles"
                :value="key"
                :key="key"
              >
                {{ value }}
              </option>
            </select>
          </td>
          <td class="description">{{ $t("userAdmin.tip.globalRole") }}</td>
        </tr>

        <tr v-if="lookupVals.mfaEnabled && userCredentials">
          <td class="label">
            <label for="hasMfaSecret">{{ $t("userAdmin.hasMfaSecret") }}</label>
          </td>
          <td class="field">
            <input
              type="text"
              size="5"
              maxlength="5"
              v-model="userCredentials.hasMfaSecret"
              readonly
            />
            <span v-show="userCredentials.hasMfaSecret == true">
              <input
                type="checkbox"
                id="eraseSecret"
                v-model="userCredentials.eraseMfaSecret"
              />
              <label for="eraseSecret">{{
                $t("userAdmin.mfaSecret.erase")
              }}</label>
            </span>
          </td>
          <td class="description">{{ $t("userAdmin.tip.mfaSecret") }}</td>
        </tr>
      </table>

      <br />

      <div class="control" v-show="userBeingEdited" v-cloak>
        <button type="button" class="buttonBox" v-on:click="updateUser()">
          {{ $t("common.save") }}
        </button>
        <button type="button" class="buttonBox" v-on:click="cancelChanges()">
          {{ $t("common.cancel") }}
        </button>
      </div>

      <div class="showinguser" v-if="userBeingEdited" v-cloak>
        <p>{{ $t("userAdmin.userMemberOf") }}</p>
        <table class="table table-bordered table-hover">
          <thead class="thead-light">
            <tr>
              <th style="width: 30%">{{ $t("common.weblog") }}</th>
              <th style="width: 10%">{{ $t("common.role") }}</th>
              <th style="width: 25%">{{ $t("common.entries") }}</th>
              <th width="width: 25%">{{ $t("userAdmin.manage") }}</th>
              <th width="width: 25%">{{ $t("userAdmin.removeFromWeblog") }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="weblogRole in userBlogList"
              v-bind:key="weblogRole.handle"
            >
              <td>
                <a v-bind:href="weblogRole.weblog.absoluteURL" target="_blank">
                  {{ weblogRole.weblog.name }} [{{ weblogRole.weblog.handle }}]
                </a>
              </td>
              <td>
                {{ weblogRole.weblogRole }}
              </td>
              <td>
                <img src="@/assets/page_white_edit.png" />
                <router-link
                  :to="{
                    name: 'entries',
                    params: { weblogId: weblogRole.weblog.id },
                  }"
                  target="_blank"
                  >{{ $t("common.entries") }}</router-link
                >
              </td>
              <td>
                <img src="@/assets/page_white_edit.png" />
                <router-link
                  :to="{
                    name: 'weblogConfig',
                    params: { weblogId: weblogRole.weblog.id },
                  }"
                  target="_blank"
                  >{{ $t("userAdmin.manage") }}</router-link
                >
              </td>
              <td>
                <button
                  type="button"
                  v-on:click="
                    setUserWeblogAffiliation(
                      weblogRole.weblog.id,
                      'NOBLOGNEEDED'
                    )
                  "
                >
                  {{ $t("common.remove") }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="potentialWeblogs.length > 0">
          {{ $t("userAdmin.addUserToWeblog") }}

          <select v-model="weblogIdToAdd" size="1" required>
            <option
              v-for="weblog in potentialWeblogs"
              :key="weblog.id"
              :value="weblog.id"
            >
              {{ formatWeblogOption(weblog) }}
            </option>
          </select>

          {{ $t("common.role") }}:

          <input type="radio" v-model="weblogToAddRole" value="OWNER" />
          {{ $t("userAdmin.owner") }}

          <input type="radio" v-model="weblogToAddRole" value="POST" />
          {{ $t("userAdmin.publisher") }}

          <button
            type="button"
            :disabled="!weblogIdToAdd || !weblogToAddRole"
            @click="setUserWeblogAffiliation(weblogIdToAdd, weblogToAddRole)"
          >
            {{ $t("common.add") }}
          </button>
        </div>

        <div v-else>
          {{ $t("userAdmin.noMoreBlogs") }}
        </div>
      </div>
      <br />
      <br />
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";
import asyncDataStatus from "@/mixins/AsyncDataStatus";

export default {
  data() {
    return {
      urlRoot: "/tb-ui/admin/rest/useradmin/",
      pendingList: {},
      userToEdit: null,
      userBeingEdited: null,
      userCredentials: null,
      userBlogList: [],
      weblogIdToAdd: null,
      weblogToAddRole: null,
      successMessage: null,
      errorObj: {},
    };
  },
  mixins: [asyncDataStatus],
  computed: {
    ...mapState("startupConfig", {
      lookupVals: (state) => state.lookupValues,
    }),
    ...mapState("dynamicConfig", {
      userList: (state) => state.userList,
      weblogList: (state) => state.weblogList,
    }),
    potentialWeblogs: function () {
      return this.weblogList.filter(
        (w) =>
          this.userBlogList.filter((u) => u.weblog.id === w.id).length === 0
      );
    },
  },
  methods: {
    ...mapActions({
      loadLookupValues: "startupConfig/loadLookupValues",
      loadUserList: "dynamicConfig/loadUserList",
      loadWeblogList: "dynamicConfig/loadWeblogList",
    }),
    formatWeblogOption: function (weblog) {
      return weblog.name + " (" + weblog.handle + ")";
    },
    getPendingRegistrations: function () {
      this.axios.get(this.urlRoot + "registrationapproval").then((response) => {
        this.pendingList = response.data;
      });
    },
    approveUser: function (userId) {
      this.processRegistration(userId, "approve");
    },
    declineUser: function (userId) {
      this.processRegistration(userId, "reject");
    },
    processRegistration: function (userId, command) {
      this.messageClear();
      this.axios
        .post(this.urlRoot + "registrationapproval/" + userId + "/" + command)
        .then(() => {
          this.getPendingRegistrations();
          this.loadUserList();
        })
        .catch((error) => this.commonErrorResponse(error, null));
    },
    loadUser: function () {
      this.messageClear();

      if (!this.userToEdit) {
        return;
      }

      this.axios
        .get(this.urlRoot + "user/" + this.userToEdit)
        .then((response) => {
          this.userBeingEdited = response.data.user;
          if (
            Object.prototype.hasOwnProperty.call(response.data, "credentials")
          ) {
            this.userCredentials = response.data.credentials;
          } else {
            this.userCredentials = null;
          }
        });

      this.loadUserWeblogs();
    },
    loadUserWeblogs: function () {
      this.axios
        .get(this.urlRoot + "user/" + this.userToEdit + "/weblogs")
        .then((response) => {
          this.userBlogList = response.data;
        });
    },
    updateUser: function () {
      this.messageClear();
      const userData = {};
      userData.user = this.userBeingEdited;
      userData.credentials = this.userCredentials;

      this.axios
        .put(this.urlRoot + "user/" + this.userBeingEdited.id, userData)
        .then((response) => {
          this.userBeingEdited = response.data.user;
          this.userCredentials = response.data.credentials;
          this.loadUserList();
          this.getPendingRegistrations();
          this.successMessage = this.$t("userAdmin.userUpdated", {
            screenName: this.userBeingEdited.screenName,
          });
        })
        .catch((error) => this.commonErrorResponse(error, null));
    },
    setUserWeblogAffiliation: function (weblogId, weblogRole) {
      this.messageClear();
      if (!weblogId || !weblogRole) {
        return;
      }
      this.axios
        .post(
          "/tb-ui/authoring/rest/weblog/" +
            weblogId +
            "/user/" +
            this.userBeingEdited.id +
            "/role/" +
            weblogRole +
            "/associate"
        )
        .then((response) => {
          this.successMessage = response.data;
          this.weblogIdToAdd = null;
          this.weblogToAddRole = null;
          this.loadUserWeblogs();
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    cancelChanges: function () {
      this.messageClear();
      this.userBeingEdited = null;
      this.userCredentials = null;
    },
    messageClear: function () {
      this.successMessage = null;
      this.errorObj = {};
    },
    commonErrorResponse: function (error, errorMsg) {
      if (errorMsg) {
        this.errorObj[0] = errorMsg;
      } else if (error && error.response && error.response.status === 401) {
        window.location.href = "/tb-ui/app/login";
      } else if (error && error.response) {
        this.errorObj = error.response.data;
      } else if (error) {
        this.errorObj[0] = error;
      } else {
        this.errorObj[0] = "System error.";
      }
    },
  },
  async created() {
    await this.loadLookupValues();
    await this.getPendingRegistrations();
    await this.loadUserList();
    await this.loadWeblogList();
    this.asyncDataStatus_fetched();
  },
};
</script>
