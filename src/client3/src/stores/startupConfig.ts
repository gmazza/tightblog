import { defineStore } from 'pinia'
import api from '@/api'
import type { LookupValues, StartupConfig } from '@/types'

type State = {
  lookupValues: LookupValues[]
  startupConfig: StartupConfig
}

export const useStartupConfigStore = defineStore('startupConfig', {
  state: (): State => ({
    lookupValues: [],
    startupConfig: {}
  }),
  actions: {
    async loadLookupValues() {
      if (!this.lookupValues || this.lookupValues.length === 0) {
        await api
          .loadLookupValues()
          .then((result) => (this.lookupValues = result))
          .catch((error) => console.log('load lookup vals error: ', error))
      }
    },
    async loadStartupConfig() {
      if (!this.startupConfig) {
        await api
          .loadStartupConfig()
          .then((result) => (this.startupConfig = result))
          .catch((error) => console.log('load startup config error: ', error))
      }
    }
  }
})
