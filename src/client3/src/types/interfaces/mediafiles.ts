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
  imageFile: boolean
  selected: boolean
}
