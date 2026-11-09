import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api, { errorMessage, formatPrice, timeAgo } from '../api.js'
import StatusBadge from '../components/StatusBadge.jsx'

function DealRow({ deal, role, onAction }) {
  const navigate = useNavigate()
  const seller = role === 'seller'
  const [countering, setCountering] = useState(false)
  const [price, setPrice] = useState(String(deal.offerPrice))

  // The person who did not make the latest offer has the turn.
  const myTurn = (seller && deal.status === 'REQUESTED') || (!seller && deal.status === 'COUNTERED')
  const waiting = (seller && deal.status === 'COUNTERED') || (!seller && deal.status === 'REQUESTED')

  const messageSeller = async () => {
    const { data } = await api.post('/chats', { listingId: deal.listingId })
    navigate(`/chats/${data.id}`)
  }

  const counter = () => { onAction(deal.id, 'COUNTER', Number(price)); setCountering(false) }
  const who = seller ? deal.buyerName : deal.sellerName
  const latestBy = deal.status === 'COUNTERED' ? deal.sellerName : deal.buyerName

  return (
    <li className="deal">
      <div className="deal-main">
        <Link to={`/listings/${deal.listingId}`} className="deal-title">{deal.listingTitle}</Link>
        <p className="muted">{seller ? `Buyer: ${who}` : `Seller: ${who}`} · {timeAgo(deal.updatedAt)}</p>
        <p className="prices">
          <span>Asking <strong>{formatPrice(deal.price)}</strong></span>
          <span className="offer">
            {deal.status === 'ACCEPTED' || deal.status === 'COMPLETED' ? 'Agreed price' : `Latest offer from ${latestBy}`}{' '}
            <strong>{formatPrice(deal.offerPrice)}</strong>
          </span>
        </p>
        {deal.message && <p className="quote">{deal.message}</p>}
        {myTurn && <p className="turn">Your turn to respond.</p>}
        {waiting && <p className="muted">Waiting for {who} to respond.</p>}

        {countering && (
          <div className="counter-box">
            <label htmlFor={`c${deal.id}`}>Your counter offer (₹)</label>
            <div className="row">
              <input id={`c${deal.id}`} type="number" min="0" value={price} onChange={(e) => setPrice(e.target.value)} />
              <button className="btn btn-primary btn-small" onClick={counter} disabled={price === ''}>Send counter</button>
              <button className="btn btn-outline btn-small" onClick={() => setCountering(false)}>Close</button>
            </div>
          </div>
        )}
      </div>
      <div className="deal-side">
        <StatusBadge status={deal.status} />
        <div className="row end">
          {myTurn && <button className="btn btn-small btn-primary" onClick={() => onAction(deal.id, 'ACCEPT')}>Accept {formatPrice(deal.offerPrice)}</button>}
          {myTurn && !countering && <button className="btn btn-small btn-outline" onClick={() => setCountering(true)}>Counter</button>}
          {seller && ['REQUESTED', 'COUNTERED'].includes(deal.status) && (
            <button className="btn btn-small btn-outline" onClick={() => onAction(deal.id, 'REJECT')}>{deal.status === 'COUNTERED' ? 'Withdraw' : 'Decline'}</button>
          )}
          {!seller && ['REQUESTED', 'COUNTERED'].includes(deal.status) && (
            <button className="btn btn-small btn-outline" onClick={() => onAction(deal.id, 'CANCEL')}>Cancel</button>
          )}
          {seller && deal.status === 'ACCEPTED' && <button className="btn btn-small btn-primary" onClick={() => onAction(deal.id, 'COMPLETE')}>Mark as completed</button>}
          {deal.status === 'ACCEPTED' && <button className="btn btn-small btn-outline" onClick={() => onAction(deal.id, 'CANCEL')}>Cancel deal</button>}
          {!seller && <button className="btn btn-small btn-outline" onClick={messageSeller}>Message seller</button>}
        </div>
      </div>
    </li>
  )
}

export default function Transactions() {
  const [tab, setTab] = useState('received')
  const [data, setData] = useState({ received: null, bought: null })
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    try {
      const [received, bought] = await Promise.all([api.get('/transactions/received'), api.get('/transactions/bought')])
      setData({ received: received.data, bought: bought.data })
    } catch (e) {
      setError(errorMessage(e))
    }
  }, [])
  useEffect(() => { load() }, [load])

  const act = async (id, action, price) => {
    setError('')
    try {
      await api.patch(`/transactions/${id}/status`, { action, price })
      await load()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  const list = data[tab]
  return (
    <>
      <h1>Offers</h1>
      <div className="tabs" role="tablist">
        <button role="tab" aria-selected={tab === 'received'} onClick={() => setTab('received')}>Offers on my items</button>
        <button role="tab" aria-selected={tab === 'bought'} onClick={() => setTab('bought')}>Offers I made</button>
      </div>
      {error && <p className="alert" role="alert">{error}</p>}
      {!list && !error && <p className="page-note">Loading offers…</p>}
      {list?.length === 0 && (
        <div className="empty">
          <h2>{tab === 'received' ? 'No offers yet' : 'You have not made an offer'}</h2>
          <p>{tab === 'received' ? 'When someone offers a price for one of your items, it shows up here.' : 'Open an item on the marketplace and send an offer.'}</p>
        </div>
      )}
      <ul className="deals">
        {list?.map((d) => <DealRow key={d.id} deal={d} role={tab === 'received' ? 'seller' : 'buyer'} onAction={act} />)}
      </ul>
    </>
  )
}
