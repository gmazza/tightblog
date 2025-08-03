export * from './interfaces/mediafiles'
export * from './interfaces/templates'
export * from './interfaces/users'
import type { User, GlobalRole, UserStatus } from './interfaces/users'
import type { SharedTheme } from './interfaces/templates'

export interface CacheItem {}

export interface Comment {
  id: string
  postTime: Date
  status: CommentStatus
  weblogEntry: WeblogEntry
  name: string
  email: string
  url: string
  content: string
  originalContent: string
  editable: boolean
  remoteHost: string
}

export interface EntryCommentsQueryParams {
  categoryName?: string
  searchText?: string
  status?: CommentStatus | ''
  startDate?: Date
  endDate?: Date
}

export type CommentStatus = 'PENDING' | 'APPROVED' | 'DISAPPROVED' | 'SPAM'

export interface CommentsData {
  entryTitle: string
  comments: Array<Comment>
  hasMore: boolean
}

export interface ErrorObj {
  errors: Array<string>
}

// AxiosError: response.data.errors contains array of these objects
export interface ErrorItem {
  fieldName: string
  message: string | null
}

export interface LookupValues {
  dateSortByOptions: Array<LookupDateSortBy>
  entryStatusOptions: Array<PublishStatus>
  commentApprovalStatuses: Array<CommentStatus>
  commentDayOptions: Array<number>
  editFormats: Array<EditFormat>
  locales: { [key: string]: string }
  timezones: { [key: string]: string }
  sharedThemeMap: { [key: string]: SharedTheme }
  spamOptionList: Array<SpamPolicy>
  commentOptionList: Array<CommentPolicy>
  globalRoles: Array<GlobalRole>
  userStatuses: Array<UserStatus>
}

export type LookupDateSortBy = 'PUBLICATION_TIME' | 'UPDATE_TIME'

export interface RecentWeblogEntry {
  id: string
  title: string
  entryEditURL: string
}

export interface SessionInfo {
  authenticatedUser?: User
  actionWeblog?: Weblog
  actionWeblogURL?: string
  absoluteUrl?: string
  userNeedsMFARegistration?: boolean
  userIsAdmin?: boolean
  userCanCreateBlogs?: boolean
}

export interface StartupConfig {
  tightblogVersion: string
  registrationPolicy: string
  absoluteSiteURL: string
  mfaEnabled: boolean
}

export interface UserWeblogRole {
  id: String
  emailComments: boolean
  weblogRole: WeblogRole
  weblog: Weblog
}

export type WeblogRole = 'OWNER' | 'POST'

export interface Weblog {
  id?: string
  name: string
  tagline: string
  about: string
  absoluteURL?: string
  handle: string
  theme: string
  locale: string
  timeZone: string
  visible: boolean
  entriesPerPage: number
  defaultCommentDays: number
  spamPolicy: SpamPolicyLabel
  editFormat: EditFormat
  allowComments: CommentPolicyLabel
  applyCommentDefaults: boolean
  weblogCategoryNames: Array<string>
  hitsToday: number
  unapprovedComments: number
  analyticsCode: string
  blacklist: string
}

export interface UserWeblogRole {
  user: User
  weblog: Weblog
  weblogRole: WeblogRole
}

export interface CommentPolicy {
  level: number
  label: CommentPolicyLabel
}

export interface SpamPolicy {
  level: number
  label: SpamPolicyLabel
}

export type SpamPolicyLabel = 'DONT_CHECK' | 'MARK_SPAM' | 'NO_EMAIL' | 'JUST_DELETE'

export type CommentPolicyLabel = 'NONE' | 'MODERATE_NONPUB' | 'MODERATE_NONAUTH'

export type EditFormat = 'HTML' | 'COMMONMARK'

export interface WeblogCategory {
  id?: string
  name: string
  position?: number
  numEntries?: number
  firstEntry?: Date | null
  lastEntry?: Date | null
}

export interface WeblogEntryTag {
  id?: string
  name: string
  total: number
  firstEntry: Date | null
  lastEntry: Date | null
  viewUrl?: string
  selected?: boolean
}

export interface WeblogBookmark {
  id?: string
  name: string
  description: string
  url: string
  position?: number
  selected?: boolean
}

export interface TagUpdateResults {
  updated: number
  unchanged: number
}

export interface WeblogTagSummaryData {
  tags: Array<WeblogEntryTag>
  hasMore: boolean
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
  permalink: string
  previewUrl: string
  status: PublishStatus
  searchDescription: string
  enclosureUrl: string
  enclosureType: string
  enclosureLength: number
  commentCountIncludingUnapproved: number
  editFormat: string
  pubTime: Date
  updateTime: Date
}

export interface WeblogEntriesData {
  entries: Array<WeblogEntry>
  hasMore: boolean
}

export interface WeblogEntryQueryParams {
  categoryName?: string
  sortBy?: 'PUBLICATION_TIME' | 'UPDATE_TIME'
  status?: PublishStatus
  startDate?: Date
  endDate?: Date
}

export interface WebloggerProperties {
  usersCustomizeThemes?: boolean
  commentPolicy?: CommentPolicyLabel
  commentPolicyLevel?: number
  spamPolicyLevel?: number
  usersOverrideAnalyticsCode?: boolean
}
