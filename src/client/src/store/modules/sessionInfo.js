import axios from "axios";

export default {
  namespaced: true,
  state: {
    items: {}
  },
  getters: {
    getSessionInfo: state => {
      return state.items;
    }
  },
  mutations: {
    setSessionInfo(state, sessionInfo) {
      state.items = sessionInfo;
    }
  },
  actions: {
    loadSessionInfo({ commit }) {
      return new Promise((resolve, reject) => {
        axios
        .get("/tb-ui/app/any/sessioninfo")
          .then(response => {
            commit("setSessionInfo", response.data);
            resolve();
          })
          .catch(error => reject(error));
      });
    }
  }
};
