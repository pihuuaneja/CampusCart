import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api, { assetUrl, errorMessage, timeAgo } from '../api.js'
import { useAuth } from '../auth.jsx'

const merge = (list, incoming) => {
  const seen = new Set(list.map((m) => m.id))
  const fresh = incoming.filter((m) => !seen.has(m.id))
  return fresh.length ? [...list, ...fresh] : list
}

function Thread({ id, conv, me, onSent }) {
  const [messages, setMessages] = useState([])
  const [text, setText] = useState('')
  const [error, setError] = useState('')
  const lastId = useRef(0)
  const box = useRef(null)

  const poll = useCallback(async () => {
    try {
      const { data } = await api.get(`/chats/${id}/messages`, { params: { after: lastId.current } })
      if (data.length) {
        lastId.current = data[data.length - 1].id
        setMessages((m) => merge(m, data))
      }
    } catch (e) {
      setError(errorMessage(e))
    }
  }, [id])

  // New messages from the other person arrive within a few seconds.
  useEffect(() => {
    poll()
    const timer = setInterval(poll, 3000)
    return () => clearInterval(timer)
  }, [poll])

  useEffect(() => {
    if (box.current) box.current.scrollTop = box.current.scrollHeight
  }, [messages])

  const send = async (e) => {
    e.preventDefault()
    const body = text.trim()
    if (!body) return
    setText('')
    try {
      const { data } = await api.post(`/chats/${id}/messages`, { body })
      lastId.current = Math.max(lastId.current, data.id)
      setMessages((m) => merge(m, [data]))
      onSent()
    } catch (err) {
      setText(body)
      setError(errorMessage(err))
    }
  }

  return (
    <>
      <header className="thread-head">
        <Link to="/chats" className="back" aria-label="Back to conversations">‹</Link>
        <div>
          <strong>{conv?.otherUserName || 'Conversation'}</strong>
          {conv && <Link to={`/listings/${conv.listingId}`} className="muted">{conv.listingTitle}</Link>}
        </div>
      </header>
      <div className="bubbles" ref={box} aria-live="polite">
        {messages.length === 0 && <p className="muted center">Say hello and ask about the item.</p>}
        {messages.map((m) => (
          <div key={m.id} className={`bubble ${m.senderId === me ? 'mine' : 'theirs'}`}>
            <p>{m.body}</p>
            <time>{timeAgo(m.createdAt)}</time>
          </div>
        ))}
      </div>
      {error && <p className="alert" role="alert">{error}</p>}
      <form className="composer" onSubmit={send}>
        <input value={text} onChange={(e) => setText(e.target.value)} maxLength={1000} placeholder="Write a message" aria-label="Message" />
        <button className="btn btn-primary" disabled={!text.trim()}>Send</button>
      </form>
    </>
  )
}

export default function Chats() {
  const { id } = useParams()
  const { user } = useAuth()
  const [convs, setConvs] = useState(null)

  const loadConvs = useCallback(() => api.get('/chats').then((r) => setConvs(r.data)).catch(() => {}), [])
  useEffect(() => {
    loadConvs()
    const timer = setInterval(loadConvs, 6000)
    return () => clearInterval(timer)
  }, [loadConvs])

  const active = convs?.find((c) => String(c.id) === id)

  return (
    <>
      <h1>Messages</h1>
      <div className={`chat-shell ${id ? 'has-thread' : ''}`}>
        <aside className="chat-list" aria-label="Conversations">
          {convs?.length === 0 && (
            <p className="muted pad">No conversations yet. Open an item and choose <strong>Chat with seller</strong>.</p>
          )}
          {convs?.map((c) => (
            <Link key={c.id} to={`/chats/${c.id}`} className={`conv ${String(c.id) === id ? 'on' : ''}`}>
              <span className="conv-thumb">
                {c.listingImageUrl ? <img src={assetUrl(c.listingImageUrl)} alt="" /> : c.listingTitle[0]}
              </span>
              <span className="conv-text">
                <strong>{c.otherUserName}</strong>
                <small>{c.listingTitle}</small>
                <small className="last">{c.lastMessage || 'No messages yet'}</small>
              </span>
            </Link>
          ))}
        </aside>
        <section className="chat-thread">
          {id ? <Thread key={id} id={id} conv={active} me={user.id} onSent={loadConvs} /> : (
            <div className="empty-thread"><h2>Pick a conversation</h2><p className="muted">Chat with sellers and buyers before you meet.</p></div>
          )}
        </section>
      </div>
    </>
  )
}
