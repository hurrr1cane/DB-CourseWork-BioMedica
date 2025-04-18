import { RouterProvider, createBrowserRouter } from 'react-router-dom';
import HomePage from './pages/HomePage';
import OrderPage from './pages/OrderPage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';

const router = createBrowserRouter(
  [
    { path: '/', element: <HomePage /> },
    { path: '/make-order', element: <OrderPage /> },
    { path: '/login', element: <LoginPage /> },
    { path: '/profile', element: <ProfilePage /> }
  ]
)

function App() {
  return (
    <RouterProvider router={router} />
  );
}

export default App;
