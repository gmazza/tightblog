import axios from "axios";

export default {
  namespaced: true,
  state: {
    items: {},
    metadata: {},
    urlRoot: "/tb-ui/admin/rest/server/"
  },
  getters: {
    globalConfig: state => {
      return state.items;
    },
    metadata: state => {
      return state.metadata;
    }
  },
  mutations: {
    setGlobalConfig(state, globalConfig) {
      state.items = globalConfig;
    },
    setMetadata(state, metadata) {
      state.metadata = metadata;
    },
  },
  actions: {
    loadGlobalConfig({ commit }) {
      return new Promise((resolve, reject) => {
        axios
        .get('/tb-ui/admin/rest/server/webloggerproperties')
        .then(response => {
          commit("setGlobalConfig", response.data);
          resolve();
        })
        .catch(error => reject(error));
      });
    },
    loadMetadata({ commit }) {
      return new Promise((resolve, reject) => {
        axios
        .get('/tb-ui/admin/rest/server/globalconfigmetadata')
        .then(response => {
          commit("setMetadata", response.data);
          resolve();
        })
        .catch(error => reject(error));
      });
    },
    saveGlobalConfig({ dispatch }, webloggerProps) {
      return new Promise((resolve, reject) => {
        axios
        .post('/tb-ui/admin/rest/server/webloggerproperties', webloggerProps)
        .then(() => {
          resolve();
          dispatch("loadGlobalConfig");
        })
        .catch(error => reject(error));
      });
    }
  }
};
