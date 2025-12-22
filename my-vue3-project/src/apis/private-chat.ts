import request from '@/utils/request'

export const apiGetHistory = (userId: number, toUserId: number) => {
	return request({
		url: '/im/history',
		method: 'GET',
		data: { userId, toUserId}
	})
}