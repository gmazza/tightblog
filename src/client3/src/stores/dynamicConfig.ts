import { defineStore } from 'pinia'
import api2 from '@/api'
import * as api from '@/api'
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
      await api2
        .loadWebloggerProperties()
        .then((result) => (this.webloggerProperties = result))
        .catch((error) => console.error('Load weblogger properties error', error))
    },
    async loadWeblogList() {
      await api2
        .loadWeblogList()
        .then((result) => (this.weblogList = result))
        .catch((error) => console.error('Load weblog list error', error))
    },
    async loadUserList() {
      await api
        .loadUserList()
        .then((result) => (this.userList = result))
        .catch((error) => console.error('Load user list error', error))
    },
    async saveWebloggerProperties(webloggerProps: WebloggerProperties) {
      await api2.saveWebloggerProperties(webloggerProps)
      await this.loadWebloggerProperties()
    }
  }
})
