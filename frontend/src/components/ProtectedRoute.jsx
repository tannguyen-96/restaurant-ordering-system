import { Navigate, Outlet, useLocation } from 'react-router-dom'
import useAuth from '../hooks/useAuth'

const ProtectedRoute = ({ roles }) => {
  const { auth } = useAuth()
  console.log(auth)

  const location = useLocation()

  return (
    roles.includes(auth?.role) 
      ? <Outlet /> 
      : auth?.user
        ? <Navigate to="/unauthorized" state={{ from: location }} replace />
        : <Navigate to="/login" state={{ from: location }} replace />
  )
}

export default ProtectedRoute;