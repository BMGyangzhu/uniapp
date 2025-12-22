<template>
	 <uni-list>
	 	<uni-list-item
		  clickable
		  v-for="item in notice"
		  :key="item.id" 
		  :title="item.name" 
		  :note="item.phone" 
		  :thumb="webConfig.baseURL + item.avatar"
		  thumb-size="lg" 
		  @click="handleClick(item)"
		  rightText="点击处理">
		 </uni-list-item>
	</uni-list>
</template>

<script setup lang="ts">
import { apiGetNotice, apiAcceptFriend } from '@/apis/contact'
import { ref } from 'vue'
import webConfig from '@/config/global.config'
import { onShow } from '@dcloudio/uni-app'

interface Notice {
 id: number
 avatar: string
 name: string
 phone: string
}

const myUserInfo = uni.getStorageSync('user')
const notice = ref<Notice[]>(null)
const loadNotice = async() => {
	try {
		const res = await apiGetNotice(myUserInfo.id)
		notice.value = res.data
	} catch (err) {
		console.error(err)
	}
}

const handleClick = (item: Notice) => {
  uni.showModal({
    title: '处理申请',
    content: '是否同意对方的好友申请？',
    success: (res) => {
      if (!res.confirm) return

      uni.showLoading({ title: '处理中' })

      apiAcceptFriend(myUserInfo.id, item.id)
        .then(() => {
          uni.showToast({
            title: '已同意',
            icon: 'success',
          })

          // 从通知列表中移除这条申请
          notice.value = notice.value.filter(n => n.id !== item.id)
        })
        .catch((err) => {
          uni.showToast({
            title: '处理失败',
            icon: 'none'
          })
		  console.error(err)
        })
        .finally(() => {
          uni.hideLoading()
        })
    }
  })
}
onShow(() => {
	loadNotice()
})
</script>

<style>
</style>