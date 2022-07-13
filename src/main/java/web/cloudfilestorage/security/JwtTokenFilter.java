package web.cloudfilestorage.security;

import com.google.gson.Gson;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingResponseWrapper;
import web.cloudfilestorage.exceptions.JwtAuthenticationException;
import web.cloudfilestorage.validation.ValidationErrorResponse;
import web.cloudfilestorage.validation.Violation;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JwtTokenFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) servletRequest);

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);

                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            ContentCachingResponseWrapper responseCacheWrapperObject = new ContentCachingResponseWrapper(
                    (HttpServletResponse) servletResponse
            );
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (AuthenticationException | JwtAuthenticationException e) {
            // TODO: change to other type of error and divide by exceptions
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                    new Violation("Authorization", "Authorization failure!")
            );
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new Gson().toJson(errorResponse));
            response.getWriter().flush();
        }
    }
}
