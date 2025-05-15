import axios, { type AxiosResponse } from 'axios'
import type { MediaFile, MediaFolder } from '@/types'

const VITE_PUBLIC_PATH = import.meta.env.VITE_PUBLIC_PATH + '/authoring/rest'

export function addFolder(
  weblogId: string,
  folderName: string
): Promise<AxiosResponse<MediaFolder>> {
  return axios.put(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/mediadirectories`, {
    name: folderName
  })
}

export function loadMediaFolders(weblogId: string): Promise<AxiosResponse<Array<MediaFolder>>> {
  return axios.get(`${VITE_PUBLIC_PATH}/weblog/${weblogId}/mediadirectories`)
}

export function loadMediaFile(fileId: string): Promise<AxiosResponse<MediaFile>> {
  return axios.get(`${VITE_PUBLIC_PATH}/mediafile/${fileId}`)
}

export function saveMediaFile(formData: FormData): Promise<AxiosResponse<MediaFile>> {
  return axios.post(`${VITE_PUBLIC_PATH}/mediafiles`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function loadMediaFiles(folderId: string): Promise<AxiosResponse<Array<MediaFile>>> {
  return axios.get(`${VITE_PUBLIC_PATH}/mediadirectories/${folderId}/files`)
}

export function deleteFolder(folderId: string): Promise<AxiosResponse<void>> {
  return axios.delete(`${VITE_PUBLIC_PATH}/mediadirectory/${folderId}`)
}

export function deleteMediaFiles(
  weblogId: string,
  fileIds: Array<string>
): Promise<AxiosResponse<void>> {
  return axios.post(`${VITE_PUBLIC_PATH}/mediafiles/weblog/${weblogId}/deletefiles`, {
    data: { fileIds }
  })
}

export function moveFiles(
  weblogId: string,
  folderId: string,
  fileIds: Array<string>
): Promise<AxiosResponse<void>> {
  return axios.post(`${VITE_PUBLIC_PATH}/mediafiles/weblog/${weblogId}/movefiles`, {
    fileIdsToMove: fileIds,
    destinationFolderId: folderId
  })
}
