<template>
  <view class="profile">
    <view class="avatar-card">
      <image
        class="avatar"
        :src="avatar"
        @click="chooseAvatar"
      />
      <text class="tip">点击头像更换</text>
    </view>

    <view class="info-card">
      <view class="info-item">
        <text class="label">名字</text>
        <input
          class="input"
          v-model="user.name"
          placeholder="请输入名字"
        />
      </view>

      <view class="info-item">
        <text class="label">性别</text>
        <picker
          mode="selector"
          :range="['男','女']"
          @change="onGenderChange"
        >
          <view class="input picker-text">
            {{ user.gender ? '男' : '女' }}
          </view>
        </picker>
      </view>

      <view class="info-item">
        <text class="label">手机</text>
        <input
          class="input"
          v-model="user.phone"
          placeholder="请输入手机号码"
        />
      </view>

    
    </view>

    <button class="save-btn" @click="saveProfile">
      保存信息
    </button>
	<button class="save-btn" @click="logout">
	  退出登录
	</button>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { apiUploadAvatar } from '@/apis/upload'
import webConfig from '@/config/global.config'

const userStorage = uni.getStorageSync('user') || {}

const user = reactive({
  ...userStorage,
  avatar: userStorage.avatar
    ? webConfig.baseURL + userStorage.avatar
    : '/static/logo.png'
})


const avatar = ref(user.avatar)

const chooseAvatar = async () => {
  const res = await uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera']
  })

  const filePath = res.tempFilePaths[0]
  avatar.value = filePath

  const result = await apiUploadAvatar(filePath)
  avatar.value = webConfig.baseURL + result.data
  user.avatar = avatar.value

  uni.setStorageSync('user', {
    ...userStorage,
    avatar: result.data
  })
}
// 退出登录
const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (!res.confirm) return

      uni.removeStorageSync('user')
      uni.removeStorageSync('token') 

      // uni.$emit('logout')

      uni.reLaunch({
        url: '/pages/login/login'
      })
    }
  })
}
const onGenderChange = (e: any) => {
  user.gender = e.detail.value === '0' ? 1 : 0
}

const saveProfile = () => {
  uni.setStorageSync('user', {
    ...userStorage,
    ...user
  })

  uni.showToast({
    title: '保存成功',
    icon: 'success'
  })
}
</script>

<style scoped lang="scss">
.profile {
  min-height: 100vh;
  background: #f5f6f8;
  padding: 40rpx 24rpx;
  box-sizing: border-box;
}

.avatar-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background-color: #eee;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.15);
}

.tip {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #888;
}

.info-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 10rpx 30rpx;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.06);
}

.info-item {
  display: flex;
  align-items: center;
  padding: 26rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.label {
  width: 120rpx;
  font-size: 28rpx;
  color: #555;
}

.input {
  flex: 1;
  font-size: 28rpx;
  padding: 16rpx 20rpx;
  background: #f7f8fa;
  border-radius: 14rpx;
  color: #333;
}

.picker-text {
  display: flex;
  align-items: center;
}

.save-btn {
  margin: 60rpx auto 0;
  width: 90%;
  height: 80rpx;
  background: linear-gradient(135deg, #ffffff, #ffffff);
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 40rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.save-btn:active {
  transform: scale(0.97);
  opacity: 0.9;
}
</style>
