package com.campuscart.repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.campuscart.dto.ListingFilter;
import com.campuscart.dto.ListingRequest;
import com.campuscart.model.ListingView;

@Repository
public class ListingRepository {
    private static final String BASE = """
            SELECT l.id, l.seller_id, u.name AS seller_name, u.hostel AS seller_hostel,
                   l.category_id, c.name AS category_name, l.title, l.description, l.price,
                   l.listing_type, l.rent_period, l.hostel_quick, l.image_url, l.status, l.created_at
            FROM listings l
            JOIN users u ON u.id = l.seller_id
            JOIN categories c ON c.id = l.category_id
            """;

    private static final RowMapper<ListingView> MAPPER = (rs, i) -> new ListingView(
            rs.getLong("id"), rs.getLong("seller_id"), rs.getString("seller_name"), rs.getString("seller_hostel"),
            rs.getInt("category_id"), rs.getString("category_name"),
            rs.getString("title"), rs.getString("description"), rs.getBigDecimal("price"),
            rs.getString("listing_type"), rs.getString("rent_period"), rs.getBoolean("hostel_quick"),
            rs.getString("image_url"), rs.getString("status"), rs.getTimestamp("created_at").toInstant());

    private final JdbcTemplate jdbc;

    public ListingRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Public search: only AVAILABLE listings. Every user value is a bound parameter. */
    public List<ListingView> search(ListingFilter f) {
        StringBuilder sql = new StringBuilder(BASE).append(" WHERE l.status = 'AVAILABLE'");
        List<Object> args = new ArrayList<>();

        if (StringUtils.hasText(f.q())) {
            sql.append(" AND (l.title ILIKE ? OR l.description ILIKE ?)");
            String like = "%" + f.q().trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (f.categoryId() != null) { sql.append(" AND l.category_id = ?"); args.add(f.categoryId()); }
        if (StringUtils.hasText(f.type())) { sql.append(" AND l.listing_type = ?"); args.add(f.type().toUpperCase()); }
        if (Boolean.TRUE.equals(f.hostelQuick())) { sql.append(" AND l.hostel_quick = TRUE"); }
        if (f.minPrice() != null) { sql.append(" AND l.price >= ?"); args.add(f.minPrice()); }
        if (f.maxPrice() != null) { sql.append(" AND l.price <= ?"); args.add(f.maxPrice()); }

        String sort = f.sort() == null ? "newest" : f.sort();
        sql.append(switch (sort) {
            case "price_asc" -> " ORDER BY l.price ASC, l.id DESC";
            case "price_desc" -> " ORDER BY l.price DESC, l.id DESC";
            default -> " ORDER BY l.created_at DESC, l.id DESC";
        });
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    public List<ListingView> findBySeller(long sellerId) {
        return jdbc.query(BASE + " WHERE l.seller_id = ? ORDER BY l.created_at DESC", MAPPER, sellerId);
    }

    public Optional<ListingView> findById(long id) {
        return jdbc.query(BASE + " WHERE l.id = ?", MAPPER, id).stream().findFirst();
    }

    public long create(long sellerId, ListingRequest r) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO listings (seller_id, category_id, title, description, price,
                                          listing_type, rent_period, hostel_quick, image_url)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, new String[] { "id" });
            ps.setLong(1, sellerId);
            ps.setInt(2, r.categoryId());
            ps.setString(3, r.title().trim());
            ps.setString(4, r.description());
            ps.setBigDecimal(5, r.price());
            ps.setString(6, r.type());
            if (r.rentPeriod() == null || "SELL".equals(r.type())) ps.setNull(7, Types.VARCHAR);
            else ps.setString(7, r.rentPeriod());
            ps.setBoolean(8, r.hostelQuick());
            ps.setString(9, StringUtils.hasText(r.imageUrl()) ? r.imageUrl().trim() : null);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void update(long id, ListingRequest r) {
        String period = "SELL".equals(r.type()) ? null : r.rentPeriod();
        jdbc.update("""
                UPDATE listings SET category_id = ?, title = ?, description = ?, price = ?,
                       listing_type = ?, rent_period = ?, hostel_quick = ?, image_url = ?
                WHERE id = ?
                """, r.categoryId(), r.title().trim(), r.description(), r.price(), r.type(), period,
                r.hostelQuick(), StringUtils.hasText(r.imageUrl()) ? r.imageUrl().trim() : null, id);
    }

    public void updateStatus(long id, String status) {
        jdbc.update("UPDATE listings SET status = ? WHERE id = ?", status, id);
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM listings WHERE id = ?", id);
    }
}
