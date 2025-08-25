import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.imageio.ImageIO;
import java.io.IOException;

public class LoginForm extends JFrame {

    JTextField userField;
    JPasswordField passField;
    JButton loginButton;

    String dbUrl = "jdbc:mysql://localhost:3306/test";
    String dbUser = "root";
    String dbPassword = "sehrish123@";

    public LoginForm() {
        setTitle("🎬 Movie Booking Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setContentPane(new BackgroundPanel("movie/ticketmovie.jpg"));
        initComponents();
    }

    private void initComponents() {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setOpaque(false);
        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        outerGbc.anchor = GridBagConstraints.CENTER;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("🔐 Login Details");
        title.setFont(new Font("Serif", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        panel.add(title, gbc);

        gbc.gridy++;
        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        panel.add(userLabel, gbc);

        gbc.gridy++;
        userField = new JTextField();
        userField.setFont(new Font("Monospaced", Font.BOLD, 24));
        userField.setBackground(Color.BLACK);
        userField.setForeground(Color.WHITE);
        userField.setCaretColor(Color.WHITE);
        userField.setPreferredSize(new Dimension(600, 50));
        panel.add(userField, gbc);

        gbc.gridy++;
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        panel.add(passLabel, gbc);

        gbc.gridy++;
        passField = new JPasswordField();
        passField.setFont(new Font("Monospaced", Font.BOLD, 24));
        passField.setBackground(Color.BLACK);
        passField.setForeground(Color.WHITE);
        passField.setCaretColor(Color.WHITE);
        passField.setPreferredSize(new Dimension(600, 50));
        panel.add(passField, gbc);

        gbc.gridy++;
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 28));
        loginButton.setBackground(new Color(30, 144, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(300, 60));
        panel.add(loginButton, gbc);

        outerPanel.add(panel, outerGbc);
        add(outerPanel);

        loginButton.addActionListener(e -> login());
    }

    private void login() {
        String username = userField.getText();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            String sql = "SELECT * FROM user WHERE username=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                this.dispose();
                new movieselection();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }

            conn.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}

// ✅ Updated BackgroundPanel class to load image properly from resources
class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        try {
            backgroundImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream(imagePath));
            if (backgroundImage == null) {
                System.out.println("Image not found: " + imagePath);
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("Error loading background image: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
