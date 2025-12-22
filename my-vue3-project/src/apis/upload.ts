import { upload } from '@/utils/upload'

/**
 * 上传头像
 */
export const apiUploadAvatar = (filePath: string) => {
  return upload<string>({
    url: '/file/upload/avatar',
    filePath,
    name: 'file'
  })
}