package com.codegym.kanflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice // Annotation báo cho Spring biết đây là một trình xử lý ngoại lệ toàn cục
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi 404 - Not Found.
     * Lỗi này xảy ra khi người dùng truy cập một URL không có controller nào xử lý.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNotFound(HttpServletRequest req, Exception ex) {
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("url", req.getRequestURL());
        modelAndView.addObject("message", "The page you are looking for does not exist.");
        return modelAndView;
    }

    /**
     * Xử lý lỗi chung (500 - Internal Server Error).
     * Bắt tất cả các loại Exception khác chưa được xử lý cụ thể.
     */
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(HttpServletRequest req, Exception ex) {
        // In lỗi ra console để dev có thể debug
        ex.printStackTrace();

        // Nếu request là một lời gọi API (URL bắt đầu bằng /api/)
        if (req.getRequestURI().startsWith("/api/")) {
            // Trả về một đối tượng JSON với thông báo lỗi
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred.",
                    ex.getMessage()
            );
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Nếu là request đến trang web thông thường, trả về trang lỗi 500
        ModelAndView modelAndView = new ModelAndView("error/500");
        modelAndView.addObject("message", "An unexpected error occurred. Please try again later.");
        modelAndView.addObject("error", ex.getClass().getSimpleName());
        return modelAndView;
    }

    // Một lớp nội bộ để biểu diễn cấu trúc lỗi JSON cho API
    private static class ErrorResponse {
        public int status;
        public String message;
        public String details;

        public ErrorResponse(int status, String message, String details) {
            this.status = status;
            this.message = message;
            this.details = details;
        }
    }


    /**
     * Xử lý lỗi 403 - Forbidden (Access Denied).
     * Lỗi này xảy ra khi một người dùng đã đăng nhập nhưng không có quyền
     * truy cập vào một tài nguyên cụ thể.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(HttpServletRequest req, AccessDeniedException ex) {
        // In lỗi ra console để dev có thể debug
        System.err.println("Access Denied: " + ex.getMessage());

        // Nếu request là một lời gọi API
        if (req.getRequestURI().startsWith("/api/")) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Access Denied",
                    "You do not have permission to perform this action."
            );
            return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        // Nếu là request đến trang web thông thường, trả về trang lỗi 403
        ModelAndView modelAndView = new ModelAndView("error/403");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

}