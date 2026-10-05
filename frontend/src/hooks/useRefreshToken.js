import axios from 'axios'
import useAuth from './useAuth'

const useRefreshToken = () => {
  const { setAuth } = useAuth()

  const refresh = async () => {
    const response = await axios.post('/refresh', { withCredentials: true })
    setAuth(prev => {
      console.log('Previous auth state:', prev)
      console.log('New access token:', response.data.accessToken)
      return { ...prev, accessToken: response.data.accessToken }
    })
    return response.data.accessToken
  }
  return refresh
};

export default useRefreshToken;
