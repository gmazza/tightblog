import axios from "axios";
import { loadDataUsingAxios } from "@/store";
import { findById, upsert } from "@/helpers";

export default {
  namespaced: true,
  state: {
    userWeblogRoles: [],
    items: {},
    userWeblogs: [],
  },
  getters: {
    getSessionInfo: (state) => {
      return state.items;
    },
    getUserWeblogRoles: (state) => {
      return state.userWeblogRoles;
    },
    getWeblog: (state) => {
      return (id) => {
        const weblog = findById(state.userWeblogs, id);
        if (!weblog) return null;
        return {
          ...weblog,
        };
      };
    },
    weblog: (state) => {
      return (id) => {
        const weblog = findById(state.userWeblogs, id);
        if (!weblog) return null;
        return {
          ...weblog,
        };
      };
    },
  },
  mutations: {
    setSessionInfo(state, sessionInfo) {
      state.items = sessionInfo;
    },
    setUserWeblogRoles(state, userWeblogRoles) {
      state.userWeblogRoles = userWeblogRoles;
    },
    setItem(state, { resource, item }) {
      upsert(state[resource], item);
    },
    deleteItem(state, { resource, itemId }) {
      const index = findById(state[resource], itemId);
      if (index >= 0) {
        // (index, how many to remove, [items to add])
        state[resource].splice(index, 1);
      }
    },
  },
  actions: {
    loadSessionInfo({ commit }) {
      loadDataUsingAxios({
        commit: commit,
        url: process.env.VUE_APP_PUBLIC_PATH + "/app/any/sessioninfo",
        setter: "setSessionInfo",
      });
    },
    loadUserWeblogRoles({ commit }) {
      loadDataUsingAxios({
        commit: commit,
        url: process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/loggedinuser/weblogs",
        setter: "setUserWeblogRoles",
      });
    },
    async fetchWeblog({ dispatch, getters }, weblogId) {
      const weblog = getters.weblog(weblogId);
      if (weblog != null) {
        return weblog;
      } else {
        return await dispatch("refreshWeblog", weblogId);
      }
    },
    refreshWeblog({ commit }, weblogId) {
      return new Promise((resolve, reject) => {
        axios
          .get(process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/weblog/" + weblogId)
          .then((response) => {
            const fetchedWeblog = response.data;
            commit("setItem", {
              resource: "userWeblogs",
              item: fetchedWeblog,
            });
            resolve(fetchedWeblog);
          })
          .catch((error) => reject(error));
      });
    },
    upsertWeblog({ commit, dispatch }, weblog) {
      const urlToUse = weblog.id
        ? process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/weblog/" + weblog.id
        : process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/weblogs";
      return new Promise((resolve, reject) => {
        axios
          .post(urlToUse, weblog)
          .then((response) => {
            const updatedWeblog = response.data;
            commit("setItem", { resource: "userWeblogs", item: updatedWeblog });
            dispatch("loadUserWeblogRoles");
            resolve(updatedWeblog);
          })
          .catch((error) => reject(error));
      });
    },
    deleteWeblog({ commit, dispatch }, weblogId) {
      return new Promise((resolve, reject) => {
        axios
          .delete(process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/weblog/" + weblogId)
          .then(() => {
            commit("userWeblogs", {
              resource: "userWeblogs",
              itemId: weblogId,
            });
            dispatch("loadUserWeblogRoles");
            resolve();
          })
          .catch((error) => reject(error));
      });
    },
    detachUserFromWeblog({ dispatch }, weblogId) {
      return new Promise((resolve, reject) => {
        axios
          .post(process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/weblogrole/" + weblogId + "/detach")
          .then(() => {
            dispatch("loadUserWeblogRoles");
            resolve();
          })
          .catch((error) => reject(error));
      });
    },
    setReceiveCommentsForWeblog({ dispatch }, role) {
      return new Promise((resolve, reject) => {
        axios
          .post(
            process.env.VUE_APP_PUBLIC_PATH + "/authoring/rest/weblogrole/" +
              role.id +
              "/emails/" +
              role.emailComments
          )
          .then(() => {
            dispatch("loadUserWeblogRoles");
            resolve();
          })
          .catch((error) => reject(error));
      });
    },
  },
};
