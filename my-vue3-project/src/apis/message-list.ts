import request from "@/utils/request";

export const apiGetMessageLists = (userId: number) => {
	return request({
		url: `/im/getMessageLists/${userId}`,
		method: 'GET'
	})
}

export const apiCreateMessageList = (userId: number, friendId: number) => {
	return request({
		url: '/im/createMessageList',
		method: 'POST',
		data: {
			userId,
			friendId
		}
	})
}