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
-->
<template>
  <div v-if="asyncDataStatus_ready">
    <AppTitleBar :mfaRegistration="true" />
    <AppErrorListMessageBox
      :in-error-obj="errorObj"
      @close-box="errorObj.errors = null"
    ></AppErrorListMessageBox>

    <h2>{{ $t("scanCode.title") }}</h2>
    <p class="pagetip" v-html="$t('scanCode.tip')"></p>

    <img v-bind:src="qrCode" />
    <p v-html="$t('scanCode.relogin', { loginURL: process.env.VUE_APP_PUBLIC_PATH + '/app/relogin' })"></p>
  </div>
</template>

<script>
import asyncDataStatus from "@/mixins/AsyncDataStatus";

export default {
  data() {
    return {
      qrCode: null,
      errorObj: {},
    };
  },
  mixins: [asyncDataStatus],
  methods: {
    loadQRCode: function () {
      this.axios
        .get(process.env.VUE_APP_PUBLIC_PATH + "/newuser/rest/newqrcode")
        .then((response) => {
          this.qrCode = response.data.qrCode;
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = process.env.VUE_APP_PUBLIC_PATH + "/app/login";
      } else {
        this.errorObj = error.response.data;
      }
    },
  },
  async created() {
    await this.loadQRCode();
    this.asyncDataStatus_fetched();
  },
};
</script>

<style></style>
