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
  <div v-if="!isFetching">
    <AppTitleBar :mfaRegistration="true" />
    <AppErrorListMessageBox
      :in-error-obj="errorObj"
      @close-box="errorObj.errors = []"
    ></AppErrorListMessageBox>

    <h2>{{ $t('scanCode.title') }}</h2>
    <p class="pagetip" v-html="$t('scanCode.tip')"></p>

    <img v-if="qrCode" :src="`data:image/png;base64,${qrCode}`" alt="QR Code" />
    <p v-html="$t('scanCode.relogin', { loginURL: loginUrl })"></p>
  </div>
</template>

<script lang="ts">
import { AxiosError } from 'axios'
import api from '@/api'

export default {
  data() {
    return {
      qrCode: null as string | null,
      loginUrl: import.meta.env.VITE_PUBLIC_PATH + '/app/relogin',
      errorObj: {
        errors: []
      },
      isFetching: true
    }
  },
  methods: {
    loadQRCode: function () {
      api
        .loadQRCode()
        .then((qrCode) => {
          this.qrCode = qrCode
          console.log('QR Code loaded successfully:', this.qrCode)
        })
        .catch((error) => this.commonErrorResponse(error))
    },
    commonErrorResponse: function (error: unknown) {
      if (error instanceof AxiosError && error.response?.status === 401) {
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/login'
      } else if (error instanceof AxiosError && error.response?.data) {
        this.errorObj = error.response.data
      }
    }
  },
  async created() {
    await this.loadQRCode()
    this.isFetching = false
  }
}
</script>

<style></style>
