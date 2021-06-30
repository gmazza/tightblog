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
      <AppSuccessMessageBox :message="successMessage" @close-box="successMessage = null"></AppSuccessMessageBox>
      <AppErrorListMessageBox :in-error-obj="errorObj" @close-box="errorObj.errors = null"></AppErrorListMessageBox>

    <h1>{{ $t(varText.pageTitleKey) }}</h1>
    <p class="subtitle">{{ $t(varText.subtitleKey) }}</p>
    <table class="formtable">
      <tr>
          <td class="label"><label for="userName">{{ $t("userEdit.username") }}</label></td>
          <td class="field">
            <span v-if="sessionInfo.authenticatedUser == null">
                <input id="userName" type="text" size="30" v-model="userBeingEdited.userName" minlength="5" maxlength="25">
            </span>
            <span v-else>
                <input id="userName" type="text" size="30" v-model="userBeingEdited.userName" readonly>
            </span>
          </td>
          <td class="description">{{ $t(varText.usernameTipKey) }}</td>
      </tr>

      <tr>
          <td class="label"><label for="screenName">{{ $t("userEdit.screenname") }}</label></td>
          <td class="field"><input id="screenName" type="text" size="30" v-model="userBeingEdited.screenName" minlength="3" maxlength="30"></td>
          <td class="description">{{ $t("userEdit.tip.screenname") }}</td>
      </tr>

      <tr>
          <td class="label"><label for="emailAddress">{{ $t("userEdit.email") }}</label></td>
          <td class="field"><input id="emailAddress" type="email" size="40" v-model="userBeingEdited.emailAddress" maxlength="40"></td>
          <td class="description">{{ $t("userEdit.tip.email") }}</td>
      </tr>

        <tr>
          <td class="label"><label for="passwordText">{{ $t("userEdit.password") }}</label></td>
            <td class="field">
                <input id="passwordText" type="password" size="20" v-model="userCredentials.passwordText" minlength="8" maxlength="20" autocomplete="new-password">
            </td>
            <td class="description">{{ $t(varText.passwordTipKey) }}</td>
        </tr>
        <tr>
            <td class="label"><label for="passwordConfirm">{{ $t("userEdit.passwordConfirm") }}</label></td>
            <td class="field">
                <input id="passwordConfirm" type="password" size="20" v-model="userCredentials.passwordConfirm" minlength="8" maxlength="20" autocomplete="new-password">
            </td>
            <td class="description"></td>
        </tr>
    </table>

    <br/>

    <div class="control" v-show="!hideButtons">
        <button type="button" class="buttonBox" v-on:click="updateUser()">{{ $t(varText.saveButtonText) }}</button>
        <button type="button" class="buttonBox" v-on:click="cancelChanges()">{{ $t("common.cancel") }}</button>
    </div>

  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";

export default {
  data: function () {
    return  {
      userBeingEdited: {},
      userCredentials: {},
      hideButtons: false,
      urlRoot: "/tb-ui/admin/rest/useradmin/",
      successMessage: null,
      errorObj: {}
    }
  },
  computed: {
    ...mapState("sessionInfo", {
      sessionInfo: state => state.items
    }),
    varText: function () {
      if (this.sessionInfo.authenticatedUser == null) {
        return {
          usernameTipKey: "userAdd.tip.username",
          passwordTipKey: "userAdd.tip.password",
          saveButtonText: "userAdd.saveButton",
          pageTitleKey: "userAdd.title",
          subtitleKey: "userAdd.subtitle"
        }
      } else {
        return {
          usernameTipKey: "userEdit.tip.username",
          passwordTipKey: "userEdit.tip.password",
          saveButtonText: "common.save",
          pageTitleKey: "userEdit.title",
          subtitleKey: "userEdit.subtitle"
        }
      }
    }
  },
  methods: {
    ...mapActions({
      loadSessionInfo: "sessionInfo/loadSessionInfo"
    }),
    loadUser: function (userId) {
      this.axios.get("/tb-ui/authoring/rest/userprofile/" + userId)
        .then(response => {
          this.userBeingEdited = response.data;
          this.userCredentials = {};
      })
    },
    updateUser: function () {
      this.messageClear();
      var userData = {};
      userData.user = this.userBeingEdited;
      userData.credentials = this.userCredentials;
      var urlToUse = (this.sessionInfo.authenticatedUser ? "/tb-ui/authoring/rest/userprofile/" + this.sessionInfo.authenticatedUser.id
          : "/tb-ui/register/rest/registeruser");

      this.axios
        .post(urlToUse, userData)
        .then(response => {
          this.userBeingEdited = response.data.user;
          if (!this.sessionInfo.authenticatedUser) {
            // user registration, hide the save/cancel buttons now
            this.hideButtons = true;
            if (this.userBeingEdited.status == "ENABLED") {
              this.successMessage = this.$t("userEdit.userCanLogIn");
            } else if (this.userBeingEdited.status == "REGISTERED") {
              this.successMessage = this.$t("userEdit.userNotActivated");
            }
          } else {
            // user profile update
            this.successMessage = this.$t("common.changesSaved");
          }
          this.userCredentials = {};
        })
        .catch(error => this.commonErrorResponse(error));
    },
    cancelChanges: function() {
      this.messageClear();
      this.userBeingEdited = null;
      this.credentials = {};
    },
    messageClear: function() {
      this.errorObj = {};
      this.showSuccessMessage = false;
    },
    commonErrorResponse: function(error) {
      if (error.response.status == 401) {
        window.location.href = "/tb-ui/app/login";
      } else {
        this.errorObj = error.response.data;
      }
    }
  },
  mounted: function() {
    this.loadSessionInfo();
    if (this.sessionInfo.authenticatedUser != null) {
      this.loadUser(this.sessionInfo.authenticatedUser.id);
    }
  }
};
</script>
