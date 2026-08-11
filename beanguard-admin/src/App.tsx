import { HashRouter, Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import ProtectedRoute from './components/auth/ProtectedRoute'
import MainLayout from './components/layout/MainLayout'
import UpdatePrompt from './components/UpdatePrompt'
import { ConfigProvider } from './context/ConfigProvider'
import BrandingPage from './pages/branding/BrandingPage'
import EditLicencePage from './pages/licences/EditLicencePage'
import LegalSettingsPage from './pages/legal/LegalSettingsPage'
import LicenceKeysPage from './pages/licence-keys/LicenceKeysPage'
import LicencesPage from './pages/licences/LicencesPage'
import Login from './pages/Login'
import MailTemplatesPage from './pages/mail-templates/MailTemplatesPage'
import OrdersPage from './pages/orders/OrdersPage'
import ParametersPage from './pages/parameters/ParametersPage'
import PdfTemplatesPage from './pages/pdf-templates/PdfTemplatesPage'
import ProductsPage from './pages/products/ProductsPage'
import UsersPage from './pages/users/UsersPage'

export default function App() {
  return (
    <ConfigProvider>
      <UpdatePrompt />
      <HashRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/licences" replace />} />
            <Route path="licences" element={<LicencesPage />} />
            <Route path="licences/:key/edit" element={<EditLicencePage />} />
            <Route path="users" element={<UsersPage />} />
            <Route path="licence-keys" element={<LicenceKeysPage />} />
            <Route path="parameters" element={<ParametersPage />} />
            <Route path="products" element={<ProductsPage />} />
            <Route path="orders" element={<OrdersPage />} />
            <Route path="mail-templates" element={<MailTemplatesPage />} />
            <Route path="pdf-templates" element={<PdfTemplatesPage />} />
            <Route path="branding" element={<BrandingPage />} />
            <Route path="legal" element={<LegalSettingsPage />} />
          </Route>
        </Routes>
      </HashRouter>
    </ConfigProvider>
  )
}
