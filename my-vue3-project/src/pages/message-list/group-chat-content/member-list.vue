<template>
	 <uni-list>
	 	<uni-list-item
		  clickable
		  v-for="item in member"
		  :key="item.id" 
		  :title="item.name" 
		  :note="item.phone" 
		  :thumb="webConfig.baseURL + item.avatar"
		  thumb-size="lg" 
		  @click="handleClick(item)"
		  rightText="点击查看">
		 </uni-list-item>
	</uni-list>
</template>

<script setup lang="ts">
import { apiGetGroupMember  } from '@/apis/group-chat'
import { ref } from 'vue'
import webConfig from '@/config/global.config'
import { onLoad, onShow } from '@dcloudio/uni-app'

interface Member {
 id: number
 avatar: string
 name: string
 phone: string
}

const myUserInfo = uni.getStorageSync('user')
const member = ref<Member[]>(null)
const groupId = ref<number>()
const loadMember = async() => {
	try {
		const res = await apiGetGroupMember(groupId.value)
		member.value = res.data
	} catch (err) {
		console.error(err)
	}
}

const handleClick = (item: Member) => {
	uni.navigateTo({
		url: `/pages/contact/contact-info?userId=${item.id}`,
		fail: (err) => {
			console.error(err)
		}
	})
}
onShow(() => {
	loadMember()
})

onLoad((options) => {
	if (options.groupId) {
		groupId.value = options.groupId
	} else {
		console.warn('没有接收到 options.groupId 参数')
	}
})
</script>

<style>
</style>