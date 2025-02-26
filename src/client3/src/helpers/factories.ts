import type { WeblogEntry } from '@/types'

export function createDefaultWeblogEntry(): WeblogEntry {
  return {
    id: '',
    title: '',
    text: '',
    summary: '',
    notes: '',
    anchor: '',
    commentDays: 0, // Replace with appropriate default value
    category: { id: '', name: '' },
    tags: [],
    tagsAsString: '',
    hours: 0,
    minutes: 0,
    dateString: '',
    permalink: '',
    previewUrl: '',
    status: 'DRAFT',
    searchDescription: '',
    enclosureLength: 0,
    enclosureType: '',
    enclosureUrl: '',
    commentCountIncludingUnapproved: 0,
    editFormat: 'HTML', // Replace with appropriate default value
    updateTime: new Date()
  }
}
