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
    <p v-html="$t('scanCode.relogin', { loginURL: '/tb-ui/app/relogin' })"></p>
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
        .get("/tb-ui/newuser/rest/newqrcode")
        .then((response) => {
          this.qrCode = response.data.qrCode;
        })
        .catch((error) => this.commonErrorResponse(error));
    },
    commonErrorResponse: function (error) {
      if (error.response.status === 401) {
        window.location.href = "/tb-ui/app/login";
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
