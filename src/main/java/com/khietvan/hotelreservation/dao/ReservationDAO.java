package com.khietvan.hotelreservation.dao;

import com.khietvan.hotelreservation.models.Reservation;
import com.khietvan.hotelreservation.utils.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservationDAO {

    public static boolean insertReservation(Reservation reservation) {
        String reservationQuery = """
            INSERT INTO reservations (guest_id, check_in, check_out, status)
            VALUES (?, ?, ?, ?)
        """;

        String mappingQuery = """
            INSERT INTO reservation_rooms (reservation_id, room_id, guests_in_room)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement resStmt = conn.prepareStatement(reservationQuery, Statement.RETURN_GENERATED_KEYS)) {

            // Insert reservation
            resStmt.setInt(1, reservation.getGuestId());
            resStmt.setDate(2, Date.valueOf(reservation.getCheckInDate()));
            resStmt.setDate(3, Date.valueOf(reservation.getCheckOutDate()));
            resStmt.setBoolean(4, reservation.isStatus());

            int affected = resStmt.executeUpdate();

            if (affected == 0) return false;

            // Get generated reservation_id
            ResultSet rs = resStmt.getGeneratedKeys();
            if (rs.next()) {
                int reservationId = rs.getInt(1);
                reservation.setReservationId(reservationId);

                // Insert room mappings
                try (PreparedStatement mapStmt = conn.prepareStatement(mappingQuery)) {
                    for (Map.Entry<Integer, Integer> entry : reservation.getReservedRooms().entrySet()) {
                        mapStmt.setInt(1, reservationId);
                        mapStmt.setInt(2, entry.getKey());       // room_id
                        mapStmt.setInt(3, entry.getValue());     // guests_in_room
                        mapStmt.addBatch();
                    }
                    mapStmt.executeBatch();
                }

                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static int insertReservationWithRooms(Reservation reservation) {
        String reservationQuery = "INSERT INTO reservations (guest_id, check_in_date, check_out_date, status) VALUES (?, ?, ?, ?)";
        String joinQuery = "INSERT INTO reservation_rooms (reservation_id, room_id, guest_count) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // Transaction start

            // Insert reservation
            try (PreparedStatement stmt = conn.prepareStatement(reservationQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, reservation.getGuestId());
                stmt.setDate(2, Date.valueOf(reservation.getCheckInDate()));
                stmt.setDate(3, Date.valueOf(reservation.getCheckOutDate()));
                stmt.setBoolean(4, reservation.isStatus());
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int reservationId = rs.getInt(1);

                    // Insert into reservation_rooms
                    try (PreparedStatement joinStmt = conn.prepareStatement(joinQuery)) {
                        for (Map.Entry<Integer, Integer> entry : reservation.getReservedRooms().entrySet()) {
                            joinStmt.setInt(1, reservationId);
                            joinStmt.setInt(2, entry.getKey());
                            joinStmt.setInt(3, entry.getValue());
                            joinStmt.addBatch();
                        }
                        joinStmt.executeBatch();
                    }

                    conn.commit();
                    return reservationId;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // error
    }

    // in ReservationDAO.java
    public static Reservation getReservationByGuestId(int guestId) {
        String sql1 = "SELECT * FROM reservations WHERE guest_id = ?";
        try (var conn = DBUtil.getConnection();
             var ps1  = conn.prepareStatement(sql1)) {

            ps1.setInt(1, guestId);
            var rs1 = ps1.executeQuery();
            if (!rs1.next()) return null;

            Reservation r = new Reservation(
                    rs1.getInt("reservation_id"),
                    rs1.getInt("guest_id"),
                    rs1.getDate("check_in_date").toLocalDate(),
                    rs1.getDate("check_out_date").toLocalDate(),
                    rs1.getBoolean("status")
            );

            String sql2 = "SELECT room_id, guest_count FROM reservation_rooms WHERE reservation_id = ?";
            try (var ps2 = conn.prepareStatement(sql2)) {
                ps2.setInt(1, r.getReservationId());
                var rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    r.addRoom(rs2.getInt("room_id"), rs2.getInt("guest_count"));
                }
            }
            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    // Latest reservation id for a guest (assumes bigger id = latest)
    public static Integer getLatestReservationIdByGuestId(int guestId) {
        String sql = "SELECT reservation_id FROM reservations WHERE guest_id=? ORDER BY reservation_id DESC LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Latest reservation status (true/false) for a guest, or null if none
    public static Boolean getLatestReservationStatusByGuestId(int guestId) {
        String sql = "SELECT status FROM reservations WHERE guest_id=? ORDER BY reservation_id DESC LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // no reservation
    }

    // Update reservation.status by reservation_id
    public static boolean updateReservationStatus(int reservationId, boolean status) {
        String sql = "UPDATE reservations SET status=? WHERE reservation_id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, status);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ReservationDAO.java
    public static Reservation getReservationWithRoomsByGuestId(int guestId) {
        String sql = """
        SELECT res.reservation_id,
               res.guest_id,
               res.check_in_date,
               res.check_out_date,
               res.status,
               rr.room_id,
               COALESCE(rr.guests_in_room, rr.guest_count) AS guests
          FROM reservations res
          JOIN reservation_rooms rr
            ON rr.reservation_id = res.reservation_id
         WHERE res.guest_id = ?
         ORDER BY rr.room_id
    """;

        try (var conn = DBUtil.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            try (var rs = ps.executeQuery()) {
                Reservation resObj = null;
                while (rs.next()) {
                    if (resObj == null) {
                        resObj = new Reservation(
                                rs.getInt("reservation_id"),
                                rs.getInt("guest_id"),
                                rs.getDate("check_in_date").toLocalDate(),
                                rs.getDate("check_out_date").toLocalDate(),
                                rs.getBoolean("status")
                        );
                    }
                    resObj.addRoom(rs.getInt("room_id"), rs.getInt("guests")); // <- uses alias
                }
                return resObj;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // in ReservationDAO.java
    public static boolean deleteReservationCascade(int reservationId) {
        String getRoomsSql = "SELECT room_id FROM reservation_rooms WHERE reservation_id = ?";
        String delJoinSql  = "DELETE FROM reservation_rooms WHERE reservation_id = ?";
        String delResSql   = "DELETE FROM reservations WHERE reservation_id = ?";
        String freeRoomSql = "UPDATE rooms SET status = TRUE WHERE room_id = ?"; // optional, if you use rooms.status

        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);

            // collect room ids for optional freeing
            List<Integer> roomIds = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(getRoomsSql)) {
                ps.setInt(1, reservationId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) roomIds.add(rs.getInt(1));
                }
            }

            // delete join rows first
            try (PreparedStatement ps = c.prepareStatement(delJoinSql)) {
                ps.setInt(1, reservationId);
                ps.executeUpdate();
            }

            // delete reservation
            try (PreparedStatement ps = c.prepareStatement(delResSql)) {
                ps.setInt(1, reservationId);
                ps.executeUpdate();
            }

            // optional: mark rooms available again (only if you still toggle rooms.status)
            try (PreparedStatement ps = c.prepareStatement(freeRoomSql)) {
                for (Integer roomId : roomIds) {
                    ps.setInt(1, roomId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            c.commit();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            try { /*noinspection CallToPrintStackTrace*/ } finally {}
            return false;
        }
    }

}
