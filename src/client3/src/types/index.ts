export type CacheItem = {}

export type SessionInfo = {
  authenticatedUser?: User
  actionWeblog?: Weblog
  actionWeblogURL?: string
  userNeedsMFARegistration?: boolean
  userIsAdmin?: boolean
}

export type WebloggerProperties = {
  usersCustomizeThemes?: boolean
}

export type User = {
  screenName: string
}

export type UserWeblogRole = {
  id: String
  emailComments: boolean
  weblogRole: String
  weblog: Weblog
}

export type Weblog = {
  id: string
  name: string
  absoluteURL: string
  handle: string
}

export type LookupValues = {}

export type StartupConfig = {
  tightblogVersion?: string
  registrationPolicy?: string
}
