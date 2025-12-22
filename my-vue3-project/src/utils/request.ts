import webConfig from '@/config/global.config'

export interface RequestOptions<T = any> {
  url: string
  method?: UniApp.RequestOptions['method']
  data?: T
  hideLoading?: boolean
  header?: UniApp.RequestOptions['header']
}


export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

function request<T = any>(options: RequestOptions): Promise<ApiResponse<T>> {
  const {
    url,
    method = 'GET',
    data,
    hideLoading = false,
    header = {}
  } = options

  const requestUrl = webConfig.baseURL + url

  // token
  const token = uni.getStorageSync('token')

  const headers: UniApp.RequestOptions['header'] = {
    'content-type': 'application/json',
    ...header
  }

  if (!webConfig.whiteListApi.includes(url) && token) {
    headers.token = token
  }

  if (!hideLoading) {
    uni.showLoading({ title: '加载中...' })
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: requestUrl,
      method,
      data,
      header: headers,

      success: (res) => {
        const result = res.data as ApiResponse<T>

        // 业务状态码处理
        if (result.code === 401) {
          uni.showToast({
            title: '身份认证失败，请重新登录',
            icon: 'none'
          })
          uni.removeStorageSync('token')

          uni.reLaunch({
            url: '/pages/login/login'
          })

          reject(result)
          return
        }
		
		if (result.code === 403) {
			uni.showToast({
				title: '权限不足',
				icon: 'none'
			})
			
			reject(result)
			return
		}

        if (result.code !== 0) {
          uni.showToast({
            title: result.msg || '请求错误',
            icon: 'none'
          })
          reject(result)
          return
        }

        resolve(result)
      },

      fail: (err) => {
        uni.showToast({
          title: '网络异常',
          icon: 'none'
        })
        reject(err)
      },

      complete: () => {
        if (!hideLoading) {
          uni.hideLoading()
        }
      }
    })
  })
}

export default request
