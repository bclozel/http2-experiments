package http2xp.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.PushBuilder;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Http2Controller {

	@RequestMapping("/hello")
	@ResponseBody
	public ResponseEntity<String> hello(@RequestParam String name) {
		return ResponseEntity.ok()
				.body("Hello " + name);
	}

	@RequestMapping("/")
	public String index(PushBuilder pushBuilder) {
		Assert.notNull(pushBuilder, "pushBuilder should not be null");
		pushBuilder.path("/talks.json").push();
		return "index";
	}

}
