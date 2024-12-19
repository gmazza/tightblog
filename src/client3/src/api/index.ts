import axios, { type AxiosResponse } from 'axios'

// indicate requests via Ajax calls, so auth problems return 401s vs. login redirects
axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest'

import type {
  CacheItem,
  LookupValues,
  SessionInfo,
  StartupConfig,
  User,
  UserData,
  UserWeblogRole,
  WebloggerProperties,
  Weblog,
  WeblogEntriesData,
  WeblogEntry,
  WeblogEntryQueryParams,
  RecentWeblogEntry
} from '@/types/interfaces'

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
  loadLookupValues(): Promise<LookupValues> {
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
  loadUser(userId: string): Promise<User> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/userprofile/' + userId)
  },
  loadUserList(): Promise<User[]> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/useradmin/userlist')
  },
  saveUser(userId: string, user: UserData): Promise<User> {
    return axios.post(
      import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/userprofile/' + userId,
      user
    )
  },
  registerUser(user: UserData) {
    return axios.post(import.meta.env.VITE_PUBLIC_PATH + '/register/rest/registeruser', user)
  },
  async loadWeblogEntry(entryId: string): Promise<WeblogEntry> {
    const response: AxiosResponse<WeblogEntry> = await axios.get(
      import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblogentries/' + entryId
    )
    return response.data
  },
  loadRecentEntries(weblogId: string, entryType: string): Promise<RecentWeblogEntry[]> {
    return axios.get(
      import.meta.env.VITE_PUBLIC_PATH +
        '/authoring/rest/weblogentries/' +
        weblogId +
        '/recententries/' +
        entryType
    )
  },
  async loadEntries(
    weblogId: string,
    pageNum: number,
    queryParams: WeblogEntryQueryParams
  ): Promise<WeblogEntriesData> {
    const response: AxiosResponse<WeblogEntriesData> = await axios.post(
      import.meta.env.VITE_PUBLIC_PATH +
        '/authoring/rest/weblogentries/' +
        weblogId +
        '/page/' +
        pageNum,
      queryParams
    )
    return response.data
  },
  saveWeblogEntry(weblogId: string, entry: WeblogEntry): Promise<string> {
    return axios
      .post(
        import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblogentries/' + weblogId + '/entries',
        entry
      )
      .then((response) => response.data)
  },
  deleteWeblogEntry(entryId: string) {
    return axios.delete(
      import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblogentries/' + entryId
    )
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
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/app/any/sessioninfo')
  },
  loadUserWeblogRoles(): Promise<UserWeblogRole[]> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/loggedinuser/weblogs')
  },
  fetchWeblog(weblogId: string): Promise<Weblog> {
    return axios.get(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblog/' + weblogId)
  },
  upsertWeblog(weblog: Weblog) {
    const urlToUse = weblog.id
      ? import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblog/' + weblog.id
      : import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblogs'
    return axios.post(urlToUse, weblog)
  },
  deleteWeblog(weblogId: string) {
    return axios.delete(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblog/' + weblogId)
  },
  detachUserFromWeblog(weblogId: string) {
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
