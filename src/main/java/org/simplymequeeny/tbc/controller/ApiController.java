package org.simplymequeeny.tbc.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.simplymequeeny.tbc.service.PushNotificationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class ApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    PushNotificationsService pushNotificationsService;

    @RequestMapping(value = "/ny-times-best-sellers/send", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> send() throws JSONException {
        try {
            // TODO call api service
            JSONObject body = new JSONObject();
            body.put("to", "/topics/ny-times-best-sellers");
            body.put("priority", "high");

            JSONObject notification = new JSONObject();
            notification.put("title", "The Book Club Notification");
            notification.put("body", "NY Times Latest Best Sellers");

            JSONObject data = new JSONObject(getData());
            LOGGER.debug("DATA", data.toString());
            body.put("notification", notification);
            //body.put("data", data);
            LOGGER.debug("body", body.toString());

            HttpEntity<String> request = new HttpEntity<>(body.toString());

            CompletableFuture<String> pushNotification = pushNotificationsService.send(request);
            CompletableFuture.allOf(pushNotification).join();

            String firebaseResponse = pushNotification.get();
            return new ResponseEntity<>(firebaseResponse, HttpStatus.OK);
        }
        catch (IOException | ParseException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("Push Notification ERROR!", HttpStatus.BAD_REQUEST);
    }

    private String getData() throws IOException, ParseException {
        org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
        Resource resource = new ClassPathResource("data.json");
        org.json.simple.JSONObject data = (org.json.simple.JSONObject)parser.parse(new FileReader(resource.getFile()));
        return data.toJSONString();
    }
}
