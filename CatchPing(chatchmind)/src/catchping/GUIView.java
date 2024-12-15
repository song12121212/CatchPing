package catchping;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class GUIView extends JPanel {
	private JButton readyButton;
	private boolean isReady = false;
	private Image gameImage;
	private int currentRound = 1;
    // GUI 컴포넌트들
    private DrawingPanel drawingPanel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JPanel toolPanel;
    private JPanel playerPanel;
    private JPanel gameInfoPanel;
    
    // 플레이어 정보
    private JLabel[] playerNames;
    private JLabel[] playerScores;
    
    // 게임 정보
    private JLabel roundLabel;
    private JLabel wordLabel;
    private JLabel timerLabel;
    private JLabel roleLabel;
    
    // 현재 선택된 그리기 도구 상태
    private Color currentColor = Color.BLACK;
    private int currentPenSize = Constants.PEN_SIZES[0];
    private boolean isEraser = false;
    private boolean isCurrentDrawer = false;  // 클래스 상단에 필드 추가
    // 이벤트 리스너
    private ChatListener chatListener;
    private DrawingListener drawingListener;
    public void updateRound(int round) {
        this.currentRound = round;
        updateGameInfo();
    }
    // 인터페이스 정의
    public interface ChatListener {
        void onMessageSent(String message);
    }

    public interface DrawingListener {
        void onDrawing(Point start, Point end, Color color, int size, boolean isEraser);
        void onClearCanvas();
    }

    public GUIView() {
        setLayout(new BorderLayout());
        initializeComponents();
        gameImage = new ImageIcon("images/game1.jpg").getImage();
        
        
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 배경 이미지 그리기
        if (gameImage != null) {
            g.drawImage(gameImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
 // getCurrentRound 메소드 추가
    public int getCurrentRound() {
        return currentRound;
    }
    private void updateGameInfo() {
        roundLabel.setText("라운드: " + currentRound + "/" + Constants.TOTAL_ROUNDS);
    }
    private void initializeComponents() {
    	
        // 플레이어 패널 초기화
        initPlayerPanel();
        
        // 중앙 패널 (그리기 + 채팅)
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // 채팅 패널 초기화
        initChatPanel();
        
        // 그리기 패널 초기화
        initDrawingPanel();
        
        // 도구 패널 초기화
        initToolPanel();
        
        // 게임 정보 패널 초기화
        initGameInfoPanel();
     // 기존 컴포넌트들의 배경을 투명하게 설정
        setOpaque(false);  // 이 패널 자체를 투명하게
        
        // 각 패널들도 투명하게 설정
        playerPanel.setOpaque(false);
        toolPanel.setOpaque(false);
        gameInfoPanel.setOpaque(false);
        // DrawingPanel은 흰색 배경 유지 (그리기를 위해)
        drawingPanel.setOpaque(true);
        drawingPanel.setBackground(Color.WHITE);
        // 레이아웃 구성
        
        centerPanel.add(drawingPanel, BorderLayout.CENTER);
        centerPanel.add(toolPanel, BorderLayout.SOUTH);
        centerPanel.setOpaque(false);
        add(playerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(gameInfoPanel, BorderLayout.SOUTH);
        
    }
    

    private void initPlayerPanel() {
        playerPanel = new JPanel(new GridLayout(2, 4, 10, 5));
        playerPanel.setPreferredSize(new Dimension(Constants.FRAME_WIDTH, Constants.PLAYER_PANEL_HEIGHT));
        playerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        playerNames = new JLabel[Constants.MAX_PLAYERS];
        playerScores = new JLabel[Constants.MAX_PLAYERS];
        
        for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
            playerNames[i] = new JLabel("player" + i);  // or you could assign the player's actual name
            playerScores[i] = new JLabel("0점");
            stylePlayerLabel(playerNames[i]);  // Apply styling to player label
            stylePlayerLabel(playerScores[i]); // Apply styling to player score label
            playerPanel.add(playerNames[i]);
            playerPanel.add(playerScores[i]);
        }

    }

    private void initChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(Constants.CHAT_PANEL_WIDTH, 0));

        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 10px 패딩 추가
        // 채팅 영역
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font(Constants.DEFAULT_FONT, Font.PLAIN, Constants.NORMAL_FONT_SIZE));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        
        // 메시지 입력 영역
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setFont(new Font(Constants.DEFAULT_FONT, Font.PLAIN, Constants.NORMAL_FONT_SIZE));
        sendButton = new JButton("전송");
        styleSendButton(sendButton);
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // 채팅 이벤트 설정
        setupChatEvents();
        
        add(chatPanel, BorderLayout.WEST);
    }

    private void initDrawingPanel() {
        drawingPanel = new DrawingPanel();
        drawingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20)); 
    }

    private void initToolPanel() {
        toolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));  // 간격 좀 더 넓게
        toolPanel.setPreferredSize(new Dimension(0, Constants.TOOL_PANEL_HEIGHT));
        toolPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. 펜 크기 패널
        JPanel penSizePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        penSizePanel.setOpaque(false);
        for (int size : Constants.PEN_SIZES) {
            JButton penButton = new JButton(size + "px");
            styleToolButton(penButton);
            penButton.addActionListener(e -> {
                currentPenSize = size;
                isEraser = false;
            });
            penSizePanel.add(penButton);
        }
        toolPanel.add(penSizePanel);

        // 구분선
        toolPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // 2. 색상 패널
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        colorPanel.setOpaque(false);
        for (Color color : Constants.DRAWING_COLORS) {
            JButton colorButton = new JButton();
            try {
                String colorName = getColorName(color);
                ImageIcon icon = new ImageIcon("colors/" + colorName + ".png");
                Image img = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                colorButton.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                colorButton.setBackground(color);
            }
            colorButton.setPreferredSize(new Dimension(40, 40));
            colorButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            colorButton.addActionListener(e -> {
                currentColor = color;
                isEraser = false;
            });
            colorPanel.add(colorButton);
        }
        toolPanel.add(colorPanel);

        // 구분선
        toolPanel.add(new JSeparator(SwingConstants.VERTICAL));

     // 3. 지우개 패널
        JPanel eraserPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        eraserPanel.setOpaque(false);

        // 지우개 아이콘 추가
        JLabel eraserIcon = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("erases/eraser.png");  // 지우개 아이콘 이미지
            Image img = icon.getImage().getScaledInstance(40,50, Image.SCALE_SMOOTH);
            eraserIcon.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            eraserIcon.setText("지우개");  // 이미지 로드 실패시 텍스트로 표시
        }
        eraserPanel.add(eraserIcon);

        // 지우개 크기 버튼들
        for (int size : Constants.ERASER_SIZES) {
            JButton eraserButton = new JButton("지우개 " + size + "px");
            styleToolButton(eraserButton);
            eraserButton.addActionListener(e -> {
                currentPenSize = size;
                isEraser = true;
            });
            eraserPanel.add(eraserButton);
        }

        // 전체 지우기 버튼 (기존 코드 유지)
        JButton clearButton = new JButton();
        try {
            ImageIcon icon = new ImageIcon("erases/allDelete.png"); 
            Image img = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            clearButton.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            clearButton.setText("전체 지우기");
        }
        clearButton.setPreferredSize(new Dimension(40, 40));
        clearButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        clearButton.setToolTipText("전체 지우기");
        clearButton.addActionListener(e -> {
            if (drawingListener != null) {
                drawingListener.onClearCanvas();
            }
        });
        eraserPanel.add(clearButton);

        toolPanel.add(eraserPanel);
    }
    // 색상 이름 반환 메소드
    private String getColorName(Color color) {
        if (color.equals(Color.BLACK)) return "black";
        if (color.equals(Color.RED)) return "red";
        if (color.equals(Color.BLUE)) return "blue";
        if (color.equals(new Color(0, 0, 128))) return "navy"; // 남색 추가
        if (color.equals(Color.GREEN)) return "green";
        if (color.equals(Color.YELLOW)) return "yellow";
        if (color.equals(Color.ORANGE)) return "orange";
        if (color.equals(Color.PINK)) return "pink";
        if (color.equals(Color.MAGENTA)) return "magenta";
        return "default";
    }
    private void initGameInfoPanel() {
    	 gameInfoPanel = new JPanel(new GridLayout(1, 5, 10, 0));  // 5칸으로 변경
        gameInfoPanel.setPreferredSize(new Dimension(Constants.FRAME_WIDTH, Constants.INFO_PANEL_HEIGHT));
        gameInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        roundLabel = new JLabel("라운드: 1/" + Constants.TOTAL_ROUNDS);
        wordLabel = new JLabel("제시어: ?????");
        timerLabel = new JLabel("남은 시간: " + Constants.GAME_TIME_SECONDS + "초");
        roleLabel = new JLabel("대기 중");  // 새로운 라벨 추가
        styleInfoLabel(roundLabel);
        styleInfoLabel(wordLabel);
        styleInfoLabel(timerLabel);
        
        readyButton = new JButton("게임 준비");
        styleReadyButton(readyButton);
        readyButton.addActionListener(e -> {
            isReady = !isReady;
            readyButton.setText(isReady ? "준비 완료" : "게임 준비");
            readyButton.setBackground(isReady ? Constants.ACCENT_COLOR : Constants.PRIMARY_COLOR);
            if (readyListener != null) {
                readyListener.onReadyStatusChanged();
            }
        });
        
        
        styleInfoLabel(roleLabel);
        
        gameInfoPanel.add(roundLabel);
        gameInfoPanel.add(wordLabel);
        gameInfoPanel.add(timerLabel);
        gameInfoPanel.add(roleLabel);    // 역할 표시 라벨
        gameInfoPanel.add(readyButton);
    }
    private void styleReadyButton(JButton button) {
        button.setFont(new Font(Constants.DEFAULT_FONT, Font.BOLD, Constants.NORMAL_FONT_SIZE));
        button.setForeground(Color.WHITE);
        button.setBackground(Constants.PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }
 // 레디 상태 리스너 인터페이스
    public interface ReadyListener {
        void onReadyStatusChanged();
    }

    private ReadyListener readyListener;

    public void setReadyListener(ReadyListener listener) {
        this.readyListener = listener;
    }
    public void updateTimer(int timeLeft) {
        timerLabel.setText("남은 시간: " + timeLeft + "초");
    }
    public void updatePlayerReadyStatus(String playerName, boolean ready) {
        // 플레이어 이름 옆에 준비 상태 표시
        for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
            if (playerNames[i].getText().equals(playerName)) {
                playerNames[i].setText(playerName + (ready ? " (준비)" : ""));
                break;
            }
        }
    }
    
    public void updateGameInfo(String word, int timeLeft, boolean isDrawer) {
        this.isCurrentDrawer = isDrawer;  // 출제자 여부 저장
        String role = isDrawer ? "[출제자]" : "[정답자]";
        String wordDisplay = isDrawer ? "제시어: " + word : "제시어: ?????";
        
        roundLabel.setText("라운드: " + currentRound + "/" + Constants.TOTAL_ROUNDS);
        wordLabel.setText(wordDisplay);
        timerLabel.setText("남은 시간: " + timeLeft + "초");
        roleLabel.setText(role);
        
        // 그리기 도구와 채팅 활성화/비활성화
        setDrawingToolsEnabled(isDrawer);
        messageField.setEnabled(!isDrawer);
        sendButton.setEnabled(!isDrawer);
        
        // 디버그 출력 추가
        System.out.println("현재 출제자 여부: " + isDrawer);
        System.out.println("그리기 권한 상태: " + drawingPanel.isEnabled());
    }


    private void setDrawingToolsEnabled(boolean enabled) {
        // 모든 그리기 도구 버튼들의 활성화 상태 설정
        for (Component c : toolPanel.getComponents()) {
            if (c instanceof JButton) {
                c.setEnabled(enabled);
            }
        }
        drawingPanel.setEnabled(enabled);
    }
    public void drawOnCanvas(Point start, Point end, Color color, int penSize, boolean isEraser) {
        Graphics2D g2d = (Graphics2D) drawingPanel.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 그리기 모드 또는 지우기 모드
        if (isEraser) {
            g2d.setColor(Color.WHITE);
        } else {
            g2d.setColor(color);
        }
        
        // 전달받은 penSize 사용
        g2d.setStroke(new BasicStroke(penSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // 선 그리기
        g2d.drawLine(start.x, start.y, end.x, end.y);
        g2d.dispose();
    }

    public void resetGame() {
        // 게임 정보 초기화
        currentRound = 1;
        isCurrentDrawer = false;
        
        // 캔버스 초기화
        clearCanvas();
        
        // 라벨 초기화
        roundLabel.setText("라운드: 1/" + Constants.TOTAL_ROUNDS);
        wordLabel.setText("제시어: ?????");
        timerLabel.setText("남은 시간: " + Constants.GAME_TIME_SECONDS + "초");
        roleLabel.setText("대기 중");
        
        // 준비 상태 초기화
        isReady = false;
        readyButton.setText("게임 준비");
        readyButton.setBackground(Constants.PRIMARY_COLOR);
        
        // 플레이어 정보 초기화
        for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
            playerNames[i].setText("player" + i);
            playerScores[i].setText("0점");
        }
        
        // 채팅창 초기화
        chatArea.setText("");
        
        // 그리기 도구 초기화
        currentColor = Color.BLACK;
        currentPenSize = Constants.PEN_SIZES[0];
        isEraser = false;
        
        // 입력 필드 활성화
        messageField.setEnabled(true);
        sendButton.setEnabled(true);
    }
    // 캔버스 초기화 메소드
    public void clearCanvas() {
        Graphics g = drawingPanel.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, drawingPanel.getWidth(), drawingPanel.getHeight());
        g.dispose();
    }
    // 그리기 패널 내부 클래스
    class DrawingPanel extends JPanel {
        private Point startPoint;
        public DrawingPanel() {  // 생성자 추가
            setBackground(Color.WHITE);
            setupDrawingEvents();
        }
        private void setupDrawingEvents() {
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!isCurrentDrawer) {  // 출제자가 아니면 리턴
                        return;
                    }
                    startPoint = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!isCurrentDrawer) {  // 출제자가 아니면 리턴
                        return;
                    }
                    if (startPoint != null && drawingListener != null) {
                        Point endPoint = e.getPoint();
                        drawingListener.onDrawing(startPoint, endPoint, 
                            isEraser ? Color.WHITE : currentColor, 
                            currentPenSize,  // currentPenSize 전달
                            isEraser);
                        startPoint = endPoint;
                    }
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }
    }

    // 스타일링 메소드들
    private void stylePlayerLabel(JLabel label) {
        label.setFont(new Font(Constants.DEFAULT_FONT, Font.BOLD, Constants.NORMAL_FONT_SIZE));
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void styleInfoLabel(JLabel label) {
        label.setFont(new Font(Constants.DEFAULT_FONT, Font.BOLD, Constants.NORMAL_FONT_SIZE));
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void styleToolButton(JButton button) {
        button.setFont(new Font(Constants.DEFAULT_FONT, Font.PLAIN, Constants.NORMAL_FONT_SIZE));
    }

    private void styleSendButton(JButton button) {
        button.setBackground(Constants.PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    // 채팅 이벤트 설정
    private void setupChatEvents() {
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && chatListener != null) {
            chatListener.onMessageSent(message);
            messageField.setText("");
        }
    }

    // 외부에서 호출할 수 있는 메소드들
    public void addChatMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void updatePlayerInfo(int index, String name, int score) {
        if (index >= 0 && index < Constants.MAX_PLAYERS) {
            playerNames[index].setText(name);
            playerScores[index].setText(score + "점");
        }
    }



    // 리스너 설정 메소드들
    public void setChatListener(ChatListener listener) {
        this.chatListener = listener;
    }

    public void setDrawingListener(DrawingListener listener) {
        this.drawingListener = listener;
    }
}