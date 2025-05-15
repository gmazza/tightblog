import axios, { type AxiosResponse } from 'axios'
import type { User, UserData, UserWeblogRole, WeblogRole } from '@/types'

const VITE_PUBLIC_PATH = import.meta.env.VITE_PUBLIC_PATH + '/admin/rest/useradmin'

export function processRegistration(userId: string, command: string): Promise<AxiosResponse<void>> {
  return axios.post(`${VITE_PUBLIC_PATH}/registrationapproval/${userId}/${command}`)
}

export function getPendingRegistrations(): Promise<AxiosResponse<Array<User>>> {
  return axios.get(`${VITE_PUBLIC_PATH}/registrationapproval`)
}

export function getUserData(userId: string): Promise<AxiosResponse<UserData>> {
  return axios.get(`${VITE_PUBLIC_PATH}/user/${userId}`)
}

export function loadUserWeblogs(userId: string): Promise<AxiosResponse<Array<UserWeblogRole>>> {
  return axios.get(`${VITE_PUBLIC_PATH}/user/${userId}/weblogs`)
}

export function updateUser(userId: string, user: UserData): Promise<AxiosResponse<UserData>> {
  return axios.put(`${VITE_PUBLIC_PATH}/user/${userId}`, user)
}

export function setUserWeblogAffiliation(
  userId: string,
  weblogId: string,
  role: string
): Promise<AxiosResponse<string>> {
  return axios.post(
    `${VITE_PUBLIC_PATH}/authoring/rest/weblog/${weblogId}/user/${userId}/weblogRole/${role}/associate`
  )
}
