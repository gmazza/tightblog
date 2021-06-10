import Vue from "vue";
import Vuex from "vuex";
import caches from "./modules/caches";
import dynamicConfig from "./modules/dynamicConfig";
import startupConfig from "./modules/startupConfig";
//import sessionConfig from "./modules/sessionConfig";

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    caches,
    startupConfig,
    dynamicConfig
//    sessionConfig
  },
  state: {},
  mutations: {},
  actions: {}
});
