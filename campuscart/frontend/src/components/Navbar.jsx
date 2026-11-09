import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <header className="nav">
      <div className="nav-inner">
        <Link to="/" className="brand" aria-label="CampusCart home">
          <span className="brand-mark" aria-hidden="true">C</span>
          <span>CampusCart<small>By Thapar students, for Thapar students</small></span>
        </Link>
        <nav className="nav-links" aria-label="Main">
          <NavLink to="/" end>Browse</NavLink>
          {user && <NavLink to="/chats">Messages</NavLink>}
          {user && <NavLink to="/deals">Offers</NavLink>}
          {user && <NavLink to="/my-listings">My listings</NavLink>}
        </nav>
        <div className="nav-actions">
          {user ? (
            <>
              <Link to="/sell" className="btn btn-accent">Post an item</Link>
              <button className="btn btn-ghost-light" onClick={() => { logout(); navigate('/') }}>Log out</button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost-light">Log in</Link>
              <Link to="/register" className="btn btn-accent">Create account</Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
