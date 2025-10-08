// csrf.ts
import axios from 'axios';

interface CsrfToken {
    token: string;
    headerName: string;
    parameterName: string;
}

const VITE_PUBLIC_PATH = import.meta.env.VITE_PUBLIC_PATH

/**
 * Initializes the CSRF token for Axios by calling the Spring Boot CSRF endpoint
 * and setting the token in Axios default headers for all future requests.
 */
export async function initializeCsrfToken(): Promise<void> {
    try {
        const response = await axios.get<CsrfToken>(VITE_PUBLIC_PATH + '/app/csrf', {
            withCredentials: true,
        });

        const csrfToken = response.data.token;
        const csrfHeader = response.data.headerName;

        if (csrfToken && csrfHeader) {
            axios.defaults.headers.common[csrfHeader] = csrfToken;
            console.info(`CSRF token initialized: ${csrfHeader}=${csrfToken}`);
        } else {
            console.warn('CSRF token or header name is missing from the response');
        }
    } catch (error) {
        console.error('Failed to initialize CSRF token:', error);
    }
}
