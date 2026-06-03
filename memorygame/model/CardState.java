package memorygame.model;

/**
 * Định nghĩa các trạng thái có thể có của một thẻ bài
 */
public enum CardState {
    FACE_DOWN, // Thẻ đang úp
    FACE_UP,   // Thẻ đang mở (chờ kiểm tra)
    MATCHED    // Thẻ đã được ghép đúng
}