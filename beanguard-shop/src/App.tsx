import { BrowserRouter, Route, Routes } from 'react-router-dom'
import AppHeader from './components/AppHeader'
import { ConfigProvider } from './ConfigProvider'
import ConfirmationPage from './pages/ConfirmationPage'
import ShopPage from './pages/ShopPage'
import TransferConfirmPage from './pages/TransferConfirmPage'

export default function App() {
  return (
    <ConfigProvider>
      <BrowserRouter>
        <div className="flex min-h-screen flex-col bg-zinc-50">
          <AppHeader />
          <Routes>
            <Route path="/" element={<ShopPage />} />
            <Route path="/confirmation" element={<ConfirmationPage />} />
            <Route path="/transfer-confirm" element={<TransferConfirmPage />} />
          </Routes>
        </div>
      </BrowserRouter>
    </ConfigProvider>
  )
}
