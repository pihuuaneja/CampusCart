import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import { useAuth } from './auth.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import ListingDetail from './pages/ListingDetail.jsx'
import ListingForm from './pages/ListingForm.jsx'
import MyListings from './pages/MyListings.jsx'
import Transactions from './pages/Transactions.jsx'
import Chats from './pages/Chats.jsx'

function Protected({ children }) {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <p className="page-note">Loading your account…</p>
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return children
}

export default function App() {
  return (
    <>
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/listings/:id" element={<ListingDetail />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/sell" element={<Protected><ListingForm /></Protected>} />
          <Route path="/listings/:id/edit" element={<Protected><ListingForm /></Protected>} />
          <Route path="/my-listings" element={<Protected><MyListings /></Protected>} />
          <Route path="/deals" element={<Protected><Transactions /></Protected>} />
          <Route path="/chats" element={<Protected><Chats /></Protected>} />
          <Route path="/chats/:id" element={<Protected><Chats /></Protected>} />
          <Route path="*" element={<p className="page-note">That page does not exist. <a href="/">Back to the marketplace</a></p>} />
        </Routes>
      </main>
      <footer className="footer">CampusCart is for Thapar students only. Meet in public campus spaces and check items before you pay.</footer>
    </>
  )
}
