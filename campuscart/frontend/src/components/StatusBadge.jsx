const LABELS = {
  REQUESTED: 'Offer sent', COUNTERED: 'Counter offer', ACCEPTED: 'Accepted', REJECTED: 'Declined',
  COMPLETED: 'Completed', CANCELLED: 'Cancelled',
  AVAILABLE: 'Available', RESERVED: 'Reserved', SOLD: 'Sold',
}

export default function StatusBadge({ status }) {
  return <span className={`badge status-${status.toLowerCase()}`}>{LABELS[status] || status}</span>
}
