<template>
  <view class="login-container">
    <button class="wx-login-btn" @click="wxLogin">
      微信一键登录
    </button>
  </view>
</template>

<script setup lang="ts">
import { apiWxLogin } from '@/apis/user'

const wxLogin = () => {
  wx.login({
    success: async (res) => {
      if (!res.code) {
        uni.showToast({ title: '获取微信 code 失败', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '登录中...' })

        const result = await apiWxLogin(res.code)

        const { user, token } = result.data

        uni.setStorageSync('user', user)
        uni.setStorageSync('token', token)

        uni.reLaunch({
          url: '/pages/message-list/index'
        })
      } catch (err) {
        uni.showToast({ title: '登录失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.wx-login-btn {
  width: 80%;
  height: 88rpx;
  background-color: #07c160;
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
}
</style>
