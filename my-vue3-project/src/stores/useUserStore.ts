import { reactive, ref, Ref } from 'vue' 
import { defineStore } from 'pinia'
import { apiMarkMessagesAsRead } from '@/apis/im'
import { apiMarkGroupMessagesAsRead } from '@/apis/group-chat'

// 定义消息类型
interface Message {
  id: number
  fromUserId: number
  toUserId: number
  content: string
  timestamp: string
}

interface UserInfo {
  id?: number
  name?: string
  [key: string]: any
}

interface ConversationUnread {
  [userId: number]: number
}

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const info = reactive<UserInfo>({})

  // token
  const token: Ref<string> = ref(uni.getStorageSync('token') || '')

  // WebSocket 实例
  const ws: Ref<WebSocket | null> = ref(null)

  // 全局未读消息数
  const unreadCount: Ref<number> = ref(0)

  // 每个联系人的未读数
  const conversationUnread = reactive<ConversationUnread>({})

  // 最新消息总线
  const messageBus: Ref<null> = ref(null)

  /** ================== WebSocket 相关 ================== */
  const setWebSocket = (socket: WebSocket) => {
    ws.value = socket
  }

  const emitMessage = (msg: any) => {
    messageBus.value = msg
	console.log("messageBus.value: ", messageBus.value)
  }

  /** ================== 未读消息相关 ================== */

  // 增加指定联系人未读数
  const addUnread = (fromUserId: number) => {
    if (!conversationUnread[fromUserId]) conversationUnread[fromUserId] = 0
    conversationUnread[fromUserId]++
    unreadCount.value++
  }

  // 清空指定联系人未读消息
  const clearUnreadByUserId = async (fromUserId: number) => {
	  
	  console.log("clearUncraete...被执行")
	  console.log("info: ", info)
    const count = conversationUnread[fromUserId] || 0
    unreadCount.value -= count
    conversationUnread[fromUserId] = 0

    // 调用后端批量标记已读
    if (fromUserId && info.id) {
      try {
        await apiMarkMessagesAsRead(fromUserId, info.id)
      } catch (err) {
        console.error('标记已读失败', err)
      }
    }
  }
  
   // 清空指定群聊未读消息
    const clearUnreadByGroupId = async (groupId: number) => {
      const count = conversationUnread[groupId] || 0;
      unreadCount.value -= count;
      conversationUnread[groupId] = 0;
  
      if (groupId && info.id) {
        try {
          await apiMarkGroupMessagesAsRead(info.id, groupId);
        } catch (err) {
          console.error('群聊标记已读失败', err);
        }
      }
    };

  return {
    info,
    token,
    ws,
    unreadCount,
    conversationUnread,
    messageBus,
    setWebSocket,
    emitMessage,
    addUnread,
    clearUnreadByUserId,
	clearUnreadByGroupId
  }
})
