export interface CacheItem {}

export interface Credentials {}

export interface ErrorObj {
  errors: Array<string>
}

export interface LookupValues {}

export interface RecentWeblogEntry {
  id: string
  title: string
  entryEditURL: string
}

export interface SessionInfo {
  authenticatedUser?: User
  actionWeblog?: Weblog
  actionWeblogURL?: string
  userNeedsMFARegistration?: boolean
  userIsAdmin?: boolean
}

export interface StartupConfig {
  tightblogVersion?: string
  registrationPolicy?: string
}

export interface User {
  screenName: string
}

export interface UserData {
  user: User
  credentials: Credentials
}

export interface UserWeblogRole {
  id: String
  emailComments: boolean
  weblogRole: String
  weblog: Weblog
}

export interface Weblog {
  id: string
  name: string
  tagline: string
  about: string
  absoluteURL: string
  handle: string
  theme: string
  locale: string
  timeZone: string
  visible: boolean
  entriesPerPage: number
  defaultCommentDays: number
  spamPolicy: 'DONT_CHECK' | 'MARK_SPAM' | 'NO_EMAIL' | 'JUST_DELETE'
  editFormat: 'HTML' | 'COMMONMARK'
  allowComments: 'NONE' | 'MODERATE_NONPUB' | 'MODERATE_NONAUTH'
  applyCommentDefaults: boolean
  weblogCategoryNames: Array<string>
  analyticsCode: string
  blacklist: string
}

export interface WeblogCategory {
  id: string
  name: string
}

export type PublishStatus = 'PUBLISHED' | 'DRAFT' | 'SCHEDULED'

export interface WeblogEntry {
  id: string
  title: string
  text: string
  summary: string
  notes: string
  anchor: string
  commentDays: number
  category: WeblogCategory
  tags: Array<string>
  tagsAsString: string
  hours: number
  minutes: number
  dateString: string
  permalink: string
  previewUrl: string
  status: PublishStatus
  searchDescription: string
  enclosureUrl: string
  enclosureType: string
  enclosureLength: number
  commentCountIncludingUnapproved: number
  editFormat: string
  updateTime: Date
}

export interface WebloggerProperties {
  usersCustomizeThemes?: boolean
}
