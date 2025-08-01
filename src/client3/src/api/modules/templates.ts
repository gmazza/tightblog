import axios, { type AxiosResponse } from 'axios'

const VITE_PUBLIC_PATH = import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest'

import type { Template, WeblogTemplateData } from '@/types'

export function loadTemplateData(weblogId: string): Promise<WeblogTemplateData> {
  return axios
    .get<WeblogTemplateData>(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/templates`)
    .then((response) => response.data)
}

export function addTemplate(
  weblogId: string,
  templateName: string,
  templateRole: string
): Promise<AxiosResponse<void>> {
  const newData = {
    name: templateName,
    roleName: templateRole,
    template: ''
  }

  return axios.post<void>(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/templates`, newData)
}

export function loadTemplateByName(weblogId: string, templateName: string): Promise<Template> {
  return axios
    .get<Template>(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/templatename/${templateName}/`)
    .then((response) => response.data)
}

export function loadTemplateById(templateId: string): Promise<Template> {
  return axios
    .get<Template>(`${VITE_PUBLIC_PATH}/template/${templateId}`)
    .then((response) => response.data)
}

export function saveTemplate(
  weblogId: string,
  updatedTemplate: Template
): Promise<AxiosResponse<string>> {
  return axios.post<string>(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/templates`, updatedTemplate)
}

export function switchTheme(weblogId: string, themeId: string): Promise<AxiosResponse<void>> {
  return axios.post<void>(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/switchtheme/${themeId}`)
}

export function deleteTemplates(
  weblogId: string,
  templateIds: Array<string>
): Promise<AxiosResponse<void>> {
  return axios.post<void>(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/templates/delete`, templateIds)
}
