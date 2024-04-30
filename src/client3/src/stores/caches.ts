import { defineStore } from 'pinia'
import api from '@/api'
import type { CacheItem } from '@/types'

type State = {
  items: CacheItem[]
  urlRoot: string
}

export const useCacheStore = defineStore('caches', {
  state: (): State => ({
    items: [],
    urlRoot: import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/server/'
  }),
  getters: {},
  actions: {
    async loadCaches() {
      await api
        .loadCaches()
        .then((result) => (this.items = result))
        .catch((error) => console.error('Load cache error', error))
    },
    async clearCacheEntry(cacheItem: CacheItem) {
      await api
        .clearCacheEntry(cacheItem)
        .catch((error) => console.error('Error clearing cache ' + cacheItem, error))
      this.loadCaches()
    }
  }
})
