import Vue from "vue";
import Vuex from "vuex";
import caches from "./modules/caches";
import dynamicConfig from "./modules/dynamicConfig";
import startupConfig from "./modules/startupConfig";
import sessionInfo from "./modules/sessionInfo";
import axios from "axios";

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    caches,
    startupConfig,
    dynamicConfig,
    sessionInfo,
  },
  strict: process.env.NODE_ENV !== "production",
  state: {},
  mutations: {},
  actions: {},
});

export const loadDataUsingAxios = ({ commit, url, setter }) => {
  return new Promise((resolve, reject) => {
    axios
      .get(url)
      .then((response) => {
        commit(setter, response.data);
        resolve();
      })
      .catch((error) => reject(error));
  });
};
