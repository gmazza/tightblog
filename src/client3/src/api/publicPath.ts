// Determine the TightBlog API base path from the HTML <base> element.
// This includes the Tomcat context path, e.g. /tightblog/tb-ui.
const basePath = new URL(document.baseURI).pathname.replace(/\/$/, '')

export const VITE_PUBLIC_PATH = basePath.endsWith('/tb-ui')
    ? basePath
    : import.meta.env.VITE_PUBLIC_PATH
