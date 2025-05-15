export interface User {
  id: string
  screenName: string
  userName: string
  emailAddress: string
  status: UserStatus
  globalRole: GlobalRole
  dateCreated: Date
  lastLogin: Date
}

export type UserStatus = 'DISABLED' | 'REGISTERED' | 'EMAILVERIFIED' | 'ENABLED'

export type GlobalRole = 'NOAUTHNEEDED' | 'MISSING_MFA_SECRET' | 'BLOGGER' | 'BLOGCREATOR' | 'ADMIN'

export interface UserCredentials {
  passwordText?: string
  passwordConfirm?: string
  hasMfaSecret?: boolean
  eraseMfaSecret?: boolean
}

export interface UserData {
  user: User
  credentials: UserCredentials
}
