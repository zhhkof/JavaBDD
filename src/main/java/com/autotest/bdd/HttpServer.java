package com.autotest.bdd;

import org.apache.http.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class HttpServer {

    private static final Logger logger = Logger.getLogger(HttpServer.class);

    private static final String DEBUG_PREFIX = "[DEBUG] ";

    private static final String ERROR_PREFIX = "[ERROR] ";

    private static final String WARN_PREFIX = "[WARN] ";

    private static RequestListenerThread thread;

    public static void start() throws IOException {
        thread = new RequestListenerThread(8099);
        thread.start();
    }

    public static void stop() {
        thread.interrupt();
    }

    static class FileHandler implements HttpRequestHandler {

        public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            String method = request.getRequestLine().getMethod().toUpperCase();
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            
            String target = request.getRequestLine().getUri();
            int index = target.indexOf('?');
            if (index != -1) {
                target = target.substring(0, index);
            }
            
            InputStream in = this.getClass().getResourceAsStream(target);
            if (in != null) {
                response.setStatusCode(HttpStatus.SC_OK);
                
                InputStreamEntity entity = new InputStreamEntity(in, -1);
                response.setEntity(entity);
            } else {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            }
        }

    }

    static class LoggerHandler implements HttpRequestHandler {

        public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                String content = new String(EntityUtils.toByteArray(entity), "UTF-8");
                if (content.startsWith(DEBUG_PREFIX)) {
                    logger.debug(content.substring(DEBUG_PREFIX.length()));
                } else if (content.startsWith(WARN_PREFIX)) {
                    logger.warn(content.substring(WARN_PREFIX.length()));
                } else if (content.startsWith(ERROR_PREFIX)) {
                    logger.error(content.substring(ERROR_PREFIX.length()));
                } else {
                    logger.info(content);
                }
            }
            
            response.setStatusCode(HttpStatus.SC_OK);
            
            // CORS
            if (request.containsHeader("Origin")) {
                response.setHeader("Access-Control-Allow-Origin", "*");
            }
        }

    }

    static class RequestListenerThread extends Thread {

        private final ServerSocket serverSocket;

        private final HttpParams httpParams;

        private final HttpService httpService;

        public RequestListenerThread(int port) throws IOException {
            this.serverSocket = new ServerSocket(port);
            this.httpParams = new SyncBasicHttpParams();
            this.httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000).setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false).setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

            // Set up the HTTP protocol processor
            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] { new ResponseDate(), new ResponseServer(), new ResponseContent(), new ResponseConnControl() });

            // Set up request handlers
            HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
            registry.register("/logger", new LoggerHandler());
            registry.register("*", new FileHandler());

            // Set up the HTTP service
            this.httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory(), registry, this.httpParams);
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Socket socket = serverSocket.accept();

                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    conn.bind(socket, httpParams);

                    Thread t = new WorkerThread(httpService, conn);
                    t.setDaemon(true);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    static class WorkerThread extends Thread {

        private final HttpService httpService;

        private final HttpServerConnection conn;

        public WorkerThread(final HttpService httpservice, final HttpServerConnection conn) {
            super();
            this.httpService = httpservice;
            this.conn = conn;
        }

        @Override
        public void run() {
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && conn.isOpen()) {
                    httpService.handleRequest(conn, context);
                }
            } catch (SocketException ignore) {} catch (SocketTimeoutException ignore) {} catch (ConnectionClosedException ignore) {} catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    conn.shutdown();
                } catch (IOException ignore) {
                	ignore.printStackTrace();
                }
            }
        }

    }

}
