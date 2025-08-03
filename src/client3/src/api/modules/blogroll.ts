import axios, { type AxiosResponse } from 'axios'
import type { WeblogBookmark } from '@/types'

const VITE_PUBLIC_PATH = import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest'

export function loadBookmarks(weblogId: string): Promise<AxiosResponse<Array<WeblogBookmark>>> {
  return axios.get(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/bookmarks`)
}

export function deleteBookmarks(bookmarksToDelete: Array<string>): Promise<AxiosResponse<void>> {
  return axios.post(`${VITE_PUBLIC_PATH}/bookmarks/delete`, bookmarksToDelete)
}

export function updateBookmark(bookmark: WeblogBookmark): Promise<AxiosResponse<void>> {
  return axios.put(`${VITE_PUBLIC_PATH}/bookmark/${bookmark.id}`, bookmark)
}

export function insertBookmark(
  weblogId: string,
  bookmark: WeblogBookmark
): Promise<AxiosResponse<void>> {
  return axios.put(`${VITE_PUBLIC_PATH}/bookmarks?weblogId=${weblogId}`, bookmark)
}
