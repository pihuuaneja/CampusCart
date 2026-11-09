package com.campuscart.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.campuscart.model.ConversationView;
import com.campuscart.model.MessageView;

@Repository
public class ChatRepository {
    public record Participants(long id, long listingId, long buyerId, long sellerId) {}

    private static final RowMapper<MessageView> MESSAGE = (rs, i) -> new MessageView(
            rs.getLong("id"), rs.getLong("conversation_id"), rs.getLong("sender_id"),
            rs.getString("name"), rs.getString("body"), rs.getTimestamp("created_at").toInstant());

    private static final String MESSAGE_SQL = """
            SELECT m.id, m.conversation_id, m.sender_id, u.name, m.body, m.created_at
            FROM messages m JOIN users u ON u.id = m.sender_id
            """;

    private final JdbcTemplate jdbc;

    public ChatRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Long> findConversation(long listingId, long buyerId) {
        return jdbc.query("SELECT id FROM conversations WHERE listing_id = ? AND buyer_id = ?",
                (rs, i) -> rs.getLong("id"), listingId, buyerId).stream().findFirst();
    }

    public long createConversation(long listingId, long buyerId) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO conversations (listing_id, buyer_id) VALUES (?, ?)", new String[] { "id" });
            ps.setLong(1, listingId);
            ps.setLong(2, buyerId);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public Optional<Participants> findParticipants(long conversationId) {
        return jdbc.query("""
                SELECT c.id, c.listing_id, c.buyer_id, l.seller_id
                FROM conversations c JOIN listings l ON l.id = c.listing_id WHERE c.id = ?
                """, (rs, i) -> new Participants(rs.getLong("id"), rs.getLong("listing_id"),
                        rs.getLong("buyer_id"), rs.getLong("seller_id")), conversationId).stream().findFirst();
    }

    public List<ConversationView> listForUser(long userId) {
        return jdbc.query("""
                SELECT c.id, c.listing_id, l.title, l.image_url,
                       CASE WHEN c.buyer_id = ? THEN l.seller_id ELSE c.buyer_id END AS other_id,
                       CASE WHEN c.buyer_id = ? THEN s.name ELSE b.name END AS other_name,
                       (SELECT m.body FROM messages m WHERE m.conversation_id = c.id ORDER BY m.id DESC LIMIT 1) AS last_body,
                       COALESCE((SELECT m.created_at FROM messages m WHERE m.conversation_id = c.id ORDER BY m.id DESC LIMIT 1),
                                c.created_at) AS last_at
                FROM conversations c
                JOIN listings l ON l.id = c.listing_id
                JOIN users b ON b.id = c.buyer_id
                JOIN users s ON s.id = l.seller_id
                WHERE c.buyer_id = ? OR l.seller_id = ?
                ORDER BY last_at DESC
                """, (rs, i) -> new ConversationView(
                        rs.getLong("id"), rs.getLong("listing_id"), rs.getString("title"), rs.getString("image_url"),
                        rs.getLong("other_id"), rs.getString("other_name"), rs.getString("last_body"),
                        rs.getTimestamp("last_at").toInstant()),
                userId, userId, userId, userId);
    }

    public List<MessageView> messages(long conversationId, long afterId) {
        return jdbc.query(MESSAGE_SQL + " WHERE m.conversation_id = ? AND m.id > ? ORDER BY m.id",
                MESSAGE, conversationId, afterId);
    }

    public MessageView addMessage(long conversationId, long senderId, String body) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO messages (conversation_id, sender_id, body) VALUES (?, ?, ?)", new String[] { "id" });
            ps.setLong(1, conversationId);
            ps.setLong(2, senderId);
            ps.setString(3, body);
            return ps;
        }, kh);
        long id = kh.getKey().longValue();
        return jdbc.query(MESSAGE_SQL + " WHERE m.id = ?", MESSAGE, id).get(0);
    }
}
