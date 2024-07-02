import { createI18n } from 'vue-i18n'
import en from './locales/en.json'

// https://github.com/intlify/bundle-tools/tree/main/packages/unplugin-vue-i18n#static-bundle-importing
// https://ahmedshaltout.com/vuejs/vue-js-3-localization-tutorial-with-vue-i18n-example/
// https://vue-i18n.intlify.dev/guide/advanced/typescript#type-safe-resources-in-createi18n

type MessageSchema = typeof en

const i18n = createI18n<[MessageSchema], 'en-US'>({
  locale: 'en-US',
  warnHtmlInMessage: 'off',
  messages: {
    'en-US': en
  }
})

export default i18n
