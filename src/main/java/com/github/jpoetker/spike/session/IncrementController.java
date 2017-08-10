package com.github.jpoetker.spike.session;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IncrementController {
    private static final Logger log = LoggerFactory.getLogger(IncrementController.class);

    @RequestMapping("/increment")
    public Map<String, Object> increment(HttpSession session) {
        // uuid is not whitelisted
        log.info("uuid {}", session.getAttribute("uuid"));

        Long increment = (Long) session.getAttribute("increment");

        increment = (increment == null) ? new Long(1) : new Long(increment.longValue() + 1L);
        LocalDate updatedDate = LocalDate.now();

        session.setAttribute("increment", increment);
        session.setAttribute("updatedDate", updatedDate);
        session.setAttribute("uuid", UUID.randomUUID());

        Map<String, Object> result = new HashMap<>();
        result.put("increment", increment);
        result.put("updatedDate", updatedDate);

        return result;

    }
}
