package com.campuscart.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuscart.exception.ApiException;
import com.campuscart.model.ListingView;
import com.campuscart.model.TransactionView;
import com.campuscart.repository.ListingRepository;
import com.campuscart.repository.TransactionRepository;

/**
 * Deal life cycle with bargaining. Whoever did NOT make the latest offer has the turn.
 *
 *   REQUESTED  (buyer's offer, seller's turn)  --ACCEPT(seller)--> ACCEPTED
 *                                              --COUNTER(seller)--> COUNTERED
 *   COUNTERED  (seller's offer, buyer's turn)  --ACCEPT(buyer)--> ACCEPTED
 *                                              --COUNTER(buyer)--> REQUESTED
 *   REQUESTED / COUNTERED --REJECT(seller)--> REJECTED
 *   REQUESTED / COUNTERED --CANCEL(buyer)--> CANCELLED
 *   ACCEPTED --COMPLETE(seller)--> COMPLETED,  ACCEPTED --CANCEL(either)--> CANCELLED
 */
@Service
public class TransactionService {
    private final TransactionRepository txs;
    private final ListingRepository listings;

    public TransactionService(TransactionRepository txs, ListingRepository listings) {
        this.txs = txs;
        this.listings = listings;
    }

    @Transactional
    public TransactionView request(long buyerId, long listingId, String message, BigDecimal offer) {
        ListingView l = listings.findById(listingId).orElseThrow(() -> ApiException.notFound("Listing"));
        if (l.sellerId() == buyerId) throw ApiException.badRequest("You cannot request your own listing");
        if (!"AVAILABLE".equals(l.status())) throw ApiException.conflict("This item is no longer available");
        if (txs.hasOpenRequest(listingId, buyerId)) throw ApiException.conflict("You already have an open request for this item");
        BigDecimal price = offer != null ? offer : l.price();
        long id = txs.create(listingId, buyerId, message, price);
        return txs.findById(id).orElseThrow();
    }

    public List<TransactionView> bought(long userId) { return txs.findByBuyer(userId); }

    public List<TransactionView> received(long userId) { return txs.findBySeller(userId); }

    @Transactional
    public TransactionView updateStatus(long userId, long id, String action, BigDecimal price) {
        TransactionView t = txs.findById(id).orElseThrow(() -> ApiException.notFound("Transaction"));
        boolean seller = t.sellerId() == userId;
        boolean buyer = t.buyerId() == userId;
        if (!seller && !buyer) throw ApiException.forbidden("This transaction does not involve you");
        String status = t.status();

        switch (action.trim().toUpperCase()) {
            case "ACCEPT" -> {
                if ("REQUESTED".equals(status)) requireRole(seller, "The seller needs to respond to this offer");
                else if ("COUNTERED".equals(status)) requireRole(buyer, "The buyer needs to respond to this counter offer");
                else throw notChangeable(status);
                txs.updateStatus(id, "ACCEPTED");
                listings.updateStatus(t.listingId(), "RESERVED");
                txs.rejectOtherRequests(t.listingId(), id);
            }
            case "COUNTER" -> {
                if (price == null) throw ApiException.badRequest("Enter the price you want to offer");
                if ("REQUESTED".equals(status)) {
                    requireRole(seller, "The seller needs to respond to this offer");
                    txs.updateOffer(id, "COUNTERED", price);
                } else if ("COUNTERED".equals(status)) {
                    requireRole(buyer, "The buyer needs to respond to this counter offer");
                    txs.updateOffer(id, "REQUESTED", price);
                } else {
                    throw notChangeable(status);
                }
            }
            case "REJECT" -> {
                requireRole(seller, "Only the seller can decline an offer");
                if (!"REQUESTED".equals(status) && !"COUNTERED".equals(status)) throw notChangeable(status);
                txs.updateStatus(id, "REJECTED");
            }
            case "CANCEL" -> {
                if ("REQUESTED".equals(status) || "COUNTERED".equals(status)) {
                    requireRole(buyer, "Only the buyer can cancel a pending request");
                } else if (!"ACCEPTED".equals(status)) {
                    throw notChangeable(status);
                }
                txs.updateStatus(id, "CANCELLED");
                if ("ACCEPTED".equals(status)) listings.updateStatus(t.listingId(), "AVAILABLE");
            }
            case "COMPLETE" -> {
                requireRole(seller, "Only the seller can mark a deal as completed");
                if (!"ACCEPTED".equals(status)) throw notChangeable(status);
                txs.updateStatus(id, "COMPLETED");
                // Sold items leave the marketplace; rented items go back on the shelf.
                listings.updateStatus(t.listingId(), "SELL".equals(t.listingType()) ? "SOLD" : "AVAILABLE");
            }
            default -> throw ApiException.badRequest("Unknown action. Use ACCEPT, COUNTER, REJECT, CANCEL or COMPLETE");
        }
        return txs.findById(id).orElseThrow();
    }

    private void requireRole(boolean ok, String msg) {
        if (!ok) throw ApiException.forbidden(msg);
    }

    private ApiException notChangeable(String status) {
        return ApiException.conflict("This deal is " + status.toLowerCase() + " and can't be changed this way");
    }
}
