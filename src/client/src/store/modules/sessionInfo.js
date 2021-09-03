import axios from "axios";
import { loadDataUsingAxios } from "@/store";

export default {
  namespaced: true,
  state: {
    userWeblogRoles: [],
    items: {},
  },
  getters: {
    getSessionInfo: (state) => {
      return state.items;
    },
    getUserWeblogRoles: (state) => {
      return state.userWeblogRoles;
    },
  },
  mutations: {
    setSessionInfo(state, sessionInfo) {
      state.items = sessionInfo;
    },
    setUserWeblogRoles(state, userWeblogRoles) {
      state.userWeblogRoles = userWeblogRoles;
    },
  },
  actions: {
    loadSessionInfo({ commit }) {
      loadDataUsingAxios({
        commit: commit,
        url: "/tb-ui/app/any/sessioninfo",
        setter: "setSessionInfo",
      });
    },
    loadUserWeblogRoles({ commit }) {
      loadDataUsingAxios({
        commit: commit,
        url: "/tb-ui/authoring/rest/loggedinuser/weblogs",
        setter: "setUserWeblogRoles",
      });
    },
    detachUserFromWeblog({ dispatch }, weblogId) {
      return new Promise((resolve, reject) => {
        axios
          .post("/tb-ui/authoring/rest/weblogrole/" + weblogId + "/detach")
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
            "/tb-ui/authoring/rest/weblogrole/" +
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
