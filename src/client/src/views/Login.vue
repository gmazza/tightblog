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
    <AppErrorMessageBox
      :message="errorMessage"
      @close-box="errorMessage = null"
    />

    <p v-if="lookupVals.mfaEnabled">{{ $t("login.mfaPrompt") }}</p>
    <p v-else>{{ $t("login.prompt") }}</p>

    <form @submit.prevent="signIn" id="loginForm">
      <table width="80%">
        <tr>
          <td width="20%" align="right">
            {{ $t("login.username") }}
          </td>
          <td width="80%">
            <input
              v-model.trim="form.j_username"
              type="text"
              autocomplete="username"
              size="25"
            />
          </td>
        </tr>

        <tr>
          <td width="20%" align="right">
            {{ $t("login.password") }}
          </td>
          <td width="80%">
            <!-- my password for local development -->
            <input
              v-model.trim="form.j_password"
              type="password"
              autocomplete="current-password"
              size="20"
            />
          </td>
        </tr>

        <tr v-if="lookupVals.mfaEnabled">
          <td width="20%" align="right">
            {{ $t("login.mfaCode") }}
          </td>
          <td width="80%">
            <input
              v-model.trim="form.totpCode"
              type="text"
              size="20"
              autocomplete="off"
            />
          </td>
        </tr>

        <tr>
          <td width="20%"></td>
          <td width="80%">
            <input type="submit" :value="$t('login.login')" />
          </td>
        </tr>
      </table>
    </form>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";

export default {
  data() {
    return {
      form: {
        j_username: "gmazza",
        j_password: "1@Password",
        totpCode: "",
      },
      successMessage: null,
      errorMessage: null,
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
    async signIn() {
      this.axios
        .post("https://localhost:8443/tb_j_security_check", this.form)
        .then(this.$router.push({ name: "myBlogs" }))
        .catch((error) => {
          this.errorMessage = error;
        });
    },
  },
  async created() {
    // await this.loadLookupValues();
  },
};
</script>

<style></style>
