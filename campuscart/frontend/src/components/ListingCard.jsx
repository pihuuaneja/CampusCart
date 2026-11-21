import { Link } from 'react-router-dom'
import { assetUrl, priceLabel } from '../api.js'

export default function ListingCard({ listing, footer }) {
  const l = listing
  return (
    <article className="card">
      <Link to={`/listings/${l.id}`} className="card-link">
        <div className="card-media">
          {l.imageUrl ? <img src={assetUrl(l.imageUrl)} alt="" loading="lazy" /> : <span aria-hidden="true">{l.categoryName?.charAt(0) || "C"}</span>}
          {l.hostelQuick && <span className="badge quick overlay">Hostel exchange</span>}
        </div>
        <div className="card-body">
          <div className="card-eyebrow">
            <span className="meta">{l.categoryName}</span>
            <span className={l.type === 'RENT' ? 'badge type-rent' : 'badge type-sell'}>
              {l.type === 'RENT' ? 'For rent' : 'For sale'}
            </span>
          </div>
          <h3>{l.title}</h3>
          <p className="price">{priceLabel(l)}</p>
          <p className="meta">{l.sellerName}{l.sellerHostel ? `, ${l.sellerHostel}` : ''}</p>
        </div>
      </Link>
      {footer && <div className="card-footer">{footer}</div>}
    </article>
  )
}
