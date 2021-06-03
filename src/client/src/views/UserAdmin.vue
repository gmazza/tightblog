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
    ></AppSuccessMessageBox>
    <AppErrorListMessageBox
      :in-error-obj="errorObj"
      @close-box="errorObj.errors = null"
    ></AppErrorListMessageBox>

    <div id="pendingList" v-cloak>
      <span v-for="item in pendingList" style="color:red" :key="item.id"
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
        <option v-for="(value, key) in userList" :value="key" :key="key">{{
          value
        }}</option>
      </select>
    </span>

    <table class="formtable" v-if="userBeingEdited" v-cloak>
      <tr>
        <td class="label">{{ $t("userSettings.username") }}</td>
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
          {{ $t("userSettings.tip.username") }}
        </td>
      </tr>

      <tr>
        <td class="label">{{ $t("userSettings.accountCreateDate") }}</td>
        <td class="field">
          {{ userBeingEdited.dateCreated | standard_datetime }}
        </td>
        <td class="description"></td>
      </tr>

      <tr>
        <td class="label">{{ $t("userSettings.lastLogin") }}</td>
        <td class="field">
          {{ userBeingEdited.lastLogin | standard_datetime }}
        </td>
        <td class="description"></td>
      </tr>

      <tr>
        <td class="label">
          <label for="screenName">{{ $t("userSettings.screenname") }}</label>
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
          <label for="emailAddress">{{ $t("userSettings.email") }}</label>
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
        <td class="description">{{ $t("userAdmin.tip.email") }}</td>
      </tr>

      <tr v-if="userBeingEdited.status == 'ENABLED'">
        <td class="label">
          <label for="passwordText">{{ $t("userSettings.password") }}</label>
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
            $t("userSettings.passwordConfirm")
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
              v-for="(value, key) in metadata.userStatuses"
              :value="key"
              :key="key"
              >{{ value }}</option
            >
          </select>
        </td>
        <td class="description">{{ $t("userAdmin.tip.userStatus") }}</td>
      </tr>

      <tr>
        <td class="label">
          <label for="globalRole">{{ $t("userAdmin.globalRole") }}</label>
        </td>
        <td class="field">
          <select id="globalRole" v-model="userBeingEdited.globalRole" size="1">
            <option
              v-for="(value, key) in metadata.globalRoles"
              :value="key"
              :key="key"
              >{{ value }}</option
            >
          </select>
        </td>
        <td class="description">{{ $t("userAdmin.tip.globalRole") }}</td>
      </tr>

      <tr v-if="metadata.mfaEnabled && userCredentials">
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

    <div class="showinguser" v-if="userBeingEdited" v-cloak>
      <p>{{ $t("userAdmin.userMemberOf") }}</p>
      <table class="table table-bordered table-hover">
        <thead class="thead-light">
          <tr>
            <th style="width:30%">{{ $t("common.weblog") }}</th>
            <th style="width:10%">{{ $t("userAdmin.pending") }}</th>
            <th style="width:10%">{{ $t("common.role") }}</th>
            <th style="width:25%">{{ $t("common.edit") }}</th>
            <th width="width:25%">{{ $t("userAdmin.manage") }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="weblogRole in userBlogList" v-bind:key="weblogRole.handle">
            <td>
              <a v-bind:href="weblogRole.weblog.absoluteURL">
                {{ weblogRole.weblog.name }} [{{ weblogRole.weblog.handle }}]
              </a>
            </td>
            <td>
              {{ weblogRole.pending }}
            </td>
            <td>
              {{ weblogRole.weblogRole }}
            </td>
            <td>
              <img src="@/assets/page_white_edit.png" />
              <a
                target="_blank"
                v-bind:href="entriesUrl + '?weblogId=' + weblogRole.weblog.id"
              >
                {{ $t("userAdmin.editEntries") }}
              </a>
            </td>
            <td>
              <img src="@/assets/page_white_edit.png" />
              <a
                target="_blank"
                v-bind:href="
                  weblogConfigUrl + '?weblogId=' + weblogRole.weblog.id
                "
              >
                {{ $t("userAdmin.manage") }}
              </a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <br />
    <br />

    <div class="control" v-show="userBeingEdited" v-cloak>
      <button type="button" class="buttonBox" v-on:click="updateUser()">
        {{ $t("common.save") }}
      </button>
      <button type="button" class="buttonBox" v-on:click="cancelChanges()">
        {{ $t("common.cancel") }}
      </button>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";

export default {
  data() {
    return {
      urlRoot: "/tb-ui/admin/rest/useradmin/",
      entriesUrl: "/tb-ui/app/authoring/entries",
      weblogConfigUrl: "/tb-ui/app/authoring/weblogConfig",
      pendingList: {},
      userList: {},
      userToEdit: null,
      userBeingEdited: null,
      userCredentials: null,
      userBlogList: {},
      successMessage: null,
      errorObj: {}
    };
  },
  computed: {
    ...mapState("staticProperties", {
      metadata: state => state.items
    })
  },
  methods: {
    ...mapActions({
      loadStaticProperties: "staticProperties/loadStaticProperties"
    }),
    loadStaticProps: function() {
      // https://stackoverflow.com/a/49284879
      this.loadStaticProperties().then(
        () => {},
        error => this.commonErrorResponse(error, null)
      );
    },
    getPendingRegistrations: function() {
      this.axios.get(this.urlRoot + "registrationapproval").then(response => {
        this.pendingList = response.data;
      });
    },
    loadUserList: function() {
      this.axios
        .get(this.urlRoot + "userlist")
        .then(response => {
          this.userList = response.data;
        })
        .catch(error => this.commonErrorResponse(error, null));
    },
    approveUser: function(userId) {
      this.processRegistration(userId, "approve");
    },
    declineUser: function(userId) {
      this.processRegistration(userId, "reject");
    },
    processRegistration: function(userId, command) {
      this.messageClear();
      this.axios
        .post(this.urlRoot + "registrationapproval/" + userId + "/" + command)
        .then(() => {
          this.getPendingRegistrations();
          this.loadUserList();
        })
        .catch(error => this.commonErrorResponse(error, null));
    },
    loadUser: function() {
      this.messageClear();

      if (!this.userToEdit) {
        return;
      }

      this.axios
        .get(this.urlRoot + "user/" + this.userToEdit)
        .then(response => {
          this.userBeingEdited = response.data.user;
          if (
            Object.prototype.hasOwnProperty.call(response.data, "credentials")
          ) {
            this.userCredentials = response.data.credentials;
          } else {
            this.userCredentials = null;
          }
        });

      this.axios
        .get(this.urlRoot + "user/" + this.userToEdit + "/weblogs")
        .then(response => {
          this.userBlogList = response.data;
        });
    },
    updateUser: function() {
      this.messageClear();
      var userData = {};
      userData.user = this.userBeingEdited;
      userData.credentials = this.userCredentials;

      this.axios
        .put(this.urlRoot + "user/" + this.userBeingEdited.id, userData)
        .then(response => {
          this.userBeingEdited = response.data.user;
          this.userCredentials = response.data.credentials;
          this.loadUserList();
          this.getPendingRegistrations();
          this.successMessage =
            "User [" + this.userBeingEdited.screenName + "] updated.";
        })
        .catch(error => this.commonErrorResponse(error, null));
    },
    cancelChanges: function() {
      this.messageClear();
      this.userBeingEdited = null;
      this.userCredentials = null;
    },
    messageClear: function() {
      this.successMessage = null;
      this.errorObj = {};
    },
    commonErrorResponse: function(error, errorMsg) {
      if (errorMsg) {
        this.errorObj[0] = errorMsg;
      } else if (error && error.response && error.response.status == 401) {
        console.log("Redirecting...");
        window.location.href = "/tb-ui/app/login";
      } else if (error && error.response) {
        this.errorObj = error.response.data;
      } else if (error) {
        this.errorObj[0] = error;
      } else {
        this.errorObj[0] = "System error.";
      }
    }
  },
  created: function() {
    this.loadStaticProps();
    this.getPendingRegistrations();
    this.loadUserList();
  }
};
</script>
