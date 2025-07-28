import axios, { type AxiosResponse } from 'axios'
import type { TagUpdateResults, WeblogTagSummaryData } from '@/types'

const VITE_PUBLIC_PATH = import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest/tags'

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
