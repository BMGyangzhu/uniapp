<template>
	<view>Hello uni-app</view>
</template>
<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
import { apiGetUserById, apiLogin } from "@/apis/user"
import { useUserStore } from '@/stores/index'
import { ref } from "vue"

const user = ref(null);
const userStore = useUserStore()

const connectWS = () => {
  const token = uni.getStorageSync('token')
  if (!token) return

  // uni-app WebSocket
  const ws = uni.connectSocket({
    url: `ws://localhost:8877/ws?token=${token}`,
    success() {
      console.log('WebSocket 实例已创建')
    }
  })

  ws.onOpen(() => {
    console.log('WebSocket 已连接')
    userStore.setWebSocket(ws)
  })

  ws.onMessage((res) => {
    if (!res.data) return
    const msg = JSON.parse(res.data)

    if (msg.type === 'KICK_OUT') {
      uni.showToast({
        title: msg.data || '你的账号已在其他地方登录',
        icon: 'none',
        duration: 1500
      })

      uni.removeStorageSync('token')
      uni.removeStorageSync('user')
      userStore.token = null
      userStore.info = {}
      ws.close()
      userStore.setWebSocket(null)
      uni.reLaunch({ url: '/pages/login/index' })
      return
    }

    if (msg.type !== 'KICK_OUT') {
      userStore.emitMessage(msg)

      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      if (currentPage.route !== 'pages/message-list/index') {
        userStore.unreadCount++
      }

     
    }
  })

  ws.onClose(() => {
    console.log('WebSocket 已断开')
    userStore.setWebSocket(null)
  })

  ws.onError((err) => {
    console.error('WebSocket 错误', err)
  })
}
onLaunch(() => {
  console.log("App Launch");

  const token = uni.getStorageSync('token')
  const user = uni.getStorageSync('user')
  console.log("user: ", user)

  if (token && user) {
    userStore.token = token
   Object.assign(userStore.info, user)
	console.log("以进入token && user if: ", user)
  } else {
    uni.reLaunch({
      url: '/pages/login/login'
    })
  }
})

onShow(() => {
  console.log("App Show");

  // 已登录才连 WS
  if (userStore.token) {
    connectWS()
  }
})
onHide(() => {
  console.log("App Hide");
});
</script>
<style></style>
