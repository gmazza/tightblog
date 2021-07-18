import axios from "axios";

export default {
  namespaced: true,
  state: {
    lookupValues: [],
    startupConfig: []
  },
  getters: {
    getLookupValues: state => {
      return state.lookupValues;
    },
    getStartupConfig: state => {
      return state.startupConfig;
    }
  },
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
      if (!this.getLookupValues || this.getLookupValues.length == 0) {
        return new Promise((resolve, reject) => {
        axios
          .get("/tb-ui/app/authoring/lookupvalues")
          .then(response => {
            commit("setLookupValues", response.data);
            resolve();
          })
          .catch(error => reject(error));
        });
      }
    },
    loadStartupConfig({ commit }) {
      if (!this.getStartupConfig || this.getStartupConfig.length == 0) {
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
  }
};
