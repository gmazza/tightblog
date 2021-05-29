import Vue from "vue";
import Vuex from "vuex";
import caches from "./modules/caches";
import globalConfig from "./modules/globalConfig";
import staticProperties from "./modules/staticProperties";

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    caches,
    globalConfig,
    staticProperties
  },
  state: {},
  mutations: {},
  actions: {}
});
