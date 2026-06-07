export interface User {
  id: string
  screenName: string
  userName: string
  emailAddress: string
  userStatus: UserStatus
  globalRole: GlobalRole
  dateCreated: Date
  lastLogin: Date
  passwordText?: string
  passwordConfirm?: string
  hasMfaSecret?: boolean
  eraseMfaSecret?: boolean
}

export type UserStatus = 'DISABLED' | 'REGISTERED' | 'EMAILVERIFIED' | 'ENABLED'

export type GlobalRole = 'NOAUTHNEEDED' | 'MISSING_MFA_SECRET' | 'BLOGGER' | 'BLOGCREATOR' | 'ADMIN'
