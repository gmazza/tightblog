import axios, { type AxiosResponse } from 'axios'
import type { TagUpdateResults, WeblogTagSummaryData } from '@/types'
import { VITE_PUBLIC_PATH as APP_BASE_PATH } from '@/api/publicPath'

const VITE_PUBLIC_PATH = APP_BASE_PATH + '/authoring/rest/tags'

export function loadTags(
  weblogId: string,
  pageNum: number
): Promise<AxiosResponse<WeblogTagSummaryData>> {
  return axios.get(`${VITE_PUBLIC_PATH}/${weblogId}/page/${pageNum}`)
}

export function deleteTags(
  weblogId: string,
  tagsToDelete: Array<string>
): Promise<AxiosResponse<void>> {
  return axios.post(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/delete`, tagsToDelete)
}

export function appendTag(
  weblogId: string,
  currentTagName: string,
  newTagName: string
): Promise<AxiosResponse<TagUpdateResults>> {
  return axios.post(
    `${VITE_PUBLIC_PATH}/weblog/${weblogId}/add/currenttag/${currentTagName}/newtag/${newTagName}`
  )
}

export function replaceTag(
  weblogId: string,
  currentTagName: string,
  newTagName: string
): Promise<AxiosResponse<TagUpdateResults>> {
  return axios.post(
    `${VITE_PUBLIC_PATH}/weblog/${weblogId}/replace/currenttag/${currentTagName}/newtag/${newTagName}`
  )
}
