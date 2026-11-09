import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api, { assetUrl, errorMessage } from '../api.js'

const BLANK = {
  title: '', description: '', price: '', categoryId: '', type: 'SELL',
  rentPeriod: 'WEEK', hostelQuick: false, imageUrl: '',
}
const MAX_BYTES = 5 * 1024 * 1024

export default function ListingForm() {
  const { id } = useParams()
  const editing = Boolean(id)
  const navigate = useNavigate()
  const [form, setForm] = useState(BLANK)
  const [categories, setCategories] = useState([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [uploading, setUploading] = useState(false)

  useEffect(() => {
    api.get('/categories').then((r) => setCategories(r.data))
    if (editing) {
      api.get(`/listings/${id}`).then(({ data }) => setForm({
        title: data.title, description: data.description || '', price: data.price,
        categoryId: data.categoryId, type: data.type, rentPeriod: data.rentPeriod || 'WEEK',
        hostelQuick: data.hostelQuick, imageUrl: data.imageUrl || '',
      })).catch((e) => setError(errorMessage(e)))
    }
  }, [id, editing])

  const bind = (key) => ({ value: form[key], onChange: (e) => setForm({ ...form, [key]: e.target.value }) })

  const upload = async (file) => {
    if (!file) return
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      setError('Choose a JPG, PNG or WebP photo.')
      return
    }
    if (file.size > MAX_BYTES) {
      setError('That photo is over 5 MB. Choose a smaller one.')
      return
    }
    setError('')
    setUploading(true)
    try {
      const body = new FormData()
      body.append('file', file)
      const { data } = await api.post('/uploads', body)
      setForm((f) => ({ ...f, imageUrl: data.url }))
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setUploading(false)
    }
  }

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    const payload = {
      ...form,
      price: Number(form.price),
      categoryId: Number(form.categoryId),
      rentPeriod: form.type === 'RENT' ? form.rentPeriod : null,
    }
    try {
      const { data } = editing ? await api.put(`/listings/${id}`, payload) : await api.post('/listings', payload)
      navigate(`/listings/${data.id}`)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-panel wide">
      <h1>{editing ? 'Edit your listing' : 'Post an item'}</h1>
      {error && <p className="alert" role="alert">{error}</p>}
      <form onSubmit={submit} className="form">
        <div className="field">
          <span className="label">Photo</span>
          {form.imageUrl ? (
            <div className="photo-preview">
              <img src={assetUrl(form.imageUrl)} alt="Selected item" />
              <button type="button" className="btn btn-outline btn-small" onClick={() => setForm({ ...form, imageUrl: '' })}>Remove photo</button>
            </div>
          ) : (
            <label className="dropzone">
              <input type="file" accept="image/jpeg,image/png,image/webp" onChange={(e) => upload(e.target.files[0])} />
              <strong>{uploading ? 'Uploading…' : 'Choose a photo from your device'}</strong>
              <small>JPG, PNG or WebP, up to 5 MB. Items with photos get noticed first.</small>
            </label>
          )}
        </div>

        <div className="field">
          <label htmlFor="title">Title</label>
          <input id="title" required maxLength={120} placeholder="Hero cycle, 21 gears, lightly used" {...bind('title')} />
        </div>

        <div className="two">
          <div className="field">
            <label htmlFor="type">I want to</label>
            <select id="type" {...bind('type')}>
              <option value="SELL">Sell this item</option>
              <option value="RENT">Rent this item out</option>
            </select>
          </div>
          <div className="field">
            <label htmlFor="category">Category</label>
            <select id="category" required {...bind('categoryId')}>
              <option value="">Choose a category</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
        </div>

        <div className="two">
          <div className="field">
            <label htmlFor="price">{form.type === 'RENT' ? 'Rent (₹)' : 'Asking price (₹)'}</label>
            <input id="price" type="number" min="0" step="1" required {...bind('price')} />
          </div>
          {form.type === 'RENT' && (
            <div className="field">
              <label htmlFor="period">Rent is charged per</label>
              <select id="period" {...bind('rentPeriod')}>
                <option value="DAY">Day</option>
                <option value="WEEK">Week</option>
                <option value="MONTH">Month</option>
              </select>
            </div>
          )}
        </div>

        <div className="field">
          <label htmlFor="description">Description</label>
          <textarea id="description" rows="5" maxLength={2000}
                    placeholder="Condition, age, what is included, any flaws" {...bind('description')} />
        </div>

        <label className="quick-toggle">
          <input type="checkbox" checked={form.hostelQuick}
                 onChange={(e) => setForm({ ...form, hostelQuick: e.target.checked })} />
          <span>
            <strong>Available for hostel exchange</strong>
            <small>Show this item to students who need it handed over quickly in the hostel</small>
          </span>
        </label>

        <button className="btn btn-primary" disabled={busy || uploading}>
          {busy ? 'Saving…' : editing ? 'Save changes' : 'Post item'}
        </button>
      </form>
    </div>
  )
}
