<template>
  <div id="app">
    <router-view v-show="showPage" @ready="showPage = true" />
    <div v-show="!showPage" class="push-top">loading...</div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      showPage: true,
    };
  },
  created() {
    const locale = localStorage.getItem("locale");
    if (locale) {
      this.$i18n.locale = locale;
    }
    /*    this.$router.beforeEach(() => {
      this.showPage = false;
    }); */
  },
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>
