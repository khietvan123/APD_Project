package com.khietvan.hotelreservation.dao;

import com.khietvan.hotelreservation.models.Discount;
import com.khietvan.hotelreservation.models.Guest;
import com.khietvan.hotelreservation.utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {

    public static List<Guest> getAllGuests() {
        List<Guest> guests = new ArrayList<>();
        String query = """
        SELECT g.*, d.discount_id, d.title, d.amount,
               r.reservation_id, r.check_in_date, r.check_out_date, 
               /* alias whatever your boolean column is */ 
               r.status AS confirmed
        FROM guests g
        LEFT JOIN discounts d ON g.discount_id = d.discount_id
        LEFT JOIN (
            SELECT r1.*
            FROM reservations r1
            JOIN (
                SELECT guest_id, MAX(reservation_id) AS max_id
                FROM reservations
                GROUP BY guest_id
            ) last ON last.guest_id = r1.guest_id AND last.max_id = r1.reservation_id
        ) r ON r.guest_id = g.guest_id
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Discount (null-safe)
                Integer discId = rs.getObject("discount_id", Integer.class);
                Discount discount = null;
                if (discId != null) {
                    discount = new Discount(
                            discId,
                            rs.getDouble("amount"),
                            rs.getString("title")
                    );
                }

                Guest guest = new Guest(
                        rs.getInt("guest_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("feedback")
                );
                guest.setDiscount(discount);

                // Attach latest reservation status if present
                Integer resId = rs.getObject("reservation_id", Integer.class);
                if (resId != null) {
                    com.khietvan.hotelreservation.models.Reservation res =
                            new com.khietvan.hotelreservation.models.Reservation(
                                    resId,
                                    guest.getGuestId(),
                                    rs.getDate("check_in_date").toLocalDate(),
                                    rs.getDate("check_out_date").toLocalDate(),
                                    rs.getBoolean("confirmed") // true = confirmed/checked-in
                            );
                    guest.setReservation(res);
                }

                guests.add(guest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guests;
    }

    public static List<Guest> searchGuests(String name, String phone) {
        List<Guest> guests = new ArrayList<>();
        String query = """
        SELECT g.*, d.discount_id, d.title, d.amount,
               r.reservation_id, r.check_in_date, r.check_out_date,
               r.status AS confirmed
        FROM guests g
        LEFT JOIN discounts d ON g.discount_id = d.discount_id
        LEFT JOIN (
            SELECT r1.*
            FROM reservations r1
            JOIN (
                SELECT guest_id, MAX(reservation_id) AS max_id
                FROM reservations
                GROUP BY guest_id
            ) last ON last.guest_id = r1.guest_id AND last.max_id = r1.reservation_id
        ) r ON r.guest_id = g.guest_id
        WHERE g.name LIKE ? AND g.phone LIKE ?
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + (name == null ? "" : name) + "%");
            stmt.setString(2, "%" + (phone == null ? "" : phone) + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Integer discId = rs.getObject("discount_id", Integer.class);
                    Discount discount = null;
                    if (discId != null) {
                        discount = new Discount(
                                discId,
                                rs.getDouble("amount"),
                                rs.getString("title")
                        );
                    }

                    Guest guest = new Guest(
                            rs.getInt("guest_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getString("feedback")
                    );
                    guest.setDiscount(discount);

                    Integer resId = rs.getObject("reservation_id", Integer.class);
                    if (resId != null) {
                        com.khietvan.hotelreservation.models.Reservation res =
                                new com.khietvan.hotelreservation.models.Reservation(
                                        resId,
                                        guest.getGuestId(),
                                        rs.getDate("check_in_date").toLocalDate(),
                                        rs.getDate("check_out_date").toLocalDate(),
                                        rs.getBoolean("confirmed")
                                );
                        guest.setReservation(res);
                    }

                    guests.add(guest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guests;
    }

    public static boolean saveGuest(Guest guest) {
        String query = "INSERT INTO guests (name, phone, email, address, feedback, discount_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getPhone());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getAddress());
            stmt.setString(5, guest.getFeedBack());

            if (guest.getDiscount() != null) {
                stmt.setInt(6, guest.getDiscount().getDiscountId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Guest saveGuestAndReturn(Guest guest) {
        String query = "INSERT INTO guests (name, phone, email, address, feedback, discount_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getPhone());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getAddress());
            stmt.setString(5, guest.getFeedBack());

            if (guest.getDiscount() != null) {
                stmt.setInt(6, guest.getDiscount().getDiscountId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    guest.setGuestId(rs.getInt(1));
                    return guest;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getLastInsertedGuestId() {
        String sql = "SELECT LAST_INSERT_ID()";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Add this to your existing GuestDAO
    public static boolean updateGuestDiscount(int guestId, Integer discountId) {
        String sql = "UPDATE guests SET discount_id = ? WHERE guest_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (discountId == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, discountId);
            ps.setInt(2, guestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // GuestDAO.java
    public static boolean updateGuestBasic(int guestId, String name, String phone, String email, String address) {
        String sql = "UPDATE guests SET name=?, phone=?, email=?, address=? WHERE guest_id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setString(4, address);
            ps.setInt(5, guestId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // in GuestDAO.java
    public static boolean deleteGuest(int guestId) {
        String sql = "DELETE FROM guests WHERE guest_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
