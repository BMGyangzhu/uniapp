import request from '@/utils/request'

export const apiLogin = (data: {
  phone: string
  password: string
}) => {
  return request({
    url: '/user/login',
    method: 'POST',
    data
  })
}

export const apiGetUserById = (userId: number) => {
  return request({
    url: `/user/info/${userId}`,
    method: 'GET',
  })
}


