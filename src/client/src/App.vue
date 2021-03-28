<template>
  <div id="app">
    <TheNavigation />
    <router-view />
  </div>
</template>

<script>
import TheNavigation from "@/components/TheNavigation";

export default {
  components: {
    TheNavigation
  },
  created() {
    const locale = localStorage.getItem("locale");

    if (locale) {
      this.$i18n.locale = locale;
    }
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
</style>
