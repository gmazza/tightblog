import { loadDataUsingAxios } from "@/store";

export default {
  namespaced: true,
  state: {
    lookupValues: [],
    startupConfig: [],
  },
  getters: {
    getLookupValues: (state) => {
      return state.lookupValues;
    },
    getStartupConfig: (state) => {
      return state.startupConfig;
    },
  },
  mutations: {
    setLookupValues(state, lookupValues) {
      state.lookupValues = lookupValues;
    },
    setStartupConfig(state, startupConfig) {
      state.startupConfig = startupConfig;
    },
  },
  actions: {
    loadLookupValues({ commit }) {
      if (!this.getLookupValues || this.getLookupValues.length === 0) {
        loadDataUsingAxios({
          commit: commit,
          url: process.env.VUE_APP_PUBLIC_PATH + "/app/authoring/lookupvalues",
          setter: "setLookupValues",
        });
      }
    },
    async loadStartupConfig({ commit }) {
      if (!this.getStartupConfig || this.getStartupConfig.length === 0) {
        loadDataUsingAxios({
          commit: commit,
          url: process.env.VUE_APP_PUBLIC_PATH + "/app/authoring/startupconfig",
          setter: "setStartupConfig",
        });
      }
    },
  },
};
