import Cookies from 'js-cookie'

export function setTokenCookie(token: string) {
  Cookies.set('token', token, { path: '/', sameSite: 'lax' })
}

export function removeTokenCookie() {
  Cookies.remove('token', { path: '/' })
}
