<template>
	<view class="chat">
		<!-- 顶部标题 -->
		<view class="topTabbar">
			<uni-icons type="arrow-left" class="icon" size="30" @click="goBack"></uni-icons>
			<view class="text">{{ friendInfo.name || '联系人' }}</view>
		</view>

		<scroll-view 
		:style="{height: `${windowHeight-inputHeight - 180}rpx`}"
		scroll-y="true" id="scrollview" :scroll-top="scrollTop" class="scroll-view">
			<!-- 聊天内容 -->
			<view class="chat-body" id="msglistview">
				<view v-for="(item, index) in msgList" :key="item.id">
					<view class="item self" v-if="item.fromUserId === myUserInfo.id">
						<view class="content right">{{ item.content }}</view>
						<image class="avatar" :src="webConfig.baseURL + myUserInfo.avatar" />
					</view>

					<view class="item" v-if="item.fromUserId === friendInfo.toUserId">
						<image class="avatar" :src="webConfig.baseURL + friendInfo.toUserAvatar" />
						<view class="content left">{{ item.content }}</view>
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
	import { computed, ref } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';
	import { useUserStore } from '@/stores/index'
	import type { MessageList } from '@/pages/message-list/index.vue';
	import { apiGetHistory } from '@/apis/private-chat';
	import webConfig from '@/config/global.config';
	interface Chat {
		id ?: number;
		fromUserId ?: number;
		toUserId ?: number;
		content ?: string;
		gmtCreated ?: string;
		type?: string
	}
	
	const userStore = useUserStore()
	const ws = computed(() => userStore.ws)
	const keyboardHeight = ref(0);
	const bottomHeight = ref(0);
	const scrollTop = ref(0);
	const windowHeight = computed(() => rpxTopx(uni.getSystemInfoSync().windowHeight));
	// 键盘弹起来的高度+发送框高度
	const inputHeight = computed(() => bottomHeight.value + keyboardHeight.value);

	const msgList = ref([]);
	const myUserInfo = ref({});
	const friendInfo = ref<MessageList>({} as MessageList);
	const sendMsg = ref();
	const chatMsg = ref()
	
	const handleSend = () => {
		console.log("chatMsg:", chatMsg.value)
		let obj: Chat = {
			data:{
				fromUserId: myUserInfo.value.id,
				toUserId: friendInfo.value.toUserId || friendInfo.value.id,
				content: chatMsg.value,
			},
			
			type: 'CHAT_MESSAGE'
		}
		msgList.value.push(obj.data)
		chatMsg.value = ''
		
		ws.value.send({
			data: JSON.stringify(obj),
			success: () => { console.log('消息发送成功')},
			fail: (err) => { console.log('消息发送失败', err)}
		})
		
		// console.log("msgList: ", msgList.value)
	}
	const rpxTopx = (px) => {
		let deviceWidth = uni.getSystemInfoSync().windowWidth;
		let rpx = (750 / deviceWidth) * Number(px);
		return Math.floor(rpx);
	};
	const focus = () => {
		scrollToBottom();
	};
	const blur = () => {
		scrollToBottom();
	};
	const goBack = () => {
		uni.navigateBack({
			delta: 1 // 返回上一级页面
		});
	};
	// 监视聊天发送栏高度
	const sendHeight = () => {
		setTimeout(() => {
			let query = uni.createSelectorQuery();
			query.select('.send-msg').boundingClientRect();
			query.exec((res) => {
				bottomHeight.value = rpxTopx(res[0].height);
			});
		}, 10);
	};
	const scrollToBottom = (e) => {
		setTimeout(() => {
			let query = uni.createSelectorQuery();
			query.select('#scrollview').boundingClientRect();
			query.select('#msglistview').boundingClientRect();
			query.exec((res) => {
				if (res[1].height > res[0].height) {
					scrollTop.value = rpxTopx(res[1].height - res[0].height);
				}
			});
		}, 15);
	};
	const getHistory = async () => {
		try {
			myUserInfo.value = uni.getStorageSync('user');
			const res = await apiGetHistory(myUserInfo.value.id, friendInfo.value.toUserId || friendInfo.value.id);
			msgList.value = res.data;
			// console.log(msgList.value);
			// console.log(myUserInfo.value);
			// console.log(friendInfo.value);
		} catch (err) {
			console.error(err);
		}
	};
	onLoad((options) => {
		// #ifdef MP || APP-PLUS
		uni.onKeyboardHeightChange((res) => {
			keyboardHeight.value = rpxTopx(res.height);
		});
		// #endif
		
		if (options.item) {
			friendInfo.value = JSON.parse(decodeURIComponent(options.item));
			console.log("friendInfo.value: " ,friendInfo.value)
		} else {
			console.warn('没有接收到 item 参数');
			friendInfo.value = {} as MessageList;
		}

		getHistory();
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

