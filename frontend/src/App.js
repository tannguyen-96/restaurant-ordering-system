import './App.css';
import Layout from './components/Layout';
import Home from './pages/Home';
import RestaurantTable from './pages/RestaurantTable';
import Branch from './pages/Branch';
import Login from './pages/Login';
import Unauthorized from './pages/Unauthorized';
import ProtectedRoute from './components/ProtectedRoute';
import Menu from './pages/Menu';
import Order from './pages/Order';
import { Routes, Route } from 'react-router-dom';

const ROLES = {
  'Admin': 'admin',
  'Guest': 'guest'
}

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        {/* public routes */}
        <Route path="/" element={<Home />} />
        <Route path="login" element={<Login />} />
        <Route path="unauthorized" element={<Unauthorized />} />

        {/* protected routes */}
        <Route element={<ProtectedRoute role={ROLES.Admin} />}>
          <Route path="branch" element={<Branch />} />
          <Route path="restaurant-table" element={<RestaurantTable />} />
        </Route>
        <Route element={<ProtectedRoute role={ROLES.Guest} />}>
          <Route path="menu" element={<Menu />} />
          <Route path="order" element={<Order />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;
