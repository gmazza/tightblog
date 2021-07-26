import axios from 'axios'

export default {
  namespaced: true,
  state: {
    webloggerProperties: {},
    weblogList: {},
    userList: {}
  },
  getters: {
    getWebloggerProperties: state => {
      return state.webloggerProperties
    },
    getWeblogList: state => {
      return state.weblogList
    },
    getUserList: state => {
      return state.userList
    }
  },
  mutations: {
    setWebloggerProperties (state, webloggerProperties) {
      state.webloggerProperties = webloggerProperties
    },
    setWeblogList (state, weblogList) {
      state.weblogList = weblogList
    },
    setUserList (state, userList) {
      state.userList = userList
    }
  },
  actions: {
    loadWebloggerProperties ({ commit }) {
      return new Promise((resolve, reject) => {
        axios
          .get('/tb-ui/app/any/webloggerproperties')
          .then(response => {
            commit('setWebloggerProperties', response.data)
            resolve()
          })
          .catch(error => reject(error))
      })
    },
    saveWebloggerProperties ({ dispatch }, webloggerProps) {
      return new Promise((resolve, reject) => {
        axios
          .post('/tb-ui/admin/rest/server/webloggerproperties', webloggerProps)
          .then(() => {
            resolve()
            dispatch('loadWebloggerProperties')
          })
          .catch(error => reject(error))
      })
    },
    loadWeblogList ({ commit }) {
      return new Promise((resolve, reject) => {
        axios
          .get('/tb-ui/admin/rest/server/webloglist')
          .then(response => {
            commit('setWeblogList', response.data)
            resolve()
          })
          .catch(error => reject(error))
      })
    },
    loadUserList ({ commit }) {
      return new Promise((resolve, reject) => {
        axios
          .get('/tb-ui/admin/rest/useradmin/userlist')
          .then(response => {
            commit('setUserList', response.data)
            resolve()
          })
          .catch(error => reject(error))
      })
    }
  }
}
