<template>
  <view class="login-page">
    <view class="card">
      <view class="title">登录</view>

      <input
        class="input"
        v-model="form.phone"
        placeholder="请输入用户名"
        placeholder-class="placeholder"
      />

      <input
        class="input"
        v-model="form.password"
        type="password"
        placeholder="请输入密码"
        placeholder-class="placeholder"
      />

      <button
        class="btn"
        :loading="loading"
        @click="handleLogin"
      >
        登录
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { apiLogin } from '@/apis/user'

const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.phone || !form.password) {
    uni.showToast({
      title: '请输入用户名和密码',
      icon: 'none'
    })
    return
  }

  try {
    loading.value = true

    const res = await apiLogin(form)
    const { token, user } = res.data

    uni.setStorageSync('token', token)
    uni.setStorageSync('user', user)

    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })

    uni.reLaunch({
      url: '/pages/message-list/index'
    })
  } catch (e) {
  
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f6f8;
}

.card {
  width: 600rpx;
  padding: 60rpx 40rpx;
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.06);
}

.title {
  text-align: center;
  font-size: 36rpx;
  font-weight: 600;
  margin-bottom: 50rpx;
}

.input {
  height: 88rpx;
  border: 1rpx solid #ddd;
  border-radius: 12rpx;
  padding: 0 24rpx;
  margin-bottom: 30rpx;
  font-size: 28rpx;
}

.placeholder {
  color: #999;
}

.btn {
  height: 88rpx;
  background: #4a7afe;
  color: #fff;
  border-radius: 12rpx;
  font-size: 30rpx;
}
</style>
