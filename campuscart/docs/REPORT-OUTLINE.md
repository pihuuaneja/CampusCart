# Project report outline

Matches the submission list in the project proposal. Each line says where to find the material.

1. **Problem definition**: scattered WhatsApp groups, no trust, no search. Reuse the text of your proposal.
2. **Objectives and scope**: features in `README.md`. State what is out of scope (payments, delivery).
3. **System design**
   - Architecture: React frontend, Spring Boot REST API, PostgreSQL (three tiers).
   - Package structure: see `README.md`.
   - Database design: `docs/ER-diagram.md` and `database/schema.sql`.
   - API design: `docs/API.md`. Include the status-flow diagram.
   - Security: BCrypt password hashing, JWT, college-email rule, owner-only edits, parameterised SQL.
4. **Implementation**: screenshots of Browse, Post item, Item detail, Deals. Explain the hostel exchange feature.
5. **Database integration**: explain `JdbcTemplate`, joins in `ListingRepository`, dynamic search with bound parameters, and `@Transactional` in `TransactionService`.
6. **Testing**: output of `mvn test`, plus a manual test table (test case, steps, expected result, actual result). Use the "Try the full flow" section of the README as a base.
7. **Limitations and future work**: image upload, email OTP, chat, notifications, ratings.
8. **Conclusion**.
9. **Appendix**: source code, database scripts, user guide.
