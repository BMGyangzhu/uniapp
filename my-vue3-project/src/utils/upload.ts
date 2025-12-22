import webConfig from '@/config/global.config'

export interface UploadOptions {
  url: string
  filePath: string
  name?: string
  formData?: Record<string, any>
  hideLoading?: boolean
  header?: UniApp.RequestOptions['header']
}

export interface UploadResponse<T = any> {
  code: number
  msg: string
  data: T
}

export function upload<T = any>(options: UploadOptions): Promise<UploadResponse<T>> {
  const {
    url,
    filePath,
    name = 'file',
    formData = {},
    hideLoading = false,
    header = {}
  } = options

  const uploadUrl = webConfig.baseURL + url

  const token = uni.getStorageSync('token')

  const headers: UniApp.RequestOptions['header'] = {
    ...header
  }

  if (token) {
    headers.token = token
  }

  if (!hideLoading) {
    uni.showLoading({ title: '上传中...' })
  }

  return new Promise((resolve, reject) => {	
    uni.uploadFile({
      url: uploadUrl,
      filePath,
      name,
      formData,
      header: headers,

      success: (res) => {
        try {
          const result = JSON.parse(res.data) as UploadResponse<T>

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

          if (result.code !== 0) {
            uni.showToast({
              title: result.msg || '上传失败',
              icon: 'none'
            })
            reject(result)
            return
          }

          resolve(result)
        } catch (e) {
          reject(e)
        }
      },

      fail: (err) => {
        uni.showToast({
          title: '上传失败',
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
