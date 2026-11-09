# API reference

Base URL: `http://localhost:8080/api`. Protected endpoints need the header `Authorization: Bearer <token>`.
Errors return JSON: `{ "timestamp", "status", "message" }`.

## Auth
| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/auth/register` | `name, email, password, hostel` | Returns `{ token, user }`. Email must match the college domain. |
| POST | `/auth/login` | `email, password` | Returns `{ token, user }`. |
| GET | `/auth/me` | | Current user. Needs token. |

## Categories
| GET | `/categories` | Public list of categories. |

## Listings
| Method | Path | Access | Notes |
|---|---|---|---|
| GET | `/listings` | Public | Query: `q, categoryId, type (SELL/RENT), hostelQuick, minPrice, maxPrice, sort (newest/price_asc/price_desc)`. Returns available items only. |
| GET | `/listings/{id}` | Public | One listing. |
| GET | `/listings/mine` | Token | Your listings, all statuses. |
| POST | `/listings` | Token | Body: `title, description, price, categoryId, type, rentPeriod (for RENT), hostelQuick, imageUrl`. |
| PUT | `/listings/{id}` | Owner | Same body as create. |
| DELETE | `/listings/{id}` | Owner | Removes the listing and its requests. |

## Transactions
| Method | Path | Notes |
|---|---|---|
| POST | `/transactions` | Body `{ listingId, message, offerPrice }`. `offerPrice` is optional (defaults to the asking price). |
| GET | `/transactions/bought` | Requests you made. |
| GET | `/transactions/received` | Requests for your items. |
| PATCH | `/transactions/{id}/status` | Body `{ action, price }`: `ACCEPT`, `COUNTER` (needs `price`), `REJECT`, `COMPLETE`, `CANCEL`. |

### Bargaining flow
Whoever did not make the latest offer has the turn.
```
REQUESTED (buyer's offer) --ACCEPT (seller)--> ACCEPTED
REQUESTED                 --COUNTER (seller)--> COUNTERED
COUNTERED (seller's offer)--ACCEPT (buyer)--> ACCEPTED
COUNTERED                 --COUNTER (buyer)--> REQUESTED
REQUESTED / COUNTERED     --REJECT (seller)--> REJECTED
REQUESTED / COUNTERED     --CANCEL (buyer)--> CANCELLED
ACCEPTED                  --COMPLETE (seller)--> COMPLETED
ACCEPTED                  --CANCEL (either)--> CANCELLED
```
Accepting reserves the listing and closes other open offers. Completing a sale marks it sold. Completing a rental makes it available again.

## Photo upload
| POST | `/uploads` | Token. `multipart/form-data` with field `file` (JPG, PNG or WebP, 5 MB max). Returns `{ "url": "/uploads/<id>.jpg" }`. Pass that url as `imageUrl` when creating a listing. |
| GET | `/uploads/{file}` | Public. Serves the photo. |

## Chat
| Method | Path | Notes |
|---|---|---|
| POST | `/chats` | Body `{ listingId }`. Buyer opens or reopens the conversation. |
| GET | `/chats` | Your conversations (as buyer or seller), newest first. |
| GET | `/chats/{id}/messages?after=<lastId>` | Messages, only newer than `after`. Participants only. |
| POST | `/chats/{id}/messages` | Body `{ body }` (max 1000 characters). |
