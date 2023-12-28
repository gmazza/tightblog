import axios from "axios";
import { loadDataUsingAxios } from "@/store";

export default {
  namespaced: true,
  state: {
    webloggerProperties: {},
    weblogList: [],
    userList: [],
  },
  getters: {
    getWebloggerProperties: (state) => {
      return state.webloggerProperties;
    },
    getWeblogList: (state) => {
      return state.weblogList;
    },
    getUserList: (state) => {
      return state.userList;
    },
  },
  mutations: {
    setWebloggerProperties(state, webloggerProperties) {
      state.webloggerProperties = webloggerProperties;
    },
    setWeblogList(state, weblogList) {
      state.weblogList = weblogList;
    },
    setUserList(state, userList) {
      state.userList = userList;
    },
  },
  actions: {
    async loadWebloggerProperties({ commit }) {
      await loadDataUsingAxios({
        commit: commit,
        url: process.env.VUE_APP_PUBLIC_PATH + "/app/any/webloggerproperties",
        setter: "setWebloggerProperties",
      });
    },
    async loadWeblogList({ commit }) {
      await loadDataUsingAxios({
        commit: commit,
        url: process.env.VUE_APP_PUBLIC_PATH + "/admin/rest/server/webloglist",
        setter: "setWeblogList",
      });
    },
    async loadUserList({ commit }) {
      await loadDataUsingAxios({
        commit: commit,
        url: process.env.VUE_APP_PUBLIC_PATH + "/admin/rest/useradmin/userlist",
        setter: "setUserList",
      });
    },
    saveWebloggerProperties({ dispatch }, webloggerProps) {
      return new Promise((resolve, reject) => {
        axios
          .post(process.env.VUE_APP_PUBLIC_PATH + "/admin/rest/server/webloggerproperties", webloggerProps)
          .then(() => {
            resolve();
            dispatch("loadWebloggerProperties");
          })
          .catch((error) => reject(error));
      });
    },
  },
};
