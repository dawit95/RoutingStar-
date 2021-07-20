import Vue from 'vue'
import Vuex from 'vuex'

import accounts from './modules/accounts.js'
import routes from './modules/routes.js'

Vue.use(Vuex)

export default new Vuex.Store({
  // state: {
  // },
  // mutations: {
  // },
  // actions: {
  // },
  modules: {
    accounts,
    routes,
  }
})
