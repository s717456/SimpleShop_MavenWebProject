package backEnd.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException webException) {
            return webException.getResponse();
        }

        Response.Status status = exception instanceof IllegalArgumentException
                ? Response.Status.BAD_REQUEST
                : Response.Status.INTERNAL_SERVER_ERROR;

        Message message = new Message(false, exception.getMessage() == null ? "系統錯誤" : exception.getMessage());
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(message)
                .build();
    }

    public static class Message {
        public boolean success;
        public String message;

        public Message() {
        }

        public Message(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
