import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api, { assetUrl, errorMessage, formatPrice, priceLabel } from '../api.js'
import { useAuth } from '../auth.jsx'
import StatusBadge from '../components/StatusBadge.jsx'

export default function ListingDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [listing, setListing] = useState(null)
  const [offer, setOffer] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [sent, setSent] = useState(false)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    api.get(`/listings/${id}`)
      .then((r) => { setListing(r.data); setOffer(String(r.data.price)) })
      .catch((e) => setError(errorMessage(e)))
  }, [id])

  const needLogin = () => navigate('/login', { state: { from: `/listings/${id}` } })

  const sendOffer = async () => {
    if (!user) return needLogin()
    setBusy(true)
    setError('')
    try {
      await api.post('/transactions', { listingId: Number(id), message, offerPrice: Number(offer) })
      setSent(true)
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  const openChat = async () => {
    if (!user) return needLogin()
    try {
      const { data } = await api.post('/chats', { listingId: Number(id) })
      navigate(`/chats/${data.id}`)
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  if (error && !listing) return <p className="alert">{error}</p>
  if (!listing) return <p className="page-note">Loading item…</p>

  const own = user && user.id === listing.sellerId
  const asking = Number(listing.price)
  const amount = Number(offer)
  const isBargain = offer !== '' && amount !== asking
  const verb = listing.type === 'RENT' ? 'rent' : 'buy'

  return (
    <article className="detail">
      <div className="detail-media">
        {listing.imageUrl ? <img src={assetUrl(listing.imageUrl)} alt={listing.title} /> : <span aria-hidden="true">{listing.categoryName[0]}</span>}
      </div>
      <div className="detail-info">
        <div className="tags">
          <span className={`badge type-${listing.type.toLowerCase()}`}>{listing.type === 'RENT' ? 'For rent' : 'For sale'}</span>
          {listing.hostelQuick && <span className="badge quick">Hostel exchange</span>}
          <StatusBadge status={listing.status} />
        </div>
        <h1>{listing.title}</h1>
        <p className="price big">{priceLabel(listing)}</p>
        <p className="description">{listing.description || 'The seller has not added a description.'}</p>
        <dl className="facts">
          <dt>Category</dt><dd>{listing.categoryName}</dd>
          <dt>Seller</dt><dd>{listing.sellerName}{listing.sellerHostel ? `, ${listing.sellerHostel}` : ''}</dd>
          <dt>Posted</dt><dd>{new Date(listing.createdAt).toLocaleDateString('en-IN', { dateStyle: 'medium' })}</dd>
        </dl>

        {own ? (
          <Link className="btn btn-outline" to={`/listings/${listing.id}/edit`}>Edit your listing</Link>
        ) : sent ? (
          <p className="alert success">Offer sent. Follow it under <Link to="/deals">Offers</Link>.</p>
        ) : listing.status === 'AVAILABLE' ? (
          <div className="offer-box">
            <h2>Make an offer</h2>
            <p className="muted">Pay the asking price or bargain. The seller can accept, decline or counter.</p>
            <div className="field">
              <label htmlFor="offer">Your offer (₹)</label>
              <input id="offer" type="number" min="0" step="1" value={offer} onChange={(e) => setOffer(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="msg">Message (optional)</label>
              <textarea id="msg" rows="2" maxLength={300} placeholder="When and where can you meet?"
                        value={message} onChange={(e) => setMessage(e.target.value)} />
            </div>
            {error && <p className="alert" role="alert">{error}</p>}
            <div className="row">
              <button className="btn btn-primary" onClick={sendOffer} disabled={busy || offer === ''}>
                {busy ? 'Sending…' : isBargain ? `Send offer of ${formatPrice(amount)}` : `Request to ${verb} at ${formatPrice(asking)}`}
              </button>
              <button className="btn btn-outline" onClick={openChat}>Chat with seller</button>
            </div>
          </div>
        ) : (
          <>
            <p className="muted">This item is not available right now.</p>
            <button className="btn btn-outline" onClick={openChat}>Chat with seller</button>
          </>
        )}
      </div>
    </article>
  )
}
