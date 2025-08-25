import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.sql.*;

public class Bookmovie extends JFrame {

    private Connection connection;

    public Bookmovie(String selectedMovie, String duration, String price, String imagePath) {
        setTitle("🎟 Book Your Seat - " + selectedMovie);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        connectToDatabase(); // DB connection

        // === Header ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.BLACK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = new JButton("←");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        backButton.setBackground(Color.ORANGE);
        backButton.setForeground(Color.BLACK);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> dispose());

        headerPanel.add(backButton, BorderLayout.WEST);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.BLACK);

        JLabel title = new JLabel(selectedMovie.toUpperCase());
        title.setFont(new Font("Serif", Font.BOLD, 38));
        title.setForeground(Color.ORANGE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("⏱ " + duration + "     💰 " + price);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        subTitle.setForeground(Color.LIGHT_GRAY);
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subTitle);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // === Poster Panel ===
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.setPreferredSize(new Dimension(350, 0));

        try {
            BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(imagePath));
            if (img != null) {
                Image scaled = img.getScaledInstance(300, 450, Image.SCALE_SMOOTH);
                JLabel poster = new JLabel(new ImageIcon(scaled));
                poster.setBorder(BorderFactory.createEmptyBorder(30, 25, 10, 25));
                leftPanel.add(poster, BorderLayout.NORTH);
            } else {
                throw new Exception("Image not found");
            }
        } catch (Exception e) {
            JLabel fallback = new JLabel("🎞 Poster Missing", JLabel.CENTER);
            fallback.setPreferredSize(new Dimension(300, 450));
            fallback.setForeground(Color.LIGHT_GRAY);
            fallback.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            leftPanel.add(fallback, BorderLayout.NORTH);
        }

        add(leftPanel, BorderLayout.WEST);

        // === Seat Selection ===
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        centerPanel.setBackground(Color.DARK_GRAY);

        JLabel seatLabel = new JLabel("🎫 Select Your Seat", JLabel.CENTER);
        seatLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        seatLabel.setForeground(Color.WHITE);
        centerPanel.add(seatLabel, BorderLayout.NORTH);

        JPanel seatPanel = new JPanel(new GridLayout(5, 10, 10, 10));
        seatPanel.setBackground(new Color(40, 40, 40));
        JCheckBox[][] seats = new JCheckBox[5][10];

        int seatNumber = 1;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                JCheckBox seat = new JCheckBox(String.valueOf(seatNumber));
                seat.setFont(new Font("Segoe UI", Font.BOLD, 14));
                seat.setBackground(Color.LIGHT_GRAY);
                seat.setFocusPainted(false);
                seat.setHorizontalAlignment(SwingConstants.CENTER);

                int finalI = i;
                int finalJ = j;
                seat.addActionListener(e -> {
                    if (seat.isSelected()) {
                        seat.setText("✔");
                        seat.setBackground(new Color(50, 205, 50));
                    } else {
                        seat.setText(String.valueOf(finalI * 10 + finalJ + 1));
                        seat.setBackground(Color.LIGHT_GRAY);
                    }
                });

                seats[i][j] = seat;
                seatPanel.add(seat);
                seatNumber++;
            }
        }

        centerPanel.add(seatPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // === Bottom Panel ===
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JPanel timePanel = new JPanel();
        timePanel.setBackground(Color.BLACK);

        JLabel timeLabel = new JLabel("🕒 Showtime:");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        String[] showtimes = {"10:00 AM", "1:30 PM", "4:00 PM", "7:30 PM", "10:00 PM"};
        JComboBox<String> timeDropdown = new JComboBox<>(showtimes);
        timeDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        timePanel.add(timeLabel);
        timePanel.add(timeDropdown);

        JButton bookBtn = new JButton("✅ Confirm Booking");
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bookBtn.setBackground(Color.ORANGE);
        bookBtn.setForeground(Color.BLACK);
        bookBtn.setFocusPainted(false);
        bookBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bookBtn.setPreferredSize(new Dimension(220, 50));
        bookBtn.setMaximumSize(new Dimension(300, 50));

        // ✅ Confirm Booking Logic + Open Payment Page
        bookBtn.addActionListener(e -> {
            StringBuilder selectedSeats = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 10; j++) {
                    if (seats[i][j].isSelected()) {
                        selectedSeats.append((i * 10 + j + 1)).append(" ");
                    }
                }
            }

            if (selectedSeats.length() == 0) {
                JOptionPane.showMessageDialog(Bookmovie.this,
                        "Please select at least one seat.",
                        "No Seat Selected",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                String selectedTime = (String) timeDropdown.getSelectedItem();

                // 💾 Save to DB
                saveBooking(selectedMovie, selectedTime, selectedSeats.toString().trim(), price, duration, imagePath);

                // 👉 Open Payment Page
                SwingUtilities.invokeLater(() -> {
                    new PaymentPage(selectedMovie, selectedTime, selectedSeats.toString().trim(), price, duration, imagePath);
                });

                // Close current window
                dispose();
            }
        });

        bottomPanel.add(timePanel);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(bookBtn);

        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/test";
            String user = "root";
            String password = "sehrish123@"; // Use your DB password
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ DB Connected (Bookmovie)");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBooking(String movie, String time, String seats, String price, String duration, String imagePath) {
        String query = "INSERT INTO bookings (movie_name, show_time, seats, price, duration, image_path) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, movie);
            stmt.setString(2, time);
            stmt.setString(3, seats);
            stmt.setString(4, price);
            stmt.setString(5, duration);
            stmt.setString(6, imagePath);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Booking failed to save!", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
