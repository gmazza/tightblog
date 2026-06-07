import { defineStore } from 'pinia'
import { findById, upsert } from '@/helpers'
import api from '@/api'
import type { Weblog, SessionInfo, UserHasWeblog } from '@/types'

type State = {
  userWeblogs: UserHasWeblog[]
  sessionInfo: SessionInfo
  cachedWeblogs: Weblog[]
}

export const useSessionInfoStore = defineStore('sessionInfo', {
  state: (): State => ({
    userWeblogs: [],
    sessionInfo: {},
    cachedWeblogs: []
  }),
  getters: {
    getSessionInfo(): SessionInfo {
      return this.sessionInfo
    },
    getUserWeblogs(): UserHasWeblog[] {
      return this.userWeblogs
    },
    getWeblog(): (id: String) => Weblog | null {
      return (id: String): Weblog | null => {
        const weblog = findById(this.cachedWeblogs, id)
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
    async loadUserWeblogs() {
      await api
        .loadUserWeblogs()
        .then((result) => {
          this.userWeblogs = result
        })
        .catch((error: String) => console.log('load user weblog roles error: ', error))
    },
    async fetchWeblog(weblogId: string): Promise<Weblog | null> {
      const weblog = this.getWeblog(weblogId)
      if (weblog != null) {
        return weblog
      } else {
        await this.refreshWeblog(weblogId)
        return this.getWeblog(weblogId)
      }
    },
    async refreshWeblog(weblogId: string): Promise<Weblog | null> {
      await api
        .fetchWeblog(weblogId)
        .then((result) => {
          upsert(this.cachedWeblogs, result)
        })
        .catch((error: String) => console.log('fetch weblog error: ', error))
      return this.getWeblog(weblogId)
    },
    async upsertWeblog(weblog: Weblog) {
      const updatedWeblog = await api
        .upsertWeblog(weblog)
        .catch((error: String) => console.log('upsert weblog error: ', error))
      upsert(this.cachedWeblogs, updatedWeblog)
      this.loadUserWeblogs()
    },
    async deleteWeblog(weblogId: string) {
      await api
        .deleteWeblog(weblogId)
        .catch((error: String) => console.log('delete weblog ' + weblogId + ' error: ', error))
      for (let i = 0; i < this.cachedWeblogs.length; i++) {
        if (this.cachedWeblogs[i].id == weblogId) {
          // https://stackoverflow.com/a/5767357/1207540
          this.cachedWeblogs.splice(i, 1)
        }
      }
      this.loadUserWeblogs()
    },
    async detachUserFromWeblog(userId: string, weblogId: string) {
      await api
        .detachUserFromWeblog(userId, weblogId)
        .catch((error: String) => console.log('detach user from ' + weblogId + ' error: ', error))
      this.loadUserWeblogs()
    },
    async setReceiveCommentsForWeblog(role: UserHasWeblog) {
      await api
        .setReceiveCommentsForWeblog(role)
        .catch((error: String) => console.log('set receive comments error: ', error))
      this.loadUserWeblogs()
    }
  }
})
