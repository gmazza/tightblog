import { createApp } from 'vue'
import App from './App.vue'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'

import pinia from './stores'
import i18n from './i18n'
import router from './router'
import { initializeCsrfToken } from './api/csrf';

import AppAdminNav from './components/AppAdminNav.vue'
import AppHelpPopup from './components/AppHelpPopup.vue'
import AppTitleBar from './components/AppTitleBar.vue'
import AppUserNav from './components/AppUserNav.vue'
import AppSuccessMessageBox from './components/AppSuccessMessageBox.vue'
import AppErrorListMessageBox from './components/AppErrorListMessageBox.vue'
import AppErrorMessageBox from './components/AppErrorMessageBox.vue'

initializeCsrfToken().then(() => {
    const app = createApp(App);

    app.mixin({
      methods: {
        globalHelper: function () {
          alert('Hello world')
        },
        // lodash replacements: https://github.com/you-dont-need/You-Dont-Need-Lodash-Underscore
        globalSortBy: (key: string) => {
          return (a: any, b: any) => (a[key] > b[key] ? 1 : b[key] > a[key] ? -1 : 0)
        }
      }
    })

    app.use(pinia)
    app.use(router)
    app.use(i18n)

    app.component('AppAdminNav', AppAdminNav)
    app.component('AppUserNav', AppUserNav)
    app.component('AppTitleBar', AppTitleBar)
    app.component('AppHelpPopup', AppHelpPopup)
    app.component('AppSuccessMessageBox', AppSuccessMessageBox)
    app.component('AppErrorMessageBox', AppErrorMessageBox)
    app.component('AppErrorListMessageBox', AppErrorListMessageBox)

    app.mount('#app')
});
