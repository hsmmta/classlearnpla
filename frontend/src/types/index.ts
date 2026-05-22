export interface ApiResult<T = unknown> {
  success: boolean
  data: T
  msg: string
}

export interface SessionUser {
  userID: string
  userName: string
  userType: 'user' | 'admin'
  classID?: string
  gender?: string
  studentID?: string
  userEmail?: string
  points?: number
}
