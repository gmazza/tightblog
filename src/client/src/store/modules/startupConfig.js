import axios from "axios";

export default {
  namespaced: true,
  state: {
    lookupValues: [],
    startupConfig: []
  },
  getters: {},
  mutations: {
    setLookupValues(state, lookupValues) {
      state.lookupValues = lookupValues;
    },
    setStartupConfig(state, startupConfig) {
      state.startupConfig = startupConfig;
    }
  },
  actions: {
    loadLookupValues({ commit }) {
      return new Promise((resolve, reject) => {
        axios
          .get("/tb-ui/app/authoring/lookupvalues")
          .then(response => {
            commit("setLookupValues", response.data);
            resolve();
          })
          .catch(error => reject(error));
      });
    },
    loadStartupConfig({ commit }) {
      return new Promise((resolve, reject) => {
        axios
        .get('/tb-ui/app/authoring/startupconfig')
        .then(response => {
          commit("setStartupConfig", response.data);
          resolve();
        })
        .catch(error => reject(error));
      });
    }
  }
};
