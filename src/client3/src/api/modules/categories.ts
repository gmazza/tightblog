import axios, { type AxiosResponse } from 'axios'
import type { WeblogCategory } from '@/types'

const VITE_PUBLIC_PATH = import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest'

export function loadCategories(weblogId: string): Promise<AxiosResponse<Array<WeblogCategory>>> {
  return axios.get(`${VITE_PUBLIC_PATH}/categories?weblogId=${weblogId}`)
}

export function updateCategory(
  weblogId: string,
  category: WeblogCategory
): Promise<AxiosResponse<WeblogCategory>> {
  return axios.put(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/category`, category)
}

export function insertCategory(
  weblogId: string,
  category: WeblogCategory
): Promise<AxiosResponse<WeblogCategory>> {
  return axios.put(`${VITE_PUBLIC_PATH}/categories?weblogId=${weblogId}`, category)
}

export function deleteCategory(
  weblogId: string,
  categoryToDeleteId: string,
  targetCategoryId: string
): Promise<AxiosResponse<void>> {
  return axios.delete(
    `${VITE_PUBLIC_PATH}/weblog/${weblogId}/category/${categoryToDeleteId}?targetCategoryId=${targetCategoryId}`
  )
}
