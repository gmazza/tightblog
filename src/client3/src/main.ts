import { createApp } from 'vue'
import App from './App.vue'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'

import pinia from './stores'
import i18n from './i18n'
import router from './router'

import AppAdminNav from './components/AppAdminNav.vue'
import AppHelpPopup from './components/AppHelpPopup.vue'
import AppTitleBar from './components/AppTitleBar.vue'
import AppUserNav from './components/AppUserNav.vue'
import AppSuccessMessageBox from './components/AppSuccessMessageBox.vue'
import AppErrorListMessageBox from './components/AppErrorListMessageBox.vue'
import AppErrorMessageBox from './components/AppErrorMessageBox.vue'

const app = createApp(App)

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
