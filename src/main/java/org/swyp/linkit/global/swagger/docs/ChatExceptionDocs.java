package org.swyp.linkit.global.swagger.docs;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.swagger.annotation.ExceptionDoc;
import org.swyp.linkit.global.swagger.exception.SwaggerExampleExceptions;

/**
 * 채팅(Chat) 도메인 API 예외 문서
 */
@ExceptionDoc
public class ChatExceptionDocs {

    public static class UnauthorizedException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.UNAUTHORIZED;
        }
    }

    public static class ChatRoomNotFoundException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.CHAT_ROOM_NOT_FOUND;
        }
    }

    public static class ChatMessageNotFoundException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.CHAT_MESSAGE_NOT_FOUND;
        }
    }

    public static class ChatNotParticipantException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.CHAT_NOT_PARTICIPANT;
        }
    }

    public static class ChatInvalidMessageException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.CHAT_INVALID_MESSAGE;
        }
    }

    public static class ChatInvalidUserException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.CHAT_INVALID_USER;
        }
    }

    public static class ChatSameUserException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.CHAT_SAME_USER;
        }
    }
}
