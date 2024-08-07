import { defineStore } from 'pinia'
import api from '@/api'
import type { User, Weblog, WebloggerProperties } from '@/types'

type State = {
  webloggerProperties: WebloggerProperties
  weblogList: Weblog[]
  userList: User[]
}

export const useDynamicConfigStore = defineStore('dynamicConfig', {
  state: (): State => ({
    webloggerProperties: {},
    weblogList: [],
    userList: []
  }),
  getters: {
    getWebloggerProperties(): WebloggerProperties {
      return this.webloggerProperties
    },
    getWeblogList(): Weblog[] {
      return this.weblogList
    },
    getUserList(): User[] {
      return this.userList
    }
  },
  actions: {
    async loadWebloggerProperties() {
      await api
        .loadWebloggerProperties()
        .then((result) => (this.webloggerProperties = result.data))
        .catch((error) => console.error('Load weblogger properties error', error))
    },
    async loadWeblogList() {
      await api
        .loadWeblogList()
        .then((result) => (this.weblogList = result.data))
        .catch((error) => console.error('Load weblog list error', error))
    },
    async loadUserList() {
      await api
        .loadUserList()
        .then((result) => (this.userList = result.data))
        .catch((error) => console.error('Load user list error', error))
    },
    async saveWebloggerProperties(webloggerProps: WebloggerProperties) {
      await api.saveWebloggerProperties(webloggerProps)
      await this.loadWebloggerProperties()
    }
  }
})
