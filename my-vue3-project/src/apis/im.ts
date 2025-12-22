import request from "@/utils/request"

export const apiCreateContact = (fromUserId: number, toUserId: number) => {
	return request({
		url: '/im/addContact',
		method: 'POST',
		data: {
			userId: fromUserId,
			toUserId
		}
	})
}

export const apiGetContact = (fromUserId: number, toUserId: number) => {
	return request({
		url: '/im/getMessageList',
		method: 'POST',
		data: {
			userId: fromUserId,
			toUserId
		}
	})
}

export const apiMarkMessagesAsRead = (userId: number, toUserId: number) => {
	return request({
		url: `/im/readAll?userId=${userId}&toUserId=${toUserId}`,
		method: 'POST'
	})
}

export const apiGetHistory = (userId: number, toUserId: number) => {
	return request({
		url: '/im/history',
		method: 'GET',
		data: {
			userId,
			toUserId
		}
	})
}

export const apiGetUnread = (userId: number) => {
	return request({
		url: '/im/unread',
		method: 'GET',
		data: {
			userId
		}
	})
}