package catchping;


import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URL;

public class LoginPanel extends JPanel {
    private JTextField nickNameField;
    private LoginListener loginListener;
    private JButton loginButton;
    private Image backgroundImage;

    public interface LoginListener {
        void onLoginRequest(String nickname);
    }

    public LoginPanel() {
        setLayout(new BorderLayout());
        setBackground(Constants.BACKGROUND_COLOR);
        setPreferredSize(new Dimension(1200, 800));
        initializeUI();
    }

    private void initializeUI() {
        // 이미지 로드 시, 경로가 정확한지 확인하세요.
        backgroundImage = new ImageIcon("images/login.jpg").getImage();  // 올바른 경로 확인

        // 그라데이션 배경을 위한 패널
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);  // 항상 첫 번째 줄에 호출해야 합니다.
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // 배경 이미지 그리기
                if (backgroundImage != null) {
                    int width = getWidth();
                    int height = getHeight();
                    g2d.drawImage(backgroundImage, 0, 0, width, height, this);
                }
            }
        };
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setOpaque(true);  // 콘텐츠 패널을 불투명하게 설정
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        
     // 닉네임 라벨
        JLabel nickLabel = createLabel("닉네임을 입력하세요", Constants.DEFAULT_FONT, Font.BOLD, Constants.NORMAL_FONT_SIZE * 2); // 볼드, 크기 크게
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 5, 10);
        contentPanel.add(nickLabel, gbc);
        
        // 닉네임 입력 필드
        nickNameField = new JTextField(15);
        nickNameField.setPreferredSize(new Dimension(200, 35));
        styleTextField(nickNameField);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 20, 10);
        contentPanel.add(nickNameField, gbc);
        
        // 로그인 버튼
        loginButton = new JButton("게임 입장");
        styleButton(loginButton);
        gbc.gridy = 4;
        contentPanel.add(loginButton, gbc);

        // 이벤트 리스너 설정
        setupEventListeners();

        // 메인 패널에 컨텐츠 패널 추가
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        loginButton.addActionListener(e -> processLogin());

        nickNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    processLogin();
                }
            }
        });
    }

    private void processLogin() {
        String nickname = nickNameField.getText().trim();
        if (nickname.isEmpty()) {
            showError("닉네임을 입력해주세요.");
            return;
        }

        if (nickname.length() > 10) {
            showError("닉네임은 10자 이내로 입력해주세요.");
            return;
        }

        if (loginListener != null) {
            loginListener.onLoginRequest(nickname);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "알림",
            JOptionPane.WARNING_MESSAGE
        );
    }

    private JLabel createLabel(String text, String fontName, int fontStyle, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(fontName, fontStyle, fontSize));
        label.setForeground(Constants.TEXT_COLOR);
        return label;
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(new Font(Constants.DEFAULT_FONT, Font.PLAIN, Constants.NORMAL_FONT_SIZE));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Constants.PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(150, 40));
        button.setFont(new Font(Constants.DEFAULT_FONT, Font.BOLD, Constants.NORMAL_FONT_SIZE));
        button.setForeground(Color.WHITE);
        button.setBackground(Constants.PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Constants.PRIMARY_COLOR.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Constants.PRIMARY_COLOR);
            }
        });
    }

    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }

    public void resetFields() {
        nickNameField.setText("");
    }

    public void setLoginEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        nickNameField.setEnabled(enabled);
    }
}
