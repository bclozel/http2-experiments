package http2xp.web;

import org.eclipse.jetty.server.Dispatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class Http2Controller {

    @RequestMapping("/hello")
    @ResponseBody
    public ResponseEntity<String> hello(@RequestParam String name) {
        return ResponseEntity.ok()
                .body("Hello " + name);
    }

    @RequestMapping("/")
    public String index(HttpServletRequest request) {
        Dispatcher dispatcher = (Dispatcher) request.getRequestDispatcher("/talks.json");
        dispatcher.push(request);

        return "index";
    }

}
