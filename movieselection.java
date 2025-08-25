import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.List;

public class movieselection extends JFrame {

    private Connection connection;

    public movieselection() {
        setTitle("🎬 Movie Selection");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        connectToDatabase(); // 🔌 DB Connection

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.BLACK);

        JLabel header = new JLabel("🎬 Welcome to CineMax", JLabel.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 56));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        headerPanel.add(header, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 60, 60));

        Map<String, List<String[]>> movieSections = fetchMoviesByCategory();

        for (String category : movieSections.keySet()) {
            String sectionTitle = switch (category.toLowerCase()) {
                case "bollywood" -> "🇮🇳 Bollywood Blockbusters";
                case "hollywood" -> "🌎 Hollywood Hits";
                case "south" -> "🔥 South Indian Spectacles";
                default -> "🎞 Other Movies";
            };
            addMovieSection(mainPanel, sectionTitle, movieSections.get(category));
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Color.BLACK);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/test";
            String user = "root";
            String password = "sehrish123@"; // Replace with your actual password

            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connected to Database.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<String, List<String[]>> fetchMoviesByCategory() {
        Map<String, List<String[]>> movieMap = new HashMap<>();
        String query = "SELECT * FROM movies";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String category = rs.getString("category");
                String[] movieData = {
                    rs.getString("imagePath"),
                    rs.getString("name"),
                    rs.getString("duration"),
                    rs.getString("price")
                };
                movieMap.computeIfAbsent(category, k -> new ArrayList<>()).add(movieData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return movieMap;
    }

    private void addMovieSection(JPanel parentPanel, String sectionTitle, List<String[]> movies) {
        JLabel sectionHeader = new JLabel(sectionTitle, JLabel.CENTER);
        sectionHeader.setFont(new Font("Serif", Font.BOLD, 38));
        sectionHeader.setForeground(new Color(255, 215, 0));
        sectionHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionHeader.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        parentPanel.add(sectionHeader);

        JPanel movieGrid = new JPanel(new GridBagLayout());
        movieGrid.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 30, 30, 30);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int col = 0, row = 0;
        for (String[] movie : movies) {
            gbc.gridx = col;
            gbc.gridy = row;
            movieGrid.add(createMovieCard(movie[0], movie[1], movie[2], movie[3]), gbc);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }

        parentPanel.add(movieGrid);
    }

    private JPanel createMovieCard(String imagePath, String name, String duration, String price) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(300, 520));
        card.setBackground(new Color(20, 20, 20));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(imagePath));
            Image scaled = img.getScaledInstance(280, 340, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaled));
            imgLabel.setHorizontalAlignment(JLabel.CENTER);
            card.add(imgLabel, BorderLayout.NORTH);
        } catch (IOException | NullPointerException e) {
            JLabel fallback = new JLabel("🎞 Poster Missing", JLabel.CENTER);
            fallback.setPreferredSize(new Dimension(280, 340));
            fallback.setForeground(Color.LIGHT_GRAY);
            fallback.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            card.add(fallback, BorderLayout.NORTH);
        }

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(20, 20, 20));

        JLabel title = new JLabel(name.toUpperCase());
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel durationLabel = new JLabel("⏱ " + duration);
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        durationLabel.setForeground(Color.LIGHT_GRAY);
        durationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel("💰 " + price);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        priceLabel.setForeground(new Color(0, 255, 180));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(title);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(durationLabel);
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        card.add(infoPanel, BorderLayout.CENTER);

        JButton bookBtn = new JButton("Book Now 🎟");
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bookBtn.setBackground(new Color(255, 140, 0));
        bookBtn.setForeground(Color.BLACK);
        bookBtn.setFocusPainted(false);
        bookBtn.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
        bookBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bookBtn.setPreferredSize(new Dimension(130, 40));

        bookBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                bookBtn.setBackground(Color.YELLOW);
            }

            public void mouseExited(MouseEvent e) {
                bookBtn.setBackground(new Color(255, 140, 0));
            }
        });

        // 🔁 Redirection to Bookmovie.java
        bookBtn.addActionListener(e -> {
            new Bookmovie(name, duration, price, imagePath);
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(20, 20, 20));
        btnPanel.add(bookBtn);
        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(movieselection::new);
    }
}
