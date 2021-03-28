import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import i18n from "./i18n";
import axios from "axios";
import dayjs from "dayjs";
import VueAxios from "vue-axios";
import store from "./store";

Vue.use(VueAxios, axios);

Vue.filter("standard_datetime", function(isoDate) {
  if (!isoDate) return "";
  return dayjs(isoDate).format("DD MMM YYYY h:mm:ss A");
});

Vue.config.devtools = true;

Vue.config.productionTip = false;

new Vue({
  router,
  i18n,
  store,
  render: h => h(App)
}).$mount("#app");
