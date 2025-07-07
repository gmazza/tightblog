import type { ResolvedTypeReferenceDirectiveWithFailedLookupLocations } from 'typescript'

export interface WeblogTemplateData {
  themes: Array<SharedTheme>
  templates: Array<SharedTemplate>
  availableTemplateRoles: Array<TemplateRoleShort>
}

export interface SharedTheme {
  id: string
  name: string
  description: string
  previewImagePath: string
}

export interface TemplateRole {
  readableName: string
  contentType: string
  singleton: boolean
  incrementsHitCount: boolean
  blogComponent: boolean
  accessibleViaUrl: boolean
  descriptionProperty: string
}

export interface TemplateRoleShort {
  constant: string
  readableName: string
  descriptionProperty: string
}

export interface Template {
  id: string
  role: TemplateRole
  name: string
  description: string
  derivation: Derivation
  template: string
  selected: boolean
  lastModified: Date
}

export interface SharedTemplate extends Template {
  contentsFile: string
}

export type Derivation = 'SHARED' | 'OVERRIDDEN' | 'SPECIFICBLOG'
