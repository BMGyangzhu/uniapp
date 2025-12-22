<template>
	 <uni-list>
	 	<uni-list-item
		  clickable
		  v-for="item in group"
		  :key="item.id" 
		  :title="item.name" 
		  :note="item.phone" 
		  :thumb="'/static/logo.png'"
		  thumb-size="lg" 
		  @click="handleClick(item)"
		  rightText="点击处理">
		 </uni-list-item>
	</uni-list>
</template>

<script setup lang="ts">
import { apiGetNotice, apiAcceptFriend } from '@/apis/contact'
import { apiAcceptGroupInvite, apiGetGroupNotice } from '@/apis/group-chat'
import { ref } from 'vue'
import webConfig from '@/config/global.config'
import { onShow } from '@dcloudio/uni-app'

interface Group {
 id: number
 avatar: string
 name: string
}

const myUserInfo = uni.getStorageSync('user')
const group = ref<Group[]>(null)
const loadGroup = async() => {
	try {
		const res = await apiGetGroupNotice(myUserInfo.id)
		group.value = res.data
	} catch (err) {
		console.error(err)
	}
}

const handleClick = (item: Group) => {
  uni.showModal({
    title: '处理申请',
    content: '是否同意对方的群聊邀请？',
    success: (res) => {
      if (!res.confirm) return

      uni.showLoading({ title: '处理中' })

      apiAcceptGroupInvite(item.id, myUserInfo.id)
        .then(() => {
          uni.showToast({
            title: '已同意',
            icon: 'success',
          })

          // 从通知列表中移除这条申请
          group.value = group.value.filter(n => n.id !== item.id)
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
	loadGroup()
})
</script>

<style>
</style>