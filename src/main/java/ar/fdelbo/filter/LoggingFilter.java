package ar.fdelbo.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {

        final var initTime = System.currentTimeMillis();
        final var cachedReq = new CachedBodyHttpServletRequest((HttpServletRequest) servletRequest);
        final var cachedResp = new ContentCachingResponseWrapper((HttpServletResponse) servletResponse);

        logRequest(cachedReq);
        filterChain.doFilter(cachedReq, cachedResp);
        logResponse(cachedResp, initTime);
    }

    private void logRequest(final CachedBodyHttpServletRequest request) {
        final var body = request.getCachedBody();
        final String req = new String(body, StandardCharsets.UTF_8);

        LOGGER.info("Incoming Req -> [{}] {} - Body: {}", request.getMethod(), request.getRequestURI(), req);
    }

    private void logResponse(final ContentCachingResponseWrapper response, final long initTime) throws IOException {
        try {
            final byte[] responseBody = response.getContentAsByteArray();
            final String res = new String(responseBody, StandardCharsets.UTF_8);
            final var finalTime = System.currentTimeMillis();

            LOGGER.info("Outgoing Res ({}ms) -> [{}] - Body: {}", finalTime - initTime, response.getStatus(), res);
        } finally {
            response.copyBodyToResponse();
        }
    }

    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            InputStream requestInputStream = request.getInputStream();
            this.cachedBody = IOUtils.toByteArray(requestInputStream);
        }

        public byte[] getCachedBody() {
            return cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CachedBodyServletInputStream(this.cachedBody);
        }
    }

    public static class CachedBodyServletInputStream extends ServletInputStream {

        private final InputStream cachedBodyInputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
        }


        @Override
        public boolean isFinished() {
            try {
                return cachedBodyInputStream.available() == 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            return cachedBodyInputStream.read();
        }
    }
}
