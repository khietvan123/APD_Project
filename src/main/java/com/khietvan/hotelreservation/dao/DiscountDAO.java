package com.khietvan.hotelreservation.dao;

import com.khietvan.hotelreservation.models.Discount;
import com.khietvan.hotelreservation.utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiscountDAO {
    public static List<Discount> getAllDiscounts() {
        List<Discount> list = new ArrayList<>();
        String sql = "SELECT discount_id, title, amount FROM discounts ORDER BY amount DESC";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Discount d = new Discount(
                        rs.getInt("discount_id"),
                        rs.getDouble("amount"),
                        rs.getString("title")

                );
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


}
