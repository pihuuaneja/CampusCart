-- Seed data: listing categories.
INSERT INTO categories (name) VALUES
    ('Books'), ('Cycles'), ('Electronics'), ('Hostel Essentials'),
    ('Furniture'), ('Clothing'), ('Stationery'), ('Sports'), ('Other')
ON CONFLICT (name) DO NOTHING;
