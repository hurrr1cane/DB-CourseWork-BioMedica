import { RouterProvider, createBrowserRouter } from 'react-router-dom';
import HomePage from './pages/HomePage';
import OrderPage from './pages/OrderPage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import LaboratoriesPage from './pages/LaboratoriesPage';
import LaboratoryPage from './pages/LaboratoryPage';
import AssistantsPage from './pages/AssistantsPage';
import AssistantDetailPage from './pages/AssistantDetailPage';
import TestsPage from './pages/TestsPage';
import TestDetails from './pages/TestDetailsPage';
import TestsManagementPage from './pages/TestsManagementPage';
import TestDetailPage from './pages/TestDetailPage';

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
    { path: '/assistant-tests', element: <TestsPage /> },
    { path: '/assistant-tests/:id', element: <TestDetails /> },
    { path: '/tests', element: <TestsManagementPage /> },
    { path: '/tests/:id', element: <TestDetailPage /> },
  ]
)

function App() {
  return (
    <RouterProvider router={router} />
  );
}

export default App;