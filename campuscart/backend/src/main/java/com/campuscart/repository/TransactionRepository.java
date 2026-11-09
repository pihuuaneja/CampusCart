package com.campuscart.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.campuscart.model.TransactionView;

@Repository
public class TransactionRepository {
    private static final String BASE = """
            SELECT t.id, t.listing_id, l.title AS listing_title, l.price,
                   COALESCE(t.offer_price, l.price) AS offer_price, l.listing_type,
                   t.buyer_id, b.name AS buyer_name, l.seller_id, s.name AS seller_name,
                   t.status, t.message, t.created_at, t.updated_at
            FROM transactions t
            JOIN listings l ON l.id = t.listing_id
            JOIN users b ON b.id = t.buyer_id
            JOIN users s ON s.id = l.seller_id
            """;

    private static final RowMapper<TransactionView> MAPPER = (rs, i) -> new TransactionView(
            rs.getLong("id"), rs.getLong("listing_id"), rs.getString("listing_title"),
            rs.getBigDecimal("price"), rs.getBigDecimal("offer_price"), rs.getString("listing_type"),
            rs.getLong("buyer_id"), rs.getString("buyer_name"),
            rs.getLong("seller_id"), rs.getString("seller_name"),
            rs.getString("status"), rs.getString("message"),
            rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant());

    private final JdbcTemplate jdbc;

    public TransactionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public long create(long listingId, long buyerId, String message, BigDecimal offerPrice) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO transactions (listing_id, buyer_id, message, offer_price) VALUES (?, ?, ?, ?)",
                    new String[] { "id" });
            ps.setLong(1, listingId);
            ps.setLong(2, buyerId);
            ps.setString(3, message);
            ps.setBigDecimal(4, offerPrice);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public Optional<TransactionView> findById(long id) {
        return jdbc.query(BASE + " WHERE t.id = ?", MAPPER, id).stream().findFirst();
    }

    public List<TransactionView> findByBuyer(long buyerId) {
        return jdbc.query(BASE + " WHERE t.buyer_id = ? ORDER BY t.updated_at DESC", MAPPER, buyerId);
    }

    public List<TransactionView> findBySeller(long sellerId) {
        return jdbc.query(BASE + " WHERE l.seller_id = ? ORDER BY t.updated_at DESC", MAPPER, sellerId);
    }

    public boolean hasOpenRequest(long listingId, long buyerId) {
        Integer n = jdbc.queryForObject("""
                SELECT COUNT(*) FROM transactions
                WHERE listing_id = ? AND buyer_id = ? AND status IN ('REQUESTED','COUNTERED','ACCEPTED')
                """, Integer.class, listingId, buyerId);
        return n != null && n > 0;
    }

    public void updateStatus(long id, String status) {
        jdbc.update("UPDATE transactions SET status = ?, updated_at = NOW() WHERE id = ?", status, id);
    }

    /** Records a new offer and hands the turn to the other person. */
    public void updateOffer(long id, String status, BigDecimal offerPrice) {
        jdbc.update("UPDATE transactions SET status = ?, offer_price = ?, updated_at = NOW() WHERE id = ?",
                status, offerPrice, id);
    }

    /** When one request is accepted, the other open negotiations for that listing are closed. */
    public void rejectOtherRequests(long listingId, long acceptedId) {
        jdbc.update("""
                UPDATE transactions SET status = 'REJECTED', updated_at = NOW()
                WHERE listing_id = ? AND id <> ? AND status IN ('REQUESTED','COUNTERED')
                """, listingId, acceptedId);
    }
}
