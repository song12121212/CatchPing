package catchping;

import javax.swing.*;

import catchping.GUIView.DrawingListener;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends JFrame {
    private LoginPanel loginPanel;
    private GUIView gameView;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String nickname;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private volatile boolean isConnected;
    private Map<String, Integer> playerScores;

    public Client() {
        setTitle("캐치마인드");
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        playerScores = new HashMap<>();
        
        initializeComponents();
        setupListeners();
    }

    private void initializeComponents() {
        // 로그인 패널 초기화
        loginPanel = new LoginPanel();
        
        // 게임 뷰 초기화
        gameView = new GUIView();
        
        // 메인 패널에 추가
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(gameView, "GAME");
        
        add(mainPanel);
    }

    private void setupListeners() {
        // 로그인 패널 리스너
        loginPanel.setLoginListener(nickname -> {
            if (connectToServer(nickname)) {
                this.nickname = nickname;
                cardLayout.show(mainPanel, "GAME");
                startMessageListener();
            }
        });

        // 게임 뷰 채팅 리스너
        gameView.setChatListener(message -> {
            sendMessage(Constants.CMD_CHAT + Constants.DELIMITER + message);
        });

        // 게임 뷰 그리기 리스너
        gameView.setDrawingListener(new GUIView.DrawingListener() {
            @Override
            public void onDrawing(Point start, Point end, Color color, int size, boolean isEraser) {
                String drawMsg = String.format("%s%s%d,%d%s%d,%d%s%d,%d,%d%s%d%s%b",
                    Constants.CMD_DRAW,
                    Constants.DELIMITER,
                    start.x, start.y,
                    Constants.DELIMITER,
                    end.x, end.y,
                    Constants.DELIMITER,
                    color.getRed(), color.getGreen(), color.getBlue(),
                    Constants.DELIMITER,
                    size,  // 크기 정보 추가
                    Constants.DELIMITER,
                    isEraser);
                sendMessage(drawMsg);
            }

            @Override
            public void onClearCanvas() {
                sendMessage(Constants.CMD_CLEAR);
            }
        });

        // 윈도우 종료 이벤트
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                disconnect();
            }
        });
        gameView.setReadyListener(() -> {
            sendMessage(Constants.CMD_READY);
        });
    }

    private boolean connectToServer(String nickname) {
        try {
            socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // 닉네임 전송
            writer.println(nickname);
            isConnected = true;
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "서버 연결에 실패했습니다: " + e.getMessage(),
                "연결 오류",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message;
                while (isConnected && (message = reader.readLine()) != null) {
                    processMessage(message);
                }
            } catch (IOException e) {
                if (isConnected) {
                    handleDisconnection("서버와의 연결이 끊어졌습니다: " + e.getMessage());
                }
            }
        }).start();
    }

    private void processMessage(String message) {
        String[] parts = message.split(Constants.DELIMITER);
        String command = parts[0];

        SwingUtilities.invokeLater(() -> {
            switch (command) {
                case "PLAYERS":
                    updatePlayers(parts);
                    break;
                    
                case Constants.CMD_CHAT:
                    gameView.addChatMessage(parts[1]);
                    break;
                    
                case Constants.CMD_START:
                    boolean isDrawer = Boolean.parseBoolean(parts[3]);
                    handleGameStart(parts[1], Integer.parseInt(parts[2]), isDrawer);
                    break;
                    
                case Constants.CMD_DRAW:
                    handleDrawing(parts);
                    break;
                    
                case Constants.CMD_CLEAR:
                    gameView.clearCanvas();
                    break;
                    
                case Constants.CMD_TIMER:
                    updateTimer(Integer.parseInt(parts[1]));
                    break;
                    
                case Constants.CMD_GAME_OVER:
                    handleGameOver();
                    break;
                case Constants.CMD_READY_STATUS:
                    for (int i = 1; i < parts.length; i++) {
                        String[] playerInfo = parts[i].split(",");
                        String playerName = playerInfo[0];
                        boolean ready = playerInfo[1].equals("1");
                        gameView.updatePlayerReadyStatus(playerName, ready);
                    }
                    break;
                case Constants.CMD_ROUND:
                    int round = Integer.parseInt(parts[1]);
                    gameView.updateRound(round);
                    break;
                    
            }
        });
    }
 // Client.java에 메소드 추가
    private void handleGameStart(String word, int timeLeft, boolean isDrawer) {
        SwingUtilities.invokeLater(() -> {
            gameView.updateGameInfo(word, timeLeft, isDrawer);
 
        });
    }
    private void updatePlayers(String[] playerData) {
        playerScores.clear();
        for (int i = 1; i < playerData.length; i++) {
            String[] playerInfo = playerData[i].split(",");
            String playerName = playerInfo[0];
            int score = Integer.parseInt(playerInfo[1]);
            playerScores.put(playerName, score);
            
            // GUIView의 플레이어 정보 업데이트
            gameView.updatePlayerInfo(i - 1, playerName, score);
        }
    }

   

    private void handleDrawing(String[] drawData) {
        if (drawData.length == 6) {  // 크기 정보가 추가되어 6개의 데이터가 됨
            try {
                // 좌표 파싱
                Point start = new Point(
                    Integer.parseInt(drawData[1].split(",")[0]),
                    Integer.parseInt(drawData[1].split(",")[1])
                );
                Point end = new Point(
                    Integer.parseInt(drawData[2].split(",")[0]),
                    Integer.parseInt(drawData[2].split(",")[1])
                );
                
                // 색상 파싱
                String[] colorParts = drawData[3].split(",");
                Color color = new Color(
                    Integer.parseInt(colorParts[0]),
                    Integer.parseInt(colorParts[1]),
                    Integer.parseInt(colorParts[2])
                );
                
                int size = Integer.parseInt(drawData[4]);  // 크기 정보 파싱
                boolean isEraser = Boolean.parseBoolean(drawData[5]);
                
                // 크기 정보를 포함하여 그리기
                gameView.drawOnCanvas(start, end, color, size, isEraser);
            } catch (Exception e) {
                System.out.println("그리기 데이터 처리 오류: " + e.getMessage());
            }
        }
    }

 // updateTimer 메소드도 수정
    private void updateTimer(int timeLeft) {
        SwingUtilities.invokeLater(() -> {
            gameView.updateTimer(timeLeft);  // GUIView에 새로운 메소드 추가
        });
    }
    private void handleGameOver() {
        StringBuilder resultMessage = new StringBuilder("게임 종료!\n\n최종 점수:\n");
        playerScores.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> 
                resultMessage.append(entry.getKey())
                           .append(": ")
                           .append(entry.getValue())
                           .append("점\n"));
        
        JOptionPane.showMessageDialog(this, resultMessage.toString(), "게임 종료", JOptionPane.INFORMATION_MESSAGE);
        
        // 게임 관련 데이터 초기화
        playerScores.clear();
        gameView.resetGame();  // GUIView에 새로운 메소드 추가 필요
      
    }

    private void handleDisconnection(String message) {
        isConnected = false;
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "연결 오류", JOptionPane.ERROR_MESSAGE);
            cardLayout.show(mainPanel, "LOGIN");
            loginPanel.resetFields();
        });
    }

    private void disconnect() {
        isConnected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("연결 종료 중 오류: " + e.getMessage());
        }
    }

    private void sendMessage(String message) {
        if (isConnected && writer != null) {
            writer.println(message);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Client().setVisible(true);
        });
    }
}