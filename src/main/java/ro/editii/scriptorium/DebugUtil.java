package ro.editii.scriptorium;

import lombok.extern.java.Log;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

@Log
public class DebugUtil {

    public static String logHttpRequestHeaders(HttpServletRequest request, UriComponentsBuilder uriComponentsBuilder) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("\n******************");
        buffer.append(request.toString());
        buffer.append("******************");
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            buffer.append("\n").append(name).append("=");
            Enumeration<String> headers = request.getHeaders(name);
            if (headers == null || ! headers.hasMoreElements())
                continue;
            ArrayList<String> list = Collections.list(headers);
            buffer.append(String.join(",",list));
            while (headers.hasMoreElements()) {
                final String crtHeader = headers.nextElement();
                buffer.append(crtHeader);
            }
        }

        buffer.append("\n======= body ====\n");
        buffer.append("\n > getServletPath() :" + request.getServletPath());
        buffer.append("\n >  getRequestURI() :" + request.getRequestURI());
        buffer.append("\n >  getRemoteHost() :" + request.getRemoteHost());
        buffer.append("\n >  getRemotePort() :" + request.getRemotePort());
        buffer.append("\n >  getScheme()     :" + request.getScheme());
        buffer.append("\n >  from param ucb() :" + uriComponentsBuilder.cloneBuilder()
                .path("/pulea/calului")
                .build()
                .toUriString());

        UriComponentsBuilder ucb2 = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request));
        buffer.append("\n >  from param ucb2() :" + ucb2
                .path("/pulea/calului")
                .build()
                .toUriString());

        final String respText = buffer.toString();
        log.info(respText);
        return respText;
    }
}
