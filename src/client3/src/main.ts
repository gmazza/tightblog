import { createApp } from 'vue'
import App from './App.vue'

import pinia from './stores'
import i18n from './i18n'
import router from './router'

import AppTitleBar from './components/AppTitleBar.vue'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(i18n)

app.component('AppTitleBar', AppTitleBar)

app.mount('#app')
