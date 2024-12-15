package catchping;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private ExecutorService executorService;
    private GameRoom gameRoom;
    private boolean isRunning;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        executorService = Executors.newCachedThreadPool();
        gameRoom = new GameRoom();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            isRunning = true;
            System.out.println("서버가 시작되었습니다. 포트: " + Constants.SERVER_PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                executorService.execute(clientHandler);
            }
        } catch (IOException e) {
            System.out.println("서버 에러: " + e.getMessage());
        }
    }

    // 게임룸 클래스 - 게임 로직 처리
    private class GameRoom {
    	 private boolean isGameRunning;
    	    private int currentRound;
    	    private String currentWord;
    	    private int currentDrawerIndex;
    	    private List<String> wordList;
    	    private Timer gameTimer;
    	    private int timeLeft;
    	    private Set<ClientHandler> readyPlayers;
    	    private static final int DRAWER_POINTS = 1;    // 출제자 점수
    	    private static final int GUESSER_POINTS = 2;   // 정답자 점수
    	    
    	    public GameRoom() {
    	        this.wordList = initializeWordList();
    	        this.currentRound = 0;
    	        this.currentDrawerIndex = 0;
    	        this.readyPlayers = new HashSet<>();
    	    }
        public void handleReady(ClientHandler client) {
            if (!isGameRunning) {
                if (readyPlayers.contains(client)) {
                    readyPlayers.remove(client);
                } else {
                    readyPlayers.add(client);
                }
                broadcastReadyStatus();
                
                // 모든 플레이어가 준비되었는지 확인
                if (readyPlayers.size() == clients.size() && clients.size() >= Constants.MIN_PLAYERS) {
                    startGame();
                }
            }
        }
        
        private void broadcastReadyStatus() {
            StringBuilder statusMsg = new StringBuilder(Constants.CMD_READY_STATUS);
            for (ClientHandler client : clients) {
                statusMsg.append(Constants.DELIMITER)
                        .append(client.getNickname())
                        .append(",")
                        .append(readyPlayers.contains(client) ? "1" : "0");
            }
            broadcastMessage(statusMsg.toString());
        }
     // 제시어 목록도 더 추가해봅시다
        private List<String> initializeWordList() {
            List<String> words = new ArrayList<>();
            words.addAll(Arrays.asList(
                "사과", "바나나", "컴퓨터", "전화기", "자동차",
                "비행기", "강아지", "고양이", "냉장고", "텔레비전",
                "피아노", "기타", "책상", "의자", "시계",
                "연필", "지우개", "가방", "학교", "병원",
                "경찰차", "소방차", "자전거", "태양", "달",
                "별", "구름", "나무", "꽃", "바다"
            ));
            Collections.shuffle(words);  // 단어 목록을 섞습니다
            return words;
        }

        public void startGame() {
            if (clients.size() >= Constants.MIN_PLAYERS) {
                isGameRunning = true;
                currentRound = 1;
                readyPlayers.clear();
                currentDrawerIndex = 0;  // 명시적으로 첫 출제자 설정
                Collections.shuffle(wordList);
                System.out.println("게임 시작! 현재 플레이어 수: " + clients.size()); // 디버그 로그
                startRound();
            }
        }
        private void startRound() {
            if (currentRound <= Constants.TOTAL_ROUNDS) {
                currentWord = wordList.get(currentRound - 1);
                timeLeft = Constants.GAME_TIME_SECONDS;
                broadcastGameState();
                startTimer();
            } else {
                endGame();
            }
        }

        private void startTimer() {
            if (gameTimer != null) {
                gameTimer.cancel();
            }
            
            gameTimer = new Timer();
            gameTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    timeLeft--;
                    broadcastMessage(Constants.CMD_TIMER + Constants.DELIMITER + timeLeft);
                    
                    if (timeLeft <= 0) {
                        nextRound();
                    }
                }
            }, 1000, 1000);
        }

        private void nextRound() {
            if (gameTimer != null) {
                gameTimer.cancel();
            }
            
            currentRound++;
            currentDrawerIndex = (currentDrawerIndex + 1) % clients.size();
            
            if (currentRound <= Constants.TOTAL_ROUNDS) {
                startRound();
            } else {
                endGame();
            }
        }

        private void endGame() {
            isGameRunning = false;
            
            // 게임 관련 변수들 초기화
            currentRound = 0;
            currentDrawerIndex = 0;
            currentWord = null;
            readyPlayers.clear();
            
            // 타이머 정리
            if (gameTimer != null) {
                gameTimer.cancel();
                gameTimer = null;
            }
            
            // 모든 플레이어의 점수 초기화
            for (ClientHandler client : clients) {
                client.resetScore(); // ClientHandler에 새로운 메소드 추가 필요
            }
            
            // 클라이언트들에게 게임 종료 메시지 전송
            broadcastMessage(Constants.CMD_GAME_OVER);
            
            // 플레이어 목록 업데이트 (점수 초기화 반영)
            updatePlayerList();
            
            // 새로운 단어 목록 준비
            wordList = initializeWordList();
        }

        public void checkAnswer(String answer, ClientHandler client) {
            if (isGameRunning && answer.trim().equals(currentWord) && 
                clients.indexOf(client) != currentDrawerIndex) {
                // 정답자에게 점수 부여
                client.addScore(GUESSER_POINTS);
                // 출제자에게도 점수 부여
                clients.get(currentDrawerIndex).addScore(DRAWER_POINTS);
                
                // 정답 맞춘 것을 알림
                broadcastMessage(Constants.CMD_CHAT + Constants.DELIMITER + 
                    client.getNickname() + "님이 정답을 맞추셨습니다! (+" + GUESSER_POINTS + "점)");
                broadcastMessage(Constants.CMD_CHAT + Constants.DELIMITER + 
                    clients.get(currentDrawerIndex).getNickname() + "님이 " + DRAWER_POINTS + "점을 획득했습니다!");
                
                // 플레이어 점수 업데이트
                updatePlayerList();
                
                // 다음 라운드로
                nextRound();
            }
        }
        private void broadcastGameState() {
            ClientHandler currentDrawer = clients.get(currentDrawerIndex);
            
            System.out.println("현재 출제자: " + currentDrawer.getNickname()); // 디버그 로그 추가
            
            // 모든 클라이언트에게 현재 출제자 알림
            broadcastMessage(Constants.CMD_CHAT + Constants.DELIMITER + 
                "현재 출제자는 " + currentDrawer.getNickname() + "님 입니다.");
            
            // 현재 라운드 정보 전송
            broadcastMessage(Constants.CMD_ROUND + Constants.DELIMITER + currentRound);
            
            // 출제자에게 제시어 전송
            currentDrawer.sendMessage(Constants.CMD_START + Constants.DELIMITER + 
                currentWord + Constants.DELIMITER + timeLeft + Constants.DELIMITER + "true");  // "true" 큰따옴표 확인
            
            // 다른 플레이어들에게는 "?????" 전송
            for (ClientHandler client : clients) {
                if (client != currentDrawer) {
                    client.sendMessage(Constants.CMD_START + Constants.DELIMITER + 
                        "?????" + Constants.DELIMITER + timeLeft + Constants.DELIMITER + "false");  // "false" 큰따옴표 확인
                }
            }
        }
    }

    // 클라이언트 핸들러 클래스
    private class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String nickname;
        private int score;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.score = 0;
        }
        public void resetScore() {
            this.score = 0;
        }

        @Override
        public void run() {
            try {
                // 첫 메시지로 닉네임 받기
                nickname = reader.readLine();
                broadcastMessage(Constants.CMD_CHAT + Constants.DELIMITER + 
                    nickname + "님이 입장하셨습니다.");
                updatePlayerList();

   
                

                String message;
                while ((message = reader.readLine()) != null) {
                    processMessage(message);
                }
            } catch (IOException e) {
                System.out.println("클라이언트 연결 에러: " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        private void processMessage(String message) {
            String[] parts = message.split(Constants.DELIMITER);
            String command = parts[0];

            switch (command) {
                case Constants.CMD_CHAT:
                    String chatMessage = parts[1];
                    gameRoom.checkAnswer(chatMessage, this);
                    broadcastMessage(Constants.CMD_CHAT + Constants.DELIMITER + 
                        nickname + ": " + chatMessage);
                    break;
                
                case Constants.CMD_DRAW:
                    broadcastMessage(message); // 그리기 데이터를 모든 클라이언트에게 전달
                    break;
                
                case Constants.CMD_CLEAR:
                    broadcastMessage(Constants.CMD_CLEAR);
                    break;
                case Constants.CMD_READY:
                    gameRoom.handleReady(this);
                    break;
            }
        }

        public void sendMessage(String message) {
            writer.println(message);
        }

        public String getNickname() {
            return nickname;
        }

        public int getScore() {
            return score;
        }

        public void addScore(int points) {
            score += points;
            updatePlayerList();
        }

        private void disconnect() {
            try {
                clients.remove(this);
                socket.close();
                broadcastMessage(Constants.CMD_CHAT + Constants.DELIMITER + 
                    nickname + "님이 퇴장하셨습니다.");
                updatePlayerList();
            } catch (IOException e) {
                System.out.println("클라이언트 연결 종료 에러: " + e.getMessage());
            }
        }
    }

    // 유틸리티 메소드
    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private void updatePlayerList() {
        StringBuilder playerInfo = new StringBuilder("PLAYERS");
        for (ClientHandler client : clients) {
            playerInfo.append(Constants.DELIMITER)
                     .append(client.getNickname())
                     .append(",")
                     .append(client.getScore());
        }
        broadcastMessage(playerInfo.toString());
    }

    // 메인 메소드
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}