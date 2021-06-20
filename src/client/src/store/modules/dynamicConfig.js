import axios from "axios";

export default {
  namespaced: true,
  state: {
    webloggerProperties: {},
    weblogList: {}
  },
  getters: {
    getWebloggerProperties: state => {
      return state.webloggerProperties;
    },
    getWeblogList: state => {
      return state.weblogList;
    },
  },
  mutations: {
    setWebloggerProperties(state, webloggerProperties) {
      state.webloggerProperties = webloggerProperties;
    },
    setWeblogList(state, weblogList) {
      state.weblogList = weblogList;
    }
  },
  actions: {
    loadWebloggerProperties({ commit }) {
      return new Promise((resolve, reject) => {
        axios
        .get('/tb-ui/admin/rest/server/webloggerproperties')
        .then(response => {
          commit("setWebloggerProperties", response.data);
          resolve();
        })
        .catch(error => reject(error));
      });
    },
    saveWebloggerProperties({ dispatch }, webloggerProps) {
      return new Promise((resolve, reject) => {
        axios
        .post('/tb-ui/admin/rest/server/webloggerproperties', webloggerProps)
        .then(() => {
          resolve();
          dispatch("loadWebloggerProperties");
        })
        .catch(error => reject(error));
      });
    },
    loadWeblogList({ commit }) {
      return new Promise((resolve, reject) => {
        axios
        .get('/tb-ui/admin/rest/server/webloglist')
        .then(response => {
          commit("setWeblogList", response.data);
          resolve();
        })
        .catch(error => reject(error));
      });
    }
  }
};
