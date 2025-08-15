import axios, { type AxiosResponse } from 'axios'

export * from './modules/blogroll'
export * from './modules/categories'
export * from './modules/mediafiles'
export * from './modules/tags'
export * from './modules/templates'
export * from './modules/users'

// indicate requests via Ajax calls, so auth problems return 401s vs. login redirects
axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest'

import type {
  CacheItem,
  Comment,
  CommentsData,
  EntryCommentsQueryParams,
  LookupValues,
  SessionInfo,
  StartupConfig,
  UserWeblogRole,
  WebloggerProperties,
  Weblog,
  WeblogEntriesData,
  WeblogEntry,
  WeblogEntryQueryParams,
  RecentWeblogEntry
} from '@/types'

export default {
  // caches
  loadCaches(): Promise<CacheItem[]> {
    return axios
      .get(import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/server/caches')
      .then((response) => response.data)
  },
  clearCacheEntry(cacheItem: CacheItem) {
    return axios.post(
      import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/server/cache/' + cacheItem + '/clear'
    )
  },
  resetHitCount(): Promise<void> {
    return axios.post(import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/server/resethitcount')
  },
  // startupConfig
  loadLookupValues(): Promise<LookupValues> {
    return axios
      .get(import.meta.env.VITE_PUBLIC_PATH + '/app/authoring/lookupvalues')
      .then((response) => response.data)
  },
  loadStartupConfig(): Promise<StartupConfig> {
    return axios
      .get(import.meta.env.VITE_PUBLIC_PATH + '/app/authoring/startupconfig')
      .then((response) => response.data)
  },
  // dynamicConfig
  loadWebloggerProperties(): Promise<WebloggerProperties> {
    return axios
      .get(import.meta.env.VITE_PUBLIC_PATH + '/app/any/webloggerproperties')
      .then((response) => response.data)
  },
  loadWeblogList(): Promise<Weblog[]> {
    return axios
      .get(import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/server/webloglist')
      .then((response) => response.data)
  },
  async loadWeblogEntry(entryId: string): Promise<WeblogEntry> {
    const response: AxiosResponse<WeblogEntry> = await axios.get(
      import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblogentries/' + entryId
    )
    return response.data
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
  loadRecentEntries(weblogId: string, entryType: string): Promise<RecentWeblogEntry[]> {
    return axios.get(
      import.meta.env.VITE_PUBLIC_PATH +
        '/authoring/rest/weblogentries/' +
        weblogId +
        '/recententries/' +
        entryType
    )
  },
  async loadComments(
    weblogId: string,
    pageNum: number,
    queryParams: EntryCommentsQueryParams,
    entryId?: string
  ): Promise<CommentsData> {
    let url =
      import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/comments/' + weblogId + '/page/' + pageNum
    if (entryId) {
      url += '?entryId=' + entryId
    }
    const response: AxiosResponse<CommentsData> = await axios.post(url, queryParams)
    return response.data
  },
  reindexWeblog(weblogHandle: string): Promise<void> {
    return axios.post(
      import.meta.env.VITE_PUBLIC_PATH +
        '/admin/rest/server/weblog/' +
        weblogHandle +
        '/rebuildindex'
    )
  },
  saveComment(comment: Comment): Promise<Comment> {
    return axios
      .put(
        import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/comments/' + comment.id + '/content',
        comment.content,
        {
          headers: { 'Content-Type': 'text/plain' }
        }
      )
      .then((response) => response.data)
  },
  approveComment(commentId: string) {
    return axios.post(
      import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/comments/' + commentId + '/approve'
    )
  },
  hideComment(commentId: string) {
    return axios.post(
      import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/comments/' + commentId + '/hide'
    )
  },
  deleteComment(commentId: string) {
    return axios.delete(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/comments/' + commentId)
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
    return axios
      .get(import.meta.env.VITE_PUBLIC_PATH + '/app/any/sessioninfo')
      .then((response) => response.data)
  },
  async loadUserWeblogRoles(): Promise<UserWeblogRole[]> {
    return axios
      .get<
        UserWeblogRole[]
      >(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/loggedinuser/weblogs')
      .then((response) => response.data)
  },
  fetchWeblog(weblogId: string): Promise<Weblog> {
    return axios
      .get<Weblog>(import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/weblog/' + weblogId)
      .then((response) => response.data)
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
  },
  loadQRCode(): Promise<string> {
    return axios
      .get(import.meta.env.VITE_PUBLIC_PATH + '/newuser/rest/newqrcode')
      .then((response) => response.data.qrCode)
  }
}
