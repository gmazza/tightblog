import { AxiosError } from 'axios'
import type { ErrorItem } from '@/types'

export function getApiErrorMessages(error: unknown): string[] | null {
    if (!(error instanceof AxiosError) || !Array.isArray(error.response?.data?.errors)) {
        return null
    }

    const errorItems = error.response.data.errors as ErrorItem[]

    return errorItems.flatMap((errorItem) => {
        if (errorItem.message == null) {
            return []
        }

        return [
            errorItem.fieldName
                ? `${errorItem.fieldName}: ${errorItem.message}`
                : errorItem.message
        ]
    })
}