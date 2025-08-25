import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.sql.*;

public class PaymentPage extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/test";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "sehrish123@";

    public PaymentPage(String movie, String showtime, String seats, String priceStr, String duration, String imagePath) {
        setTitle("💳 Payment - " + movie);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(new Color(20, 20, 20));

        Color bgColor = new Color(20, 20, 20);
        Color fgColor = Color.WHITE;
        Color accentColor = new Color(255, 165, 0);
        Font titleFont = new Font("Segoe UI", Font.BOLD, 28);
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 18);

        double ticketPrice = Double.parseDouble(priceStr.replaceAll("[^\\d.]", ""));
        int seatCount = seats.trim().split("\\s+").length;
        double subtotal = ticketPrice * seatCount;
        double gst = subtotal * 0.18;
        double total = subtotal + gst;

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(bgColor);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 10));

        try {
            BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(imagePath));
            if (img != null) {
                Image scaled = img.getScaledInstance(350, 500, Image.SCALE_SMOOTH);
                JLabel poster = new JLabel(new ImageIcon(scaled));
                poster.setHorizontalAlignment(SwingConstants.CENTER);
                leftPanel.add(poster, BorderLayout.NORTH);
            } else {
                throw new Exception("Image not found");
            }
        } catch (Exception e) {
            JLabel fallback = new JLabel("🎞 Poster Not Available", SwingConstants.CENTER);
            fallback.setFont(labelFont);
            fallback.setForeground(fgColor);
            leftPanel.add(fallback, BorderLayout.CENTER);
        }

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(bgColor);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 50));

        JLabel heading = new JLabel("🎬 Booking Summary");
        heading.setFont(titleFont);
        heading.setForeground(accentColor);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel movieLabel = createStyledLabel("Movie: " + movie, fgColor, labelFont);
        JLabel timeLabel = createStyledLabel("Showtime: " + showtime, fgColor, labelFont);
        JLabel seatLabel = createStyledLabel("Seats: " + seats, fgColor, labelFont);
        JLabel durLabel = createStyledLabel("Duration: " + duration, fgColor, labelFont);
        JLabel perSeatLabel = createStyledLabel("Price per Seat: ₹" + ticketPrice, fgColor, labelFont);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(bgColor);
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.add(heading);
        summaryPanel.add(Box.createVerticalStrut(10));
        summaryPanel.add(movieLabel);
        summaryPanel.add(timeLabel);
        summaryPanel.add(seatLabel);
        summaryPanel.add(durLabel);
        summaryPanel.add(perSeatLabel);

        JPanel breakdown = new JPanel(new GridLayout(3, 2, 10, 10));
        breakdown.setBackground(bgColor);
        breakdown.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(accentColor),
                "💵 Payment Breakdown",
                0, 0, labelFont, accentColor));

        breakdown.add(createStyledLabel("Subtotal:", fgColor, labelFont));
        breakdown.add(createStyledLabel("₹" + String.format("%.2f", subtotal), fgColor, labelFont));
        breakdown.add(createStyledLabel("GST (18%):", fgColor, labelFont));
        breakdown.add(createStyledLabel("₹" + String.format("%.2f", gst), fgColor, labelFont));
        breakdown.add(createStyledLabel("Total Amount:", fgColor, labelFont));
        breakdown.add(createStyledLabel("₹" + String.format("%.2f", total), accentColor, new Font("Segoe UI", Font.BOLD, 20)));

        JPanel cardPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        cardPanel.setBackground(bgColor);
        cardPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(accentColor),
                "💳 Card Details",
                0, 0, labelFont, accentColor));

        JTextField cardNum = new JTextField();
        JTextField cardName = new JTextField();
        JTextField expiry = new JTextField();
        JTextField cvv = new JTextField();

        cardPanel.add(createStyledLabel("Card Number:", fgColor, labelFont));
        cardPanel.add(cardNum);
        cardPanel.add(createStyledLabel("Card Holder Name:", fgColor, labelFont));
        cardPanel.add(cardName);
        cardPanel.add(createStyledLabel("Expiry (MM/YY):", fgColor, labelFont));
        cardPanel.add(expiry);
        cardPanel.add(createStyledLabel("CVV:", fgColor, labelFont));
        cardPanel.add(cvv);

        for (Component comp : cardPanel.getComponents()) {
            if (comp instanceof JTextField tf) {
                tf.setBackground(new Color(50, 50, 50));
                tf.setForeground(Color.WHITE);
                tf.setCaretColor(Color.WHITE);
            }
        }

        JButton payBtn = new JButton("✅ Pay ₹" + String.format("%.2f", total));
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        payBtn.setBackground(accentColor);
        payBtn.setForeground(Color.BLACK);
        payBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        payBtn.setFocusPainted(false);
        payBtn.setPreferredSize(new Dimension(250, 45));

        payBtn.addActionListener(e -> {
            String cardNumber = cardNum.getText().trim();
            String cardHolder = cardName.getText().trim();
            String cardExpiry = expiry.getText().trim();

            if (cardNumber.isEmpty() || cardHolder.isEmpty() || cardExpiry.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all card details.", "Missing Info", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

                // 1. Insert into Bookings
                String bookingSQL = "INSERT INTO Bookings (movie_name, show_time, seats, price, duration, image_path) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement bookStmt = conn.prepareStatement(bookingSQL, Statement.RETURN_GENERATED_KEYS);
                bookStmt.setString(1, movie);
                bookStmt.setString(2, showtime);
                bookStmt.setString(3, seats);
                bookStmt.setDouble(4, ticketPrice);
                bookStmt.setString(5, duration);
                bookStmt.setString(6, imagePath);
                bookStmt.executeUpdate();

                ResultSet keys = bookStmt.getGeneratedKeys();
                int bookingId = -1;
                if (keys.next()) {
                    bookingId = keys.getInt(1);
                }

                // 2. Insert into Payments
                if (bookingId != -1) {
                    String paySQL = "INSERT INTO Payments (booking_id, card_number, card_name, expiry, total_amount) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement payStmt = conn.prepareStatement(paySQL);
                    payStmt.setInt(1, bookingId);
                    payStmt.setString(2, cardNumber);
                    payStmt.setString(3, cardHolder);
                    payStmt.setString(4, cardExpiry);
                    payStmt.setDouble(5, total);
                    payStmt.executeUpdate();
                }

                conn.close();

                JOptionPane.showMessageDialog(this, "🎉 Payment Successful!\nBooking ID: " + bookingId, "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel payPanel = new JPanel();
        payPanel.setBackground(bgColor);
        payPanel.add(payBtn);

        rightPanel.add(summaryPanel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(breakdown);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(cardPanel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(payPanel);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JLabel createStyledLabel(String text, Color color, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(font);
        return label;
    }
}
