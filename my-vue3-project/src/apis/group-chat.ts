import request from "@/utils/request";

export const apiGetGroupHistory = (groupId: number, limit = 50) =>  {
	return request({
		url: '/group/history',
		method: 'GET',
		data: { groupId, limit}
	})
}

export const apiMarkGroupMessagesAsRead = (userId: number, groupId: number) => {
	return request({
		url: '/group/readAll',
		method: 'POST',
		data: 
		{ userId: userId,
		  groupId: groupId
		}
	})
}

export const  apiGetGroupMemberCount = (groupId: number) => {
	return request({
		url: `/group/getGroupMemberCount/${groupId}`,
		method: 'GET'
	})
}

export const apiGetGroupMember = (groupId: number) => {
	return request({
		url: `/group/getGroupMember/${groupId}`,
		method: 'GET'
	})
}

export const apiInviteGroup = (groupId: number, userId: number) => {
	return request({
		url: '/group/inviteGroup',
		method: 'POST',
		data: {
			groupId,
			userId
		}
	})
}

export const apiGetGroupByUserId = (userId: number) => {
	return request({
		url: `/group/getGroupByUserId/${userId}`,
		method: 'GET'
	})
}

export const apiGetGroupNotice = (userId: number) => {
	return request({
		url: `/group/getNotice/${userId}`,
		method: 'GET'
	})
}

export const apiAcceptGroupInvite = (groupId: number, userId: number) => {
	return request({
		url: '/group/acceptGroupInvite',
		method: 'POST',
		data:{
			groupId,
			userId
		}
	})
}

export const apiGetGroupInviteUnread = (userId: number) => {
	return request({
		url: `/group/unread/${userId}`,
		method: 'GET'
	})
}

export const apiGroupInviteReadAll = (userId: number) => {
	return request({
		url: `/group/readAll/${userId}`,
		method: 'GET'
	})
}