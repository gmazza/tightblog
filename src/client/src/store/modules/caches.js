import axios from "axios";
import { loadDataUsingAxios } from "@/store";

export default {
  namespaced: true,
  state: {
    items: [],
    urlRoot: "/tb-ui/admin/rest/server/",
  },
  getters: {},
  mutations: {
    setCaches(state, caches) {
      state.items = caches;
    },
  },
  actions: {
    loadCaches({ state, commit }) {
      loadDataUsingAxios({
        commit: commit,
        url: state.urlRoot + "caches",
        setter: "setCaches",
      });
    },
    clearCacheEntry({ state, dispatch }, cacheItem) {
      return new Promise((resolve, reject) => {
        axios
          .post(state.urlRoot + "cache/" + cacheItem + "/clear")
          .then(() => {
            return dispatch("loadCaches");
          })
          .catch((error) => reject(error));
      });
    },
  },
};
