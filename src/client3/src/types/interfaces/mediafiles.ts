export interface MediaFolder {
  id: string
  name: string
}

export interface MediaFile {
  id: string
  name: string
  anchor: string
  titleText: string
  altText: string
  permalink: string
  contentType: string
  length: number
  width: number
  height: number
  notes: string
  directory?: MediaFolder
  thumbnailURL: string
  imageFile: boolean
  selected: boolean
}
