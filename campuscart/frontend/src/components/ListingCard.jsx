import { Link } from 'react-router-dom'
import { assetUrl, priceLabel } from '../api.js'

export default function ListingCard({ listing, footer }) {
  const l = listing
  return (
    <article className="card">
      <Link to={`/listings/${l.id}`} className="card-link">
        <div className="card-media">
          {l.imageUrl ? <img src={assetUrl(l.imageUrl)} alt="" loading="lazy" /> : <span aria-hidden="true">{l.categoryName[0]}</span>}
          {l.hostelQuick && <span className="badge quick overlay">Hostel exchange</span>}
        </div>
        <div className="card-body">
          <p className="meta">{l.categoryName} · {l.type === 'RENT' ? 'For rent' : 'For sale'}</p>
          <h3>{l.title}</h3>
          <p className="price">{priceLabel(l)}</p>
          <p className="meta">{l.sellerName}{l.sellerHostel ? `, ${l.sellerHostel}` : ''}</p>
        </div>
      </Link>
      {footer && <div className="card-footer">{footer}</div>}
    </article>
  )
}
