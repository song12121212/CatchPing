package catchping;

public class Constants {
    // 네트워크 관련 상수
    public static final int SERVER_PORT = 1000;
    public static final String SERVER_IP = "localhost";
    public static final int MAX_PLAYERS = 4;

    // 게임 시작 관련 상수 추가
    public static final int MIN_PLAYERS = 2;  // 최소 플레이어 수 추가
    
    // 프로토콜 관련 상수
    public static final String DELIMITER = "//";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_CHAT = "CHAT";
    public static final String CMD_DRAW = "DRAW";
    public static final String CMD_CLEAR = "CLEAR";
    public static final String CMD_START = "START";
    public static final String CMD_GAME_OVER = "GAME_OVER";
    public static final String CMD_ANSWER = "ANSWER";
    public static final String CMD_TIMER = "TIMER";
    public static final String CMD_NEXT_TURN = "NEXT_TURN";
    public static final String CMD_READY = "READY";           // 준비 상태 변경 명령어 추가
    public static final String CMD_READY_STATUS = "READY_STATUS";  // 준비 상태 업데이트 명령어 추가
    public static final String CMD_ROUND = "ROUND";
    // GUI 크기 관련 상수
    public static final int FRAME_WIDTH = 1200;
    public static final int FRAME_HEIGHT = 800;
    public static final int CHAT_PANEL_WIDTH = 300;
    public static final int TOOL_PANEL_HEIGHT = 100;
    public static final int PLAYER_PANEL_HEIGHT = 80;
    public static final int INFO_PANEL_HEIGHT = 50;
    
    // GUI 컬러 관련 상수
    public static final java.awt.Color BACKGROUND_COLOR = new java.awt.Color(255, 255, 255);
    public static final java.awt.Color PRIMARY_COLOR = new java.awt.Color(52, 152, 219);  // 파란색
    public static final java.awt.Color SECONDARY_COLOR = new java.awt.Color(231, 76, 60); // 빨간색
    public static final java.awt.Color ACCENT_COLOR = new java.awt.Color(46, 204, 113);   // 초록색
    public static final java.awt.Color TEXT_COLOR = new java.awt.Color(44, 62, 80);       // 진한 회색
    
    // 게임 관련 상수
    public static final int GAME_TIME_SECONDS = 60;
    public static final int TOTAL_ROUNDS = 10;
    public static final int POINTS_FOR_CORRECT_ANSWER = 10;
    // 드로잉 관련 상수
    public static final int[] PEN_SIZES = {10, 20, 30};          // 펜 크기 배열         // 펜 크기 배열
    public static final int[] ERASER_SIZES = {10, 20, 30};    // 지우개 크기 배열
    public static final java.awt.Color[] DRAWING_COLORS = {    // 그리기 색상 배열
    	    java.awt.Color.BLACK,
    	    java.awt.Color.RED,
    	    java.awt.Color.BLUE,
    	    new java.awt.Color(0, 0, 128),  // 남색 추가 (RGB: 0, 0, 128)
    	    java.awt.Color.GREEN,
    	    java.awt.Color.YELLOW,
    	    java.awt.Color.ORANGE,
    	    java.awt.Color.PINK,
    	    java.awt.Color.MAGENTA
    	};

    // 폰트 관련 상수
    public static final String DEFAULT_FONT = "맑은 고딕";
    public static final int TITLE_FONT_SIZE = 32;
    public static final int SUBTITLE_FONT_SIZE = 16;
    public static final int NORMAL_FONT_SIZE = 14;
    
    // 메시지 관련 상수
    public static final String MSG_WAITING_PLAYERS = "다른 플레이어를 기다리는 중...";
    public static final String MSG_GAME_STARTING = "게임이 곧 시작됩니다!";
    public static final String MSG_YOUR_TURN = "당신의 차례입니다. 제시어: ";
    public static final String MSG_CORRECT_ANSWER = "정답을 맞추셨습니다!";
    public static final String MSG_WRONG_ANSWER = "틀렸습니다. 다시 시도해보세요.";
    public static final String MSG_ROUND_OVER = "라운드가 종료되었습니다.";
    public static final String MSG_GAME_OVER = "게임이 종료되었습니다.";
}