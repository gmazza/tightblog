import { defineStore } from 'pinia'
import { findById, upsert } from '@/helpers'
import api from '@/api'
import type { Weblog, SessionInfo, UserWeblogRole } from '@/types'

type State = {
  userWeblogRoles: UserWeblogRole[]
  sessionInfo: SessionInfo
  userWeblogs: Weblog[]
}

export const useSessionInfoStore = defineStore('sessionInfo', {
  state: (): State => ({
    userWeblogRoles: [],
    sessionInfo: {},
    userWeblogs: []
  }),
  getters: {
    getSessionInfo(): SessionInfo {
      return this.sessionInfo
    },
    getUserWeblogRoles(): UserWeblogRole[] {
      return this.userWeblogRoles
    },
    getWeblog({ userWeblogs }): (id: String) => Weblog | null {
      return (id: String): Weblog | null => {
        const weblog = findById(userWeblogs, id)
        if (!weblog) return null
        return {
          ...weblog
        }
      }
    }
  },
  actions: {
    async loadSessionInfo() {
      await api
        .loadSessionInfo()
        .then((result) => (this.sessionInfo = result))
        .catch((error: String) => console.log('load session info error: ', error))
    },
    async loadUserWeblogRoles() {
      await api
        .loadUserWeblogRoles()
        .then((result) => {
          this.userWeblogRoles = result.data
        })
        .catch((error: String) => console.log('load user weblog roles error: ', error))
    },
    async fetchWeblog(weblogId: String) {
      const weblog = this.getWeblog(weblogId)
      if (weblog != null) {
        return weblog
      } else {
        return this.refreshWeblog(weblogId)
      }
    },
    async refreshWeblog(weblogId: String) {
      await api
        .fetchWeblog(weblogId)
        .then((result) => upsert(this.userWeblogs, result))
        .catch((error: String) => console.log('fetch weblog error: ', error))
    },
    async upsertWeblog(weblog: Weblog) {
      const updatedWeblog = await api
        .upsertWeblog(weblog)
        .catch((error: String) => console.log('upsert weblog error: ', error))
      upsert(this.userWeblogs, updatedWeblog)
    },
    async deleteWeblog(weblogId: String) {
      await api
        .deleteWeblog(weblogId)
        .catch((error: String) => console.log('delete weblog ' + weblogId + ' error: ', error))
      for (let i = 0; i < this.userWeblogs.length; i++) {
        if (this.userWeblogs[i].id == weblogId) {
          // https://stackoverflow.com/a/5767357/1207540
          this.userWeblogs.splice(i, 1)
        }
      }
      this.loadUserWeblogRoles()
    },
    async detachUserFromWeblog(weblogId: String) {
      await api
        .detachUserFromWeblog(weblogId)
        .catch((error: String) => console.log('detach user from ' + weblogId + ' error: ', error))
      this.loadUserWeblogRoles()
    },
    async setReceiveCommentsForWeblog(role: UserWeblogRole) {
      await api
        .setReceiveCommentsForWeblog(role)
        .catch((error: String) => console.log('set receive comments error: ', error))
      this.loadUserWeblogRoles()
    }
  }
})
