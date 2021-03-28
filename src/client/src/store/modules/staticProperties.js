import axios from "axios";

export default {
  namespaced: true,
  state: {
    items: [],
    apiUrl: "/tb-ui/authoring/rest/server/staticproperties"
  },
  getters: {},
  mutations: {
    setStaticProperties(state, properties) {
      state.items = properties;
    }
  },
  actions: {
    loadStaticProperties({ commit, state }) {
      return new Promise((resolve, reject) => {
        axios
          .get(state.apiUrl)
          .then(response => {
            commit("setStaticProperties", response.data);
            resolve();
          })
          .catch(error => reject(error));
      });
    }
  }
};
