<template>
  <view v-if="user" class="container">
    <image :src="webConfig.baseURL + user.avatar" alt="头像" />
    <text>{{ user.name }}</text>
	<text>{{ user.gender ? '男' : '女'}}</text>
    <text>{{ user.phone }}</text>
    <text>{{ user.email }}</text>
	<view v-if="isFriend"
	  class="message-button"
	  hover-class="message-button-hover"
	  @click="handleClick"
	>
	  发消息
	</view>
	<view v-else
	class="add-contact"
	hover-class="message-button-hover"
	:class="{ disabled: isApplying }"
	 @click="addFriend">
		添加好友
	</view>
	
	<view v-if="isFriend"
	  class="message-button danger"
	  hover-class="message-button-danger-hover"
	  @click="deleteContact"
	>
	  删除好友
	</view>
	
	<!-- <view v-if="isFriend"
		class="message-button"
		hover-class="message-button-hover"
		@click="inviteGroupClick"
	>邀请入群</view> -->
	<view class="message-button" v-if="isFriend" @click="openDialog">
		邀请入群
	</view>
	<uni-popup ref="popup" type="center">
	   <view class="popup-container">
	  
	      <!-- 标题 -->
	      <view class="popup-header">
	        选择群聊
	      </view>
	  
	      <!-- 群聊列表 -->
	      <scroll-view
	        scroll-y
	        class="group-list"
	      >
	        <view
	          v-for="group in groupList"
	          :key="group.id"
	          class="group-item"
	          :class="{ active: selectedGroupId === group.id }"
	          @click="selectGroup(group)"
	        >
	          <image
	            class="group-avatar"
	            :src="group.avatar || '/static/logo.png'"
	          />
	          <text class="name">{{ group.name }}</text>
	        </view>
	      </scroll-view>
	  
	      <view class="popup-footer">
	        <button
	          type="primary"
	          :disabled="!selectedGroupId"
	          @click="confirmInvite"
	        >
	          确认
	        </button>
	      </view>
	  
	    </view>
	</uni-popup>
	
  </view>
  
  

  <view v-else>
    <text>加载中...</text>
  </view>
  
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from "@dcloudio/uni-app";
import webConfig from '@/config/global.config'
import { apiGetUserById } from '@/apis/user' 
import { apiIsFriend, apiDeleteContact, apiAddContact } from '@/apis/contact'
import { apiCreateMessageList } from '@/apis/message-list'
import { apiInviteGroup, apiGetGroupByUserId } from '@/apis/group-chat';

interface User {
  id?: number
  name?: string
  gender?: boolean
  phone?: string | null
  email?: string | null
  avatar?: string
}

interface Group {
  id: number
  name: string
  avatar: string
}

const groupList = ref<Group[]>([])
const popup = ref<any>(null)

const selectedGroupId = ref<number | null>(null)

const openPopup = () => {
  selectedGroupId.value = null
  popup.value.open()
}

const selectGroup = (group: Group) => {
  selectedGroupId.value = group.id
}

const confirmInvite = async() => {
  if (!selectedGroupId.value) return

  console.log('选择的群聊:', selectedGroupId.value)
  await apiInviteGroup(selectedGroupId.value, user.value?.id)
  popup.value.close()
}
const inputDialog = ref<any>(null)

const openDialog = () => {
  popup.value?.open()
}

const user = ref<User | null>(null)
const myUserInfo = ref()
const isFriend = ref<boolean>(false)
const isApplying = ref<boolean>(false)


const inviteGroupClick = () => {
  uni.showModal({
    title: "提示",
    content: "您确定要邀请对方加入群聊吗？",
    success: (res) => {
      if (!res.confirm) return

      uni.showLoading({ title: '申请发送中' })

      apiInviteGroup(groupId, user.value?.id)
        .then(() => {
          uni.showToast({
            title: '邀请已发送',
            icon: 'success'
          })
        })
        .catch(err => {
          console.error(err)
          uni.showToast({
            title: '邀请失败',
            icon: 'none'
          })
        })
        .finally(() => {
          uni.hideLoading()
        })
    }
  })
}

const loadMyGroup = async() => {
	try {
		const res = await apiGetGroupByUserId(myUserInfo.value.id);
		groupList.value = res.data
	} catch (err) {
		console.error(err)
	}
}
const handleClick = async() => {
	await apiCreateMessageList(myUserInfo.value.id ,user.value?.id)
  uni.navigateTo({
    url:
      '/pages/message-list/chat-content/index?item=' +
      encodeURIComponent(JSON.stringify({
		  ...user.value,
		  toUserAvatar: user.value?.avatar,
		  toUserId: user.value?.id
	  }))
  })
}

const addFriend = () => {

  uni.showModal({
    title: "提示",
    content: "您确定要发送添加申请吗？",
    success: (res) => {
      if (!res.confirm) return

      uni.showLoading({ title: '申请发送中' })

      apiAddContact(myUserInfo.value.id, user.value?.id)
        .then(() => {
          isApplying.value = true

          uni.showToast({
            title: '申请已发送',
            icon: 'success'
          })
        })
        .catch((err) => {
          console.error('添加好友失败', err)
          uni.showToast({
            title: '发送失败',
            icon: 'none'
          })
		   isApplying.value = false
        })
        .finally(() => {
          uni.hideLoading()
        })
    }
  })
}

const deleteContact = () => {
	try {
		uni.showModal({
			title:"删除好友",
			content:"您确定要删除好友吗？",
			success: async (res) => {
				if (res.confirm) {
					await apiDeleteContact(myUserInfo.value.id, user.value?.id)
					uni.showToast({ title: '删除成功', icon: 'success' });
					
					uni.$emit('friendDeleted')
					   
					   
					setTimeout(() => {
					    uni.navigateBack();
					}, 500);
				} else if (res.cancel) {
					console.error('删除好友失败', err);
					uni.showToast({ title: '删除失败', icon: 'none' });
					setTimeout(() => {
					    uni.navigateBack();
					}, 500);
				}
			}
		})
	} catch(err) {
		console.error(err)
	}
}

onLoad(async (options: any) => {
  const userId = options.userId
  if (!userId) {
    console.error('没有接收到 userId: ',options )
    return
  }

  try {
	 myUserInfo.value = uni.getStorageSync('user')
    const res = await apiGetUserById(userId)
	const result = await apiIsFriend(myUserInfo.value.id, userId)
	isFriend.value = result.data
    user.value = res.data 
	loadMyGroup()
    console.log('获取的用户信息：', user.value)
	console.log('是否为好友：', isFriend.value)
  } catch (err) {
    console.error('获取用户信息失败', err)
  }
})
</script>

<style lang="scss" scoped>
.container{
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
}
.message-button {
  width: 80%;
  margin-top: 20rpx;
  padding: 24rpx 0;
  text-align: center;
  color: #fff;
  border-radius: 12rpx;
  background-color: #007aff;

  &-hover { background-color: #005ecb; }

  &.danger {
    background-color: #ff3b30;
  }

  &.danger-hover {
    background-color: #d63028;
  }

  /* #ifdef H5 */
  &:hover { background-color: #005ecb; cursor: pointer; }
  &.danger:hover { background-color: #d63028; }
  /* #endif */
}
image {
  width: 100px;
  height: 100px;
  /* border-radius: 50%; */
  margin-top: 10rpx;
  border: 1px solid gray;
}
text {
  display: block;
  margin-top: 10px;
}
.add-contact {
  padding: 20rpx;
  text-align: center;
  background-color: #2979ff;
  color: #fff;
  border-radius: 8rpx;
}
/* disabled 状态 */
.add-contact.disabled {
  background-color: #ccc;
  color: #999;
  pointer-events: none; /* 直接禁止点击 */
}
.popup-container {
  background: #fff;
  border-radius: 16rpx 16rpx 0 0;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.popup-header {
  padding: 24rpx;
  font-size: 32rpx;
  font-weight: bold;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.group-list {
  flex: 1;
  overflow: hidden;
}

.group-item {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
}

.group-item.active {
  background: #f0f7ff;
}

.group-avatar {
  width: 72rpx;
  height: 72rpx;
  margin-right: 20rpx;
}

.name {
  font-size: 28rpx;
}

.popup-footer {
  padding: 20rpx;
  border-top: 1px solid #eee;
}
</style>
