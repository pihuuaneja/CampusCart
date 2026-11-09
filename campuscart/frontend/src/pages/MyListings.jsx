import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../api.js'
import ListingCard from '../components/ListingCard.jsx'
import StatusBadge from '../components/StatusBadge.jsx'

export default function MyListings() {
  const [listings, setListings] = useState(null)
  const [error, setError] = useState('')

  const load = () => api.get('/listings/mine').then((r) => setListings(r.data)).catch((e) => setError(errorMessage(e)))
  useEffect(() => { load() }, [])

  const remove = async (l) => {
    if (!window.confirm(`Delete "${l.title}"? This also removes its requests.`)) return
    try {
      await api.delete(`/listings/${l.id}`)
      load()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  if (!listings && !error) return <p className="page-note">Loading your listings…</p>

  return (
    <>
      <div className="page-head">
        <h1>My listings</h1>
        <Link to="/sell" className="btn btn-primary">Post an item</Link>
      </div>
      {error && <p className="alert">{error}</p>}
      {listings?.length === 0 && (
        <div className="empty">
          <h2>You have not posted anything yet</h2>
          <p>Old textbooks, a spare cycle or a kettle you no longer use can help another student today.</p>
        </div>
      )}
      <div className="grid">
        {listings?.map((l) => (
          <ListingCard key={l.id} listing={l} footer={
            <div className="row">
              <StatusBadge status={l.status} />
              <span className="spacer" />
              <Link className="link-btn" to={`/listings/${l.id}/edit`}>Edit</Link>
              <button className="link-btn danger" onClick={() => remove(l)}>Delete</button>
            </div>
          } />
        ))}
      </div>
    </>
  )
}
