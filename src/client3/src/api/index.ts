import axios from 'axios'

import type {
  CacheItem,
  LookupValues,
  SessionInfo,
  StartupConfig,
  User,
  UserWeblogRole,
  WebloggerProperties,
  Weblog
} from '@/types'

export default {
  // caches
  loadCaches(): Promise<CacheItem[]> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + 'caches')
  },
  clearCacheEntry(cacheItem: CacheItem) {
    return axios.post(
      import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/server/cache/' + cacheItem + '/clear'
    )
  },
  // startupConfig
  loadLookupValues(): Promise<LookupValues[]> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/app/authoring/lookupvalues')
  },
  loadStartupConfig(): Promise<StartupConfig> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/app/authoring/startupconfig')
  },
  // dynamicConfig
  loadWebloggerProperties(): Promise<WebloggerProperties> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/app/any/webloggerproperties')
  },
  loadWeblogList(): Promise<Weblog[]> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/server/webloglist')
  },
  loadUserList(): Promise<User[]> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/useradmin/userlist')
  },
  saveWebloggerProperties(webloggerProps: WebloggerProperties) {
    axios
      .post(
        import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/server/webloggerproperties',
        webloggerProps
      )
      .catch((error) => {
        console.error('Save weblogger properties error', error)
      })
  },
  // sessionInfo
  loadSessionInfo(): Promise<SessionInfo> {
    return axios.get<SessionInfo>(import.meta.env.VITE_PUBLIC_PATH + '/app/any/sessioninfo')
  },
  loadUserWeblogRoles(): Promise<UserWeblogRole[]> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/loggedinuser/weblogs')
  },
  fetchWeblog(weblogId: String): Promise<Weblog> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblog/' + weblogId)
  },
  upsertWeblog(weblog: Weblog) {
    const urlToUse = weblog.id
      ? import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblog/' + weblog.id
      : import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblogs'
    return axios.post(urlToUse, weblog)
  },
  deleteWeblog(weblogId: String) {
    return axios.delete(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblog/' + weblogId)
  },
  detachUserFromWeblog(weblogId: String) {
    return axios.post(
      import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblogrole/' + weblogId + '/detach'
    )
  },
  setReceiveCommentsForWeblog(role: UserWeblogRole) {
    return axios.post(
      import.meta.env.VITE_PUBLIC_PATH +
        '/authoring/rest/weblogrole/' +
        role.id +
        '/emails/' +
        role.emailComments
    )
  }
}
