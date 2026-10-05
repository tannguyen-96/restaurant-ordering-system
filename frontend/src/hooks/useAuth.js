import { useContext, useDebugValue } from "react";
import AuthProvider from "../context/AuthContext";

const useAuth = () => {
    const { auth } = useContext(AuthProvider);
    console.log(auth);
    useDebugValue(auth, auth => auth?.user ? "Logged In" : "Logged Out")
    return useContext(AuthProvider);
}

export default useAuth;