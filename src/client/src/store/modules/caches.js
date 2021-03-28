import axios from "axios";

export default {
  namespaced: true,
  state: {
    items: [],
    urlRoot: "/tb-ui/admin/rest/server/"
  },
  getters: {},
  mutations: {
    setCaches(state, caches) {
      state.items = caches;
    }
  },
  actions: {
    loadCaches({ commit, state }) {
      return new Promise((resolve, reject) => {
        axios
          .get(state.urlRoot + "caches")
          .then(response => {
            commit("setCaches", response.data);
            resolve();
          })
          .catch(error => reject(error));
      });
    },
    clearCacheEntry({ state, dispatch }, cacheItem) {
      return new Promise((resolve, reject) => {
        axios
          .post(state.urlRoot + "cache/" + cacheItem + "/clear")
          .then(() => {
            return dispatch("loadCaches");
          })
          .catch(error => reject(error));
      });
    }
  }
};
