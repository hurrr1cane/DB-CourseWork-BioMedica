import { RouterProvider, createBrowserRouter } from 'react-router-dom';
import HomePage from './pages/HomePage';
import OrderPage from './pages/OrderPage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import LaboratoriesPage from './pages/LaboratoriesPage';
import LaboratoryPage from './pages/LaboratoryPage';
import AssistantsPage from './pages/AssistantsPage';
import AssistantDetailPage from './pages/AssistantDetailPage';

const router = createBrowserRouter(
  [
    { path: '/', element: <HomePage /> },
    { path: '/make-order', element: <OrderPage /> },
    { path: '/login', element: <LoginPage /> },
    { path: '/profile', element: <ProfilePage /> },
    { path: '/laboratories', element: <LaboratoriesPage /> },
    { path: '/laboratories/edit/:id', element: <LaboratoryPage /> },
    { path: '/assistants', element: <AssistantsPage /> },
    { path: '/assistants/:id', element: <AssistantDetailPage /> },
  ]
)

function App() {
  return (
    <RouterProvider router={router} />
  );
}

export default App;
