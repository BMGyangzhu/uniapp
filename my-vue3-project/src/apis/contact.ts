import request from '@/utils/request'


export const apiGetContacts = (userId: number) => {
	return request({
		url: `/contact/getContacts/${userId}`,
		method: 'GET',
	})
}

export const apiIsFriend = (userId: number, toUserId: number) => {
	return request({
		url: '/contact/isFriend',
		method: 'GET',
		data: {
			userId: userId,
			toUserId: toUserId
		}
	})
}

export const apiDeleteContact = (userId: number, friendId: number) => {
	return request({
		url: '/contact/deleteContact',
		method: 'POST',
		data: {
			userId,
			friendId
		}
	})
}

export const apiAddContact = (userId: number, friendId: number) => {
	return request({
		url: '/contact/addContact',
		method: 'POST',
		data: {
			userId,
			friendId
		}
	})
}

export const apiGetUserIdByPhoneNumber = (phone: number) => {
	return request({
		url: `/contact/getUserIdByPhoneNumber/${phone}`,
		method: 'GET'
	})
}

export const apiGetNotice = (userId: number) => {
	return request({
		url: `/contact/getNotice/${userId}`,
		method: 'GET'
	})
}

export const apiAcceptFriend = (userId: number, friendId: number) => {
	return request({
		url: '/contact/accept',
		method: 'POST',
		data: {
			userId,
			friendId
		}
	})
}

export const apiGetUnreadFriendNotice = (userId: number) => {
	return request({
		url: `/contact/unread/${userId}`,
		method: 'GET'
	})
}

export const apiFriendNoticeReadAll = (userId: number) => {
	return request({
		url: `/contact/readAll/${userId}`,
		method: 'GET'
	})
}