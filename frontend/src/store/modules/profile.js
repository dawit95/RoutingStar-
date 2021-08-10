// profile.js 
import { getUserInfoByUserId } from '@/api/accounts.js'
import { follow, getFollowList } from '@/api/follow.js'
import { getWrittenRoute, getSavedRoute } from '@/api/routes.js'
// import accounts from './accounts'
// import axios from 'axios'

const state = {
  userInfo: [],
  writtenRouteList: [],
  savedRouteList: [],
  followUserList: [],
}

const getters = {
  userInfo(state) {
    return state.userInfo
  },
  writtenRouteList(state) {
    return state.writtenRouteList
  },
  savedRouteList(state) {
    return state.savedRouteList
  },
  followUserList(state) {
    return state.followUserList
  },
}

const mutations = {
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
  },
  SET_WRITTEN_ROUTE_LIST(state, writtenRouteList) {
    state.writtenRouteList = writtenRouteList
  },
  SET_SAVED_ROUTE_LIST(state, savedRouteList) {
    state.savedRouteList = savedRouteList
  },
  SET_FOLLOW_USER_LIST(state, followUserList) {
    state.followUserList = followUserList
  },
}

const actions = {
  // async fetchUserInfo({ commit }, userId, access_token) {
  fetchUserInfo({ commit }, payload ) {
    // console.log('여기는 store')
    // console.log(payload)
    // // console.log(payload.userId)
    // console.log(payload.access_token)
    getUserInfoByUserId(payload.userId, payload.access_token,
    (res) => {
      console.log('유저정보 가져오기 성공!')
      console.log(res.data.success)
      commit('SET_USER_INFO', res.data.success)
    }, (error) => {
      console.log(error)
    });
  },
  
  followOtherUser({ dispatch }, payload ) {
    console.log('여기 페이로드')
    console.log(payload)
    follow(payload.userId, payload.targetId, payload.access_token,
    (res) => {
      console.log(res.data.success)
      dispatch('fetchFollowUserList', payload)
      dispatch('fetchUserInfo', payload)
    }, (error) => {
      console.log(error)
    });
  },

  fetchFollowUserList({ commit }, payload ) {
    console.log('그럼 여기는 리스트 불러오는 =')
    console.log(payload)
    getFollowList(payload.targetId, payload.access_token,
    (res) => {
      console.log(res.data.success)
      commit('SET_FOLLOW_USER_LIST', res.data.success)
    }, (error) => {
      console.log(error)
    });
  },

  fetchWrittenRouteList({ commit }, payload ) {
    getWrittenRoute(payload.userId, payload.access_token,
    (res) => {
      console.log(res.data.success)
      commit('SET_WRITTEN_ROUTE_LIST', res.data.success)
    }, (error) => {
      console.log(error)
    });
  },

  fetchSavedRouteList({ commit }, payload ) {
    getSavedRoute(payload.userId, payload.access_token,
    (res) => {
      console.log(res.data.success)
      commit('SET_SAVED_ROUTE_LIST', res.data.success)
    }, (error) => {
      console.log(error)
    });
  },



  // followOtherUser({ getters, dispatch }, userI+d) {
  //   axios.post(DRF.URL + DRF.ROUTES.follow(userId), null, getters.config)
  //     .then(() => dispatch('fetchOtherUserInfo', userId))
  //     // 팔로우, 팔로잉 리스트도 받아야와야함
  //     .catch(err => console.error(err))
  // },

}

export default {
  state, getters, mutations, actions
}