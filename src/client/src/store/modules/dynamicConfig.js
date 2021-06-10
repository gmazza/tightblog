import axios from "axios";

export default {
  namespaced: true,
  state: {
    items: {},
    metadata: {},
    urlRoot: "/tb-ui/admin/rest/server/"
  },
  getters: {
    dynamicConfig: state => {
      return state.items;
    }
  },
  mutations: {
    setDynamicConfig(state, dynamicConfig) {
      state.items = dynamicConfig;
    }
  },
  actions: {
    loadDynamicConfig({ commit }) {
      return new Promise((resolve, reject) => {
        axios
        .get('/tb-ui/admin/rest/server/webloggerproperties')
        .then(response => {
          commit("setDynamicConfig", response.data);
          resolve();
        })
        .catch(error => reject(error));
      });
    },
    saveDynamicConfig({ dispatch }, webloggerProps) {
      return new Promise((resolve, reject) => {
        axios
        .post('/tb-ui/admin/rest/server/webloggerproperties', webloggerProps)
        .then(() => {
          resolve();
          dispatch("loadDynamicConfig");
        })
        .catch(error => reject(error));
      });
    }
  }
};
