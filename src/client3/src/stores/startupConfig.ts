import { defineStore } from 'pinia'
import api from '@/api'
import type { LookupValues, StartupConfig } from '@/types'

type State = {
  lookupValues?: LookupValues
  startupConfig?: StartupConfig
}

export const useStartupConfigStore = defineStore('startupConfig', {
  state: (): State => ({
    lookupValues: undefined,
    startupConfig: undefined
  }),
  actions: {
    async loadLookupValues() {
      if (!this.lookupValues) {
        await api
          .loadLookupValues()
          .then((result) => {
            this.lookupValues = result.data
          })
          .catch((error) => console.log('load lookup vals error: ', error))
      }
    },
    async loadStartupConfig() {
      if (!this.startupConfig) {
        await api
          .loadStartupConfig()
          .then((result) => {
            this.startupConfig = result.data
          })
          .catch((error) => console.log('load startup config error: ', error))
      }
    }
  }
})
