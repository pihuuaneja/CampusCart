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
    setError('')
    const controller = new AbortController()
    const timer = setTimeout(() => {
      api.get('/listings', { params, signal: controller.signal })
        .then((r) => { if (!controller.signal.aborted) setListings(r.data) })
        .catch((e) => { if (!controller.signal.aborted) setError(errorMessage(e)) })
        .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    }, 250)
    return () => {
      clearTimeout(timer)
      controller.abort()
    }
  }, [filters])

  const set = (key) => (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setFilters((f) => ({ ...f, [key]: value }))
  }

  return (
    <>
      <section className="hero" aria-labelledby="marketplace-title">
        <div className="hero-copy">
          <p className="market-label">Thapar student marketplace</p>
          <h1 id="marketplace-title">Good finds.<br />Right on campus.</h1>
          <p>Buy, sell or rent textbooks, cycles and hostel essentials from fellow students.</p>
          <label className="search-box">
            <span aria-hidden="true" className="search-icon" />
            <input className="search" type="search" aria-label="Search listings"
              placeholder="Try textbooks, a cycle or a kettle" value={filters.q} onChange={set('q')} />
          </label>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="orbit orbit-outer" />
          <div className="orbit orbit-inner" />
          <span className="hero-monogram">C</span>
          <span className="hero-spark">✦</span>
        </div>
      </section>

      {!user && (
        <p className="signup-note">
          <Link to="/register">Create a free account</Link> with your Thapar email to chat, bargain and post items.
        </p>
      )}

      <h2 className="browse-title">Browse listings</h2>
      <div className="chips" role="group" aria-label="Categories">
        <button className={`chip ${filters.categoryId === '' ? 'on' : ''}`} aria-pressed={filters.categoryId === ''} onClick={() => setFilters((f) => ({ ...f, categoryId: '' }))}>All</button>
        {categories.map((c) => (
          <button key={c.id} className={`chip ${String(filters.categoryId) === String(c.id) ? 'on' : ''}`}
                  aria-pressed={String(filters.categoryId) === String(c.id)}
                  onClick={() => setFilters((f) => ({ ...f, categoryId: c.id }))}>{c.name}</button>
        ))}
      </div>

      <div className="layout">
        <aside className="filters" aria-labelledby="filter-title">
          <h3 id="filter-title">Filters</h3>
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

        <section className="results" aria-label="Listing results">
          <p className="results-count" role="status">
            {loading ? 'Searching…' : !error && `${listings.length} ${listings.length === 1 ? 'listing' : 'listings'}`}
          </p>
          {error && <p className="alert" role="alert">{error}</p>}
          {!error && !loading && listings.length === 0 && (
            <div className="empty">
              <h2>No items match yet</h2>
              <p>Try a different search or clear your filters.</p>
              <button type="button" className="btn btn-outline" onClick={() => setFilters(EMPTY)}>Clear filters</button>
            </div>
          )}
          <div className="grid">
            {!error && listings.map((l) => <ListingCard key={l.id} listing={l} />)}
          </div>
        </section>
      </div>
    </>
  )
}
