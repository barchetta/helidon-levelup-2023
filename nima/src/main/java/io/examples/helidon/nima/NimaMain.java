package io.examples.helidon.nima;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.common.http.Http;
import io.helidon.common.http.InternalServerException;
import io.helidon.nima.webclient.http1.Http1Client;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRouting;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

public class NimaMain {
    private static final Http.HeaderValue SERVER = Http.Header.create(Http.Header.SERVER, "Nima");
    private static final AtomicInteger COUNTER = new AtomicInteger();

    /**
     * Random generator to simulate remote service. No need to use secure random to
     * compute sleep times
     */
    private static final Random RANDOM = new Random();

    /**
     * Main method for this service.
     *
     * @param args CLI args
     */
    public static void main(String[] args) {
        // Create and start webserver
        WebServer ws = WebServer.builder()
                .routing(NimaMain::routing)
                .start();

        // Create client for BlockingService to use
        BlockingService.client(Http1Client.builder()
                .baseUri("http://localhost:" + ws.port())
                .build());
    }

    /**
     * Routing adds:
     * (1) filter to add {@link #SERVER} to all responses
     * (2) a "/remote" service to be accessed by {@link BlockingService}
     * (3) the {@link BlockingService} itself.
     *
     * @param rules routing rules to update
     */
    static void routing(HttpRouting.Builder rules) {
        rules.addFilter((chain, req, res) -> {
                    res.header(SERVER);
                    chain.proceed();
                })
                .get("/remote", NimaMain::remote)
                .register("/", new BlockingService());
    }

    /**
     * Simulates a "remote" service accessed using {@link BlockingService}.
     * The implementation randomly sleeps for up to half a second to
     * simulate some business logic.
     *
     * @param req the request
     * @param res the response
     */
    private static void remote(ServerRequest req, ServerResponse res) {
        int sleepMillis = RANDOM.nextInt(500);
        int counter = COUNTER.incrementAndGet();

        try {
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        } catch (InterruptedException e) {
            throw new InternalServerException("Failed to sleep", e);
        }
        res.send("remote_" + counter);
    }
}
