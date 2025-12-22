<template>
  <uni-list :border="true">
    <uni-list-chat
      v-for="item in data"
      :key="item.id"
      link
      :to="item.groupId 
                ? '/pages/message-list/group-chat-content/index?item=' + encodeURIComponent(JSON.stringify(item)) 
                : '/pages/message-list/chat-content/index?item=' + encodeURIComponent(JSON.stringify(item))"
      :title="item.name"
      :avatar="item.groupId ? '/static/logo.png' : webConfig.baseURL + item.toUserAvatar"
      @click="handleClick(item)"
      :note="item.lastMessage || ''"
      :time="item.lastMessageTime || ''"
      :badge-text="item.unreadCount"
    >
    </uni-list-chat>
  </uni-list>
</template>


<script setup lang="ts">
import { ref, watch } from 'vue'
import { apiGetMessageLists } from '@/apis/message-list';
import { useUserStore } from '@/stores/index'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app';
import webConfig from '@/config/global.config';

const data = ref<MessageList[]>([])
let user = null
const userStore = useUserStore()
interface MessageList {
	id?: number
	name?: string
	toUserAvatar?: string
	toUserId?: number
	unreadCount?: number
	userId?: number
	groupId?: number
	lastMessage?:string
	lastMessageTime?: string
}
function handleClick(item) {
  if (item.unreadCount) {
    userStore.unreadCount -= item.unreadCount;
    item.unreadCount = 0;

    if (item.groupId) {
      userStore.clearUnreadByGroupId(item.groupId);
    } else {
      userStore.clearUnreadByUserId(item.toUserId);
    }
  }
}

const loadMessageList = async () => {
  try {
    user = user || uni.getStorageSync('user') || {}
    if (!user.id){
      console.warn('没有拿到 userId')
      return
    }
    
    const res = await apiGetMessageLists(user.id)
    data.value = res.data
    console.log("消息列表加载完成")
  } catch (err) {
    console.error('加载消息列表失败:', err)
  }
}
function formatTimestamp(ts: number | string) {
  const date = new Date(Number(ts)); 
  const pad = (n: number) => n.toString().padStart(2, '0');

  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hour = pad(date.getHours());
  const minute = pad(date.getMinutes());
  const second = pad(date.getSeconds());

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
}

const refreshPage = () => {
	loadMessageList()
}

onLoad(async() => {
	loadMessageList()
})


onShow(() => {
  uni.$on('friendDeleted', refreshPage)
})

onUnload(() => {
  uni.$off('friendDeleted', refreshPage)
})

// defineExpose({
//   refreshPage,
//   loadMessageList
// })

watch(
  () => userStore.messageBus,
  (msg) => {
    console.log('监听到 msg 变化:', msg)

    if (!msg || !msg.type) return

    // ===== 私聊 =====
    if (msg.type === 'CHAT_MESSAGE') {
      let contact = data.value.find(
        c => c.toUserId === msg.data.toUserId
      )

      if (!contact) {
		 contact = data.value.find(
		 c => c.toUserId === msg.data.fromUserId) 
	  }
	  
	  if (!contact) return

      contact.lastMessage = msg.data.content
      contact.lastMessageTime = formatTimestamp(msg.data.gmtCreated)

      if (user.id !== msg.data.fromUserId) {
        contact.unreadCount = (contact.unreadCount || 0) + 1
        userStore.unreadCount++
      }
    }

    // ===== 群聊 =====
    if (msg.type === 'GROUP_CHAT_MESSAGE') {
      const group = data.value.find(
        g => g.groupId === msg.data.groupId
      )

      if (!group) return

      group.lastMessage = msg.data.content
      group.lastMessageTime = formatTimestamp(msg.data.gmtCreated)

      if (msg.data.fromUserId !== user.id) {
        group.unreadCount = (group.unreadCount || 0) + 1
        userStore.unreadCount++
      } else {
		   userStore.clearUnreadByGroupId(msg.data.groupId);
	  }
    }
  }
)

</script>

<style lang="scss" scoped>
.chat-custom-right {
	flex: 1;
	/* #ifndef APP-NVUE */
	display: flex;
	/* #endif */
	flex-direction: column;
	justify-content: space-between;
	align-items: flex-end;
}

.chat-custom-text {
	font-size: 12px;
	color: #999;
}

</style>
