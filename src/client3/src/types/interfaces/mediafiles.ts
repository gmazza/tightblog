export interface MediaFolder {
  id: string
  name: string
}

export interface MediaFile {
  id: string
  name: string
  anchor: string
  titleAttribute: string
  altAttribute: string
  permalink: string
  contentType: string
  sizeInBytes: number
  width: number
  height: number
  notes: string
  directory?: MediaFolder
  thumbnailURL: string
  imageFile: boolean
  selected: boolean
}
