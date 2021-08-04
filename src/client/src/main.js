import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import TheNavigation from "@/components/TheNavigation";
import i18n from "./i18n";
import axios from "axios";
import dayjs from "dayjs";
import VueAxios from "vue-axios";
import store from "./store";
import { BootstrapVue, IconsPlugin } from "bootstrap-vue";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";

Vue.use(VueAxios, axios);
Vue.use(BootstrapVue);
Vue.use(IconsPlugin);

Vue.mixin({
  methods: {
    globalHelper: function () {
      alert("Hello world");
    },
    // lodash replacements: https://github.com/you-dont-need/You-Dont-Need-Lodash-Underscore
    globalSortBy: (key) => {
      return (a, b) => (a[key] > b[key] ? 1 : b[key] > a[key] ? -1 : 0);
    },
  },
});

Vue.filter("standard_datetime", function (isoDate) {
  if (!isoDate) return "";
  return dayjs(isoDate).format("DD MMM YYYY h:mm:ss A");
});

// Make all components starting with "App" global
// https://vuejs.org/v2/style-guide/index.html#Base-component-names-strongly-recommended
const requireComponent = require.context(
  "./components",
  true,
  /App[A-Z]\w+\.(vue|js)$/
);
requireComponent.keys().forEach(function (fileName) {
  let baseComponentConfig = requireComponent(fileName);
  baseComponentConfig = baseComponentConfig.default || baseComponentConfig;
  const baseComponentName =
    baseComponentConfig.name ||
    fileName.replace(/^.+\//, "").replace(/\.\w+$/, "");
  Vue.component(baseComponentName, baseComponentConfig);
});

Vue.component(TheNavigation);

Vue.config.devtools = true;

Vue.config.productionTip = false;

new Vue({
  router,
  i18n,
  store,
  render: (h) => h(App),
}).$mount("#app");
