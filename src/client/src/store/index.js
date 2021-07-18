import Vue from "vue";
import Vuex from "vuex";
import caches from "./modules/caches";
import dynamicConfig from "./modules/dynamicConfig";
import startupConfig from "./modules/startupConfig";
import sessionInfo from "./modules/sessionInfo";

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    caches,
    startupConfig,
    dynamicConfig,
    sessionInfo
  },
  strict: process.env.NODE_ENV !== 'production',
  state: {},
  mutations: {},
  actions: {}
});
