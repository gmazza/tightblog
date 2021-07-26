import axios from 'axios'

export default {
  namespaced: true,
  state: {
    userWeblogRoles: [],
    items: {}
  },
  getters: {
    getSessionInfo: state => {
      return state.items
    },
    getUserWeblogRoles: state => {
      return state.userWeblogRoles
    }
  },
  mutations: {
    setSessionInfo (state, sessionInfo) {
      state.items = sessionInfo
    },
    setUserWeblogRoles (state, userWeblogRoles) {
      state.userWeblogRoles = userWeblogRoles
    }
  },
  actions: {
    loadSessionInfo ({ commit }) {
      return new Promise((resolve, reject) => {
        axios
          .get('/tb-ui/app/any/sessioninfo')
          .then(response => {
            commit('setSessionInfo', response.data)
            resolve()
          })
          .catch(error => reject(error))
      })
    },
    loadUserWeblogRoles ({ commit }) {
      return new Promise((resolve, reject) => {
        axios.get('/tb-ui/authoring/rest/loggedinuser/weblogs')
          .then(response => {
            commit('setUserWeblogRoles', response.data)
            resolve()
          })
          .catch(error => reject(error))
      })
    },
    detachUserFromWeblog ({ dispatch }, weblogId) {
      return new Promise((resolve, reject) => {
        axios.post('/tb-ui/authoring/rest/weblogrole/' + weblogId + '/detach')
          .then(() => {
            dispatch('loadUserWeblogRoles')
            resolve()
          })
          .catch(error => reject(error))
      })
    },
    setReceiveCommentsForWeblog ({ dispatch }, role) {
      return new Promise((resolve, reject) => {
        axios.post('/tb-ui/authoring/rest/weblogrole/' + role.id + '/emails/' + role.emailComments)
          .then(() => {
            dispatch('loadUserWeblogRoles')
            resolve()
          })
          .catch(error => reject(error))
      })
    }
  }
}
