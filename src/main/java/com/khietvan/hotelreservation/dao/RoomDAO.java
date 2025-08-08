package com.khietvan.hotelreservation.dao;

import com.khietvan.hotelreservation.models.Room;
import com.khietvan.hotelreservation.models.RoomType;
import com.khietvan.hotelreservation.utils.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    // ✅ Find one available room of a specific type
    public static Room findAvailableRoom(String type, LocalDate checkIn, LocalDate checkOut) {
        String query = """
            SELECT * 
              FROM rooms
             WHERE room_type = ?
               AND status = TRUE
               AND room_id NOT IN (
                     SELECT rr.room_id
                       FROM reservations res
                       JOIN reservation_rooms rr 
                         ON res.reservation_id = rr.reservation_id
                      WHERE NOT (
                          res.check_out_date <= ? 
                          OR res.check_in_date  >= ?
                      )
                 )
             ORDER BY price ASC
             LIMIT 1
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, type.toUpperCase());
            stmt.setDate(2, Date.valueOf(checkIn));
            stmt.setDate(3, Date.valueOf(checkOut));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Room room = new Room(
                            rs.getInt("room_id"),
                            RoomType.valueOf(rs.getString("room_type").toUpperCase()),
                            rs.getInt("number_of_beds"),
                            rs.getDouble("price"),
                            rs.getBoolean("status")
                    );
                    // if you have min/max guest columns:
                    room.setGuestLimits(rs.getInt("min_guests"), rs.getInt("max_guests"));
                    return room;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // no match (or SQL error)
        return null;
    }



    // ✅ Update room status (true = Available, false = Booked)
    public static boolean updateRoomStatus(int roomId, boolean status) {
        String query = "UPDATE rooms SET status = ? WHERE room_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, status);
            stmt.setInt(2, roomId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ Get all rooms (optional helper)
    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("room_id"),
                        RoomType.valueOf(rs.getString("room_type").toUpperCase()),
                        rs.getInt("number_of_beds"),
                        rs.getDouble("price"),
                        rs.getBoolean("status")   // <- boolean, not string compare
                );
                // if you actually have these columns; otherwise guard/remove
                room.setGuestLimits(rs.getInt("min_guests"), rs.getInt("max_guests"));
                rooms.add(room);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    public static List<Room> getAllAvailableRoomsSortedByPrice(LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> availableRooms = new ArrayList<>();
        String query = """
    SELECT r.*
    FROM rooms r
    WHERE r.status = TRUE
      AND r.room_id NOT IN (
          SELECT rr.room_id
          FROM reservations res
          JOIN reservation_rooms rr ON res.reservation_id = rr.reservation_id
          WHERE NOT (res.check_out_date <= ? OR res.check_in_date >= ?)
      )
    ORDER BY r.price ASC
""";


        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(checkInDate));
            stmt.setDate(2, Date.valueOf(checkOutDate));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("room_id"),
                        RoomType.valueOf(rs.getString("room_type").toUpperCase()),
                        rs.getInt("number_of_beds"),
                        rs.getDouble("price"),
                        rs.getBoolean("status")
                );
                availableRooms.add(room);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableRooms;
    }

    // in RoomDAO.java
    public static Room getRoomById(int id) {
        String q = "SELECT * FROM rooms WHERE room_id = ?";
        try (var conn = DBUtil.getConnection();
             var ps   = conn.prepareStatement(q)) {
            ps.setInt(1, id);
            var rs = ps.executeQuery();
            if (!rs.next()) return null;
            Room room = new Room(
                    rs.getInt("room_id"),
                    RoomType.valueOf(rs.getString("room_type").toUpperCase()),
                    rs.getInt("number_of_beds"),
                    rs.getDouble("price"),
                    rs.getBoolean("status")
            );
            room.setGuestLimits(rs.getInt("min_guests"), rs.getInt("max_guests"));
            return room;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
