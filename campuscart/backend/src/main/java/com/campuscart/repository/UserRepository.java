package com.campuscart.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.campuscart.model.User;

@Repository
public class UserRepository {
    private static final RowMapper<User> MAPPER = (rs, i) -> new User(
            rs.getLong("id"), rs.getString("name"), rs.getString("email"),
            rs.getString("password_hash"), rs.getString("hostel"));

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<User> findByEmail(String email) {
        List<User> r = jdbc.query("SELECT * FROM users WHERE LOWER(email) = LOWER(?)", MAPPER, email);
        return r.stream().findFirst();
    }

    public Optional<User> findById(long id) {
        List<User> r = jdbc.query("SELECT * FROM users WHERE id = ?", MAPPER, id);
        return r.stream().findFirst();
    }

    public long create(String name, String email, String passwordHash, String hostel) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (name, email, password_hash, hostel) VALUES (?, ?, ?, ?)",
                    new String[] { "id" });
            ps.setString(1, name);
            ps.setString(2, email.toLowerCase());
            ps.setString(3, passwordHash);
            ps.setString(4, hostel);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }
}
