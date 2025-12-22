<template>
	<view class="container">
		
		<view @click="openSearch">
			<uni-icons type="search" size="30"></uni-icons>
			<view>
				<text>搜索</text>
			</view>
		</view>
		<view @click="goToFriendNotice">
			<uni-icons type="personadd" size="30"></uni-icons>
			<view>
				<text>新的朋友</text>
			</view>
			<uni-badge  :text="friendUnreadNotice" />
		</view>
		
		<view @click="goToGroupNotice">
			<uni-icons type="mail-open" size="30"></uni-icons>
			<view>
				<text>群聊邀请</text>
			</view>
			<uni-badge  :text="groupUnreadNotice" />
		</view>
		
		
	</view>
	<uni-indexed-list
	class="indexed-list"
	:options="res"
	@click="handleItemClick"
	>
	</uni-indexed-list>

</template>

<script lang="ts" setup>
import {  onUnload, onShow, onLoad } from '@dcloudio/uni-app';  // 从 uni-app 导入
import { ref } from 'vue'
import { apiFriendNoticeReadAll, apiGetContacts, apiGetUnreadFriendNotice, apiGetUserIdByPhoneNumber } from '@/apis/contact';
import { apiGetGroupInviteUnread, apiGroupInviteReadAll } from '@/apis/group-chat';

const res = ref([])
const groupUnreadNotice = ref<number>(0)
const friendUnreadNotice = ref<number>(0)
let user = null

const getUnreadFriendNotice = async () => {
	try {
		const res = await apiGetUnreadFriendNotice(user.id)
		friendUnreadNotice.value = res.data
	} catch (err) {
		console.error(err)
	}
}

const readAllFreindNotice = async () => {
	try {
		await apiFriendNoticeReadAll(user.id)
		friendUnreadNotice.value = 0
	}catch (err) {
		console.log(err)
	}
}

const getGroupInviteUnread = async () => {
	try{
		const res = await apiGetGroupInviteUnread(user.id)
		groupUnreadNotice.value = res.data
	} catch (err) {
		console.error(err)
	}
}
const readAllGroupInvite = async () => {
	try {
		await apiGroupInviteReadAll(user.id)
		groupUnreadNotice.value = 0
	} catch (err) {
		console.error(err)
	}
}

const goToFriendNotice = () => {
	
	readAllFreindNotice()
	uni.navigateTo({
		url: '/pages/contact/notice'
	})
}
const goToGroupNotice = () => {
	console.log('click')
	
	readAllGroupInvite()
	uni.navigateTo({
		url: '/pages/contact/group-notice'
	})
}
// 加载联系人数据
const loadContacts = async () => {
  uni.showLoading({ title: '加载中' })
  try {
    user = uni.getStorageSync('user')
    const result = await apiGetContacts(user.id)
    res.value = result.data || [] 
    console.log('联系人数据加载完成')
  } catch (err) {
    console.error('获取联系人失败', err)
  } finally {
    uni.hideLoading()
  }
}

// 刷新方法
const refreshPage = () => {
  loadContacts()
}

const openSearch = () => {
  uni.showModal({
    title: '搜索用户',
    placeholderText: '请输入用户手机号码',
    editable: true,
    success: (res) => {
      if (!res.confirm) return

      const value = (res.content || '').trim()

      if (!value) {
        uni.showToast({ title: '请输入手机号码', icon: 'none' })
        return
      }

      if (!/^1\d{10}$/.test(value)) {
        uni.showToast({ title: '手机号格式不正确', icon: 'none' })
        return
      }

      uni.showLoading({ title: '加载中' })

      apiGetUserIdByPhoneNumber(value)
        .then(result => {
          const userId = result.data
          uni.navigateTo({
            url: `/pages/contact/contact-info?userId=${userId}`
          })
        })
        .catch(err => {
          console.error(err)
          uni.showToast({ title: '用户不存在', icon: 'none' })
        })
        .finally(() => {
          uni.hideLoading()
        })
    }
  })
}
const handleItemClick = (event: any) => {
  const userId = event.item.userId; 
  console.log('点击的 userId', userId);

  uni.navigateTo({
    url: `/pages/contact/contact-info?userId=${userId}`,
    fail: (err) => console.error('跳转失败', err)
  });
};

onLoad(async () => {
  console.log('联系人页面加载')
  await loadContacts()
  getGroupInviteUnread()
  getUnreadFriendNotice()
  
})

onShow(() => {
  console.log('联系人页面显示')
  // 监听删除好友事件
  uni.$on('friendDeleted', refreshPage)
})

onUnload(() => {
  console.log('联系人页面卸载')
  // 清理事件监听
  uni.$off('friendDeleted', refreshPage)
})

// defineExpose({
//   refreshPage,
//   loadContacts
// })
</script>

<style lang="scss" scoped>
.container {
	display: flex;
	  flex-direction: column;
	  flex: 1;
  view {
    display: flex;
    align-items: center;
    padding: 10px;

    image {
      width: 50rpx;
      height: 50rpx;
      margin-right: 10rpx;
    }
  }
}
.indexed-list{
	position: static !important;
}
</style>