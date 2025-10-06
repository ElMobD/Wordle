import { createI18n } from 'vue-i18n'

const messages = {
  en: { hello: 'Hello!'},
  fr: { hello: 'Bonjourxwcxwcxw !'}
}

export const i18n = createI18n({
  locale: 'fr', // langue par défaut
  fallbackLocale: 'en',
  messages,
})
