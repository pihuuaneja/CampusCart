import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../api.js'
import { useAuth } from '../auth.jsx'
import ListingCard from '../components/ListingCard.jsx'

const EMPTY = { q: '', categoryId: '', type: '', hostelQuick: false, minPrice: '', maxPrice: '', sort: 'newest' }

export default function Home() {
  const { user } = useAuth()
  const [filters, setFilters] = useState(EMPTY)
  const [categories, setCategories] = useState([])
  const [listings, setListings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/categories').then((r) => setCategories(r.data)).catch(() => {})
  }, [])

  // Re-run the search shortly after the filters stop changing.
  useEffect(() => {
    const params = {}
    Object.entries(filters).forEach(([k, v]) => {
      if (v !== '' && v !== false) params[k] = v
    })
    setLoading(true)
    const timer = setTimeout(() => {
      api.get('/listings', { params })
        .then((r) => { setListings(r.data); setError('') })
        .catch((e) => setError(errorMessage(e)))
        .finally(() => setLoading(false))
    }, 250)
    return () => clearTimeout(timer)
  }, [filters])

  const set = (key) => (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setFilters((f) => ({ ...f, [key]: value }))
  }

  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <span className="badge verified">Only for @thapar.edu students</span>
          <h1>Your campus already has what you need.</h1>
          <p>Buy, sell and rent textbooks, cycles, electronics and hostel essentials. Chat with the seller and bargain for a better price.</p>
          <input
            className="search"
            type="search"
            placeholder="Search for a cycle, a textbook, a kettle"
            aria-label="Search listings"
            value={filters.q}
            onChange={set('q')}
          />
        </div>
        <ol className="steps" aria-label="How CampusCart works">
          <li><strong>Post or find</strong><span>List what you don&apos;t need, or search what you do.</span></li>
          <li><strong>Chat and bargain</strong><span>Message the seller and send your own offer.</span></li>
          <li><strong>Meet on campus</strong><span>Hand over in the hostel, even late at night.</span></li>
        </ol>
      </section>

      {!user && (
        <p className="signup-note">
          <Link to="/register">Create a free account</Link> with your Thapar email to chat, bargain and post items.
        </p>
      )}

      <div className="chips" role="group" aria-label="Categories">
        <button className={`chip ${filters.categoryId === '' ? 'on' : ''}`} onClick={() => setFilters((f) => ({ ...f, categoryId: '' }))}>All</button>
        {categories.map((c) => (
          <button key={c.id} className={`chip ${String(filters.categoryId) === String(c.id) ? 'on' : ''}`}
                  onClick={() => setFilters((f) => ({ ...f, categoryId: c.id }))}>{c.name}</button>
        ))}
      </div>

      <div className="layout">
        <aside className="filters" aria-label="Filters">
          <label className="quick-toggle">
            <input type="checkbox" checked={filters.hostelQuick} onChange={set('hostelQuick')} />
            <span>
              <strong>Hostel exchange</strong>
              <small>Items a seller can hand over in the hostel, even at night</small>
            </span>
          </label>

          <div className="field">
            <label htmlFor="type">Buy or rent</label>
            <select id="type" value={filters.type} onChange={set('type')}>
              <option value="">Buy and rent</option>
              <option value="SELL">For sale</option>
              <option value="RENT">For rent</option>
            </select>
          </div>

          <div className="field">
            <label htmlFor="minPrice">Price range (₹)</label>
            <div className="range">
              <input id="minPrice" type="number" min="0" placeholder="Min" value={filters.minPrice} onChange={set('minPrice')} />
              <input type="number" min="0" placeholder="Max" aria-label="Maximum price" value={filters.maxPrice} onChange={set('maxPrice')} />
            </div>
          </div>

          <div className="field">
            <label htmlFor="sort">Sort by</label>
            <select id="sort" value={filters.sort} onChange={set('sort')}>
              <option value="newest">Newest first</option>
              <option value="price_asc">Price, low to high</option>
              <option value="price_desc">Price, high to low</option>
            </select>
          </div>

          <button className="btn btn-outline" onClick={() => setFilters(EMPTY)}>Clear filters</button>
        </aside>

        <section aria-live="polite">
          {error && <p className="alert">{error}</p>}
          {!error && loading && <p className="page-note">Searching…</p>}
          {!error && !loading && listings.length === 0 && (
            <div className="empty">
              <h2>No items match yet</h2>
              <p>Try removing a filter, or post what you have so others can find it.</p>
            </div>
          )}
          <div className="grid">
            {listings.map((l) => <ListingCard key={l.id} listing={l} />)}
          </div>
        </section>
      </div>
    </>
  )
}
