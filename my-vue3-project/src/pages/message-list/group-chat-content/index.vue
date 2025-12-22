<template>
	<view class="chat">
		<!-- 顶部标题 -->
		<view class="topTabbar">
			<uni-icons type="arrow-left" class="icon" size="30" @click="goBack"></uni-icons>
			<view class="text">{{ groupInfo.name || '群聊' }}{{ '(' + groupMemberCount +')'}}
			<uni-icons @click="openMemberList" type="list" size="25"></uni-icons>
			</view>
		
		</view>

		<scroll-view 
		:style="{height: `${windowHeight-inputHeight - 180}rpx`} "
		scroll-y="true" id="scrollview" :scroll-top="scrollTop" class="scroll-view">
			<!-- 聊天内容 -->
			<view class="chat-body" id="msglistview">
				<view v-for="(item, index) in msgList" :key="item.id">
					<view class="item self" v-if="item.fromUserId === myUserInfo.id">
						<view class="content right">{{ item.content }}</view>
						<image class="avatar" :src="webConfig.baseURL + myUserInfo.avatar" />
					</view>

					<view class="item other" v-else>
						<image class="avatar" :src="webConfig.baseURL + item.fromUserAvatar" />
						<view class="msg-wrapper">
							<text class="name">{{ item.fromUserName}}</text>
							<view class="content left">{{ item.content }}</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<!-- 底部信息发送栏 -->
		<view class="chat-bottom" :style="{ height: `${inputHeight}rpx` }">
			<view class="send-msg" :style="{ bottom: `${keyboardHeight - 60}rpx` }">
				<view class="uni-textarea">
					<textarea v-model="chatMsg" maxlength="300" confirm-type="send" @confirm="handleSend"
						placeholder="快来聊天吧~" :show-confirm-bar="false" :adjust-position="false" @linechange="sendHeight"
						@focus="focus" @blur="blur" auto-height></textarea>
				</view>
				<button @click="handleSend" class="send-btn">发送</button>
			</view>
		</view>
	</view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useUserStore } from '@/stores/index';
import webConfig from '@/config/global.config';
import { apiGetGroupHistory,  apiGetGroupMemberCount } from '@/apis/group-chat';

interface Chat {
	id?: number;
	fromUserId?: number;
	groupId?: number;
	content?: string;
	gmtCreated?: string;
	avatar?: string;
	type?: string;
}

const userStore = useUserStore()
const ws = computed(() => userStore.ws)

const keyboardHeight = ref(0);
const bottomHeight = ref(0);
const scrollTop = ref(0);
const windowHeight = computed(() => rpxTopx(uni.getSystemInfoSync().windowHeight));
const inputHeight = computed(() => bottomHeight.value + keyboardHeight.value);

const msgList = ref<Chat[]>([]);
const myUserInfo = ref<any>({});
const groupInfo = ref<any>({});
const chatMsg = ref('');
const groupMemberCount = ref<number>(0)

const handleSend = () => {
	if (!chatMsg.value) return;

	const obj = {
		data: {
			fromUserId: myUserInfo.value.id,
			groupId: groupInfo.value.groupId,
			content: chatMsg.value,
		},
		type: 'GROUP_CHAT_MESSAGE'
	}

	msgList.value.push(obj.data)
	chatMsg.value = ''

	ws.value.send({
		data: JSON.stringify(obj),
		success: () => console.log('消息发送成功'),
		fail: (err) => console.log('消息发送失败', err)
	})
}

const openMemberList = () => {
	uni.navigateTo({
		url: `/pages/message-list/group-chat-content/member-list?groupId=${groupInfo.value.groupId}`,
		fail: (err) => console.error('跳转失败', err)
	})
}

// 工具函数
const rpxTopx = (px) => Math.floor((750 / uni.getSystemInfoSync().windowWidth) * Number(px));

const focus = () => scrollToBottom();
const blur = () => scrollToBottom();
const goBack = () => {
		uni.navigateBack({
			delta: 1 // 返回上一级页面
		});
	};
const scrollToBottom = () => {
	setTimeout(() => {
		const query = uni.createSelectorQuery();
		query.select('#scrollview').boundingClientRect();
		query.select('#msglistview').boundingClientRect();
		query.exec((res) => {
			if (res[1].height > res[0].height) {
				scrollTop.value = rpxTopx(res[1].height - res[0].height);
			}
		});
	}, 15);
};

const sendHeight = () => {
	setTimeout(() => {
		const query = uni.createSelectorQuery();
		query.select('.send-msg').boundingClientRect();
		query.exec((res) => { bottomHeight.value = rpxTopx(res[0].height) });
	}, 10);
};

const getHistory = async () => {
	myUserInfo.value = uni.getStorageSync('user');
	const res = await apiGetGroupHistory(groupInfo.value.groupId);
	msgList.value = res.data;
};

onMounted(async() => {
	if (getCurrentPages().length) {
		const options = getCurrentPages().pop().options;
		if (options.item) groupInfo.value = JSON.parse(decodeURIComponent(options.item));
	}


	// 键盘监听
	// #ifdef MP || APP-PLUS
	uni.onKeyboardHeightChange((res) => keyboardHeight.value = rpxTopx(res.height));
	// #endif
	getHistory();
	const res = await apiGetGroupMemberCount(groupInfo.value.groupId)
	groupMemberCount.value = res.data
});


</script>
<style lang="scss" scoped>
	
	$chatContentbgc: #C2DCFF;
	$sendBtnbgc: #4F7DF5;
	
	view,button,text,input,textarea {
		margin: 0;
		padding: 0;
		box-sizing: border-box;
	}
 
	/* 聊天消息 */
	.chat {
     .topTabbar {
          width: 100%;
          height: 90rpx;
          line-height: 90rpx;
          display: flex;
          margin-top: 80rpx; // 避免覆盖电量那一行
          justify-content: space-between;
      
          .icon {
            margin-left: 20rpx;
          }
      
          .text {
            margin: auto;
            font-size: 16px;
            font-weight: 700;
          }
      
          .button {
            width: 10%;
            margin: auto 20rpx auto 0rpx;
          }
        }
		.scroll-view {
			::-webkit-scrollbar {
					    display: none;
					    width: 0 !important;
					    height: 0 !important;
					    -webkit-appearance: none;
					    background: transparent;
					    color: transparent;
					  }
			
			background-color: #F6F6F6;
			
			.chat-body {
				display: flex;
				flex-direction: column;
				padding-top: 23rpx;
				
				.self {
					justify-content: flex-end;
				}
				.item {
					display: flex;
					padding: 23rpx 30rpx;
 
					.right {
						background-color: $chatContentbgc;
					}
					.left {
						background-color: #FFFFFF;
					}
                    // 聊天消息的三角形
					.right::after {
						position: absolute;
						display: inline-block;
						content: '';
						width: 0;
						height: 0;
						left: 100%;
						top: 10px;
						border: 12rpx solid transparent;
						border-left: 12rpx solid $chatContentbgc;
					}
 
					.left::after {
						position: absolute;
						display: inline-block;
						content: '';
						width: 0;
						height: 0;
						top: 10px;
						right: 100%;
						border: 12rpx solid transparent;
						border-right: 12rpx solid #FFFFFF;
					}
					.msg-wrapper{
						 margin-top: -20rpx;
						 
					}
					.name{
						font-size: 8rpx;
						font-weight: 300;
						margin-left: 16rpx;
						
					}
					.content {
						position: relative;
						max-width: 486rpx;
						border-radius: 8rpx;
						word-wrap: break-word;
						padding: 24rpx 24rpx;
						margin: 0 24rpx;
						border-radius: 5px;
						font-size: 32rpx;
						font-family: PingFang SC;
						font-weight: 500;
						color: #333333;
						line-height: 42rpx;
					}
 
					.avatar {
						display: flex;
						justify-content: center;
						width: 78rpx;
						height: 78rpx;
						background: white;
						// border-radius: 50rpx;
						overflow: hidden;
						
						image {
							align-self: center;
						}
 
					}
				}
			}
		}
 
		/* 底部聊天发送栏 */
		.chat-bottom {
			width: 100%;
			height: 100rpx;
			background: #F4F5F7;
			transition: all 0.1s ease;
			
			.send-msg {
				display: flex;
				align-items: flex-end;
				padding: 16rpx 30rpx;
				width: 100%;
				min-height: 177rpx;
				position: fixed;
				bottom: 0;
				background: #fff;
				transition: all 0.1s ease;
			}
 
			.uni-textarea {
				padding-bottom: 70rpx;  
				textarea {
					width: 537rpx;
					min-height: 75rpx;
					max-height: 500rpx;
					background: #f1f1f1;
					border-radius: 40rpx;
					font-size: 32rpx;
					font-family: PingFang SC;
					color: #333333;
					line-height: 74rpx;
					padding: 5rpx 8rpx;
          text-indent: 30rpx;
				}
			}
            
			.send-btn {
				display: flex;
				align-items: center;
				justify-content: center;
				margin-bottom: 76rpx;
				margin-left: 25rpx;
				width: 120rpx;
				height: 75rpx;
				background: #ed5a65;
				border-radius: 50rpx;
				font-size: 28rpx;
				font-family: PingFang SC;
				font-weight: 500;
				color: #FFFFFF;
				line-height: 28rpx;
			}
		}
	}
</style>
