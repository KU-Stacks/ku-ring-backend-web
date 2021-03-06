package com.kustacks.kuring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.TopicManagementResponse;
import com.kustacks.kuring.controller.dto.AdminMessageDTO;
import com.kustacks.kuring.controller.dto.NoticeDTO;
import com.kustacks.kuring.controller.dto.NoticeMessageDTO;
import com.kustacks.kuring.error.ErrorCode;
import com.kustacks.kuring.error.InternalLogicException;
import com.kustacks.kuring.kuapi.CategoryName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FirebaseService {

    @Value("${notice.normal-base-url}")
    private String normalBaseUrl;

    @Value("${notice.library-base-url}")
    private String libraryBaseUrl;

    @Value("${server.deploy.environment}")
    private String deployEnv;

    private final String DEV_SUFFIX = ".dev";

    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    FirebaseService(ObjectMapper objectMapper, @Value("${firebase.file-path}") String filePath) throws IOException {

        this.objectMapper = objectMapper;

        ClassPathResource resource = new ClassPathResource(filePath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build();

        FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
        this.firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
    }

    public void verifyToken(String token) throws FirebaseMessagingException {

        Message message = Message.builder()
                .setToken(token)
                .build();

        firebaseMessaging.send(message);
    }

    public void subscribe(String token, String topic) throws FirebaseMessagingException, InternalLogicException {

        ArrayList<String> tokens = new ArrayList<>(1);
        tokens.add(token);

        if(deployEnv.equals("dev")) {
            topic = topic + DEV_SUFFIX;
        }

        TopicManagementResponse response = firebaseMessaging.subscribeToTopic(tokens, topic);

        if(response.getFailureCount() > 0) {
            throw new InternalLogicException(ErrorCode.FB_FAIL_SUBSCRIBE);
        }
    }

    public void unsubscribe(String token, String topic) throws FirebaseMessagingException, InternalLogicException {

        ArrayList<String> tokens = new ArrayList<>(1);
        tokens.add(token);

        if(deployEnv.equals("dev")) {
            topic = topic + DEV_SUFFIX;
        }

        TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(tokens, topic);

        if(response.getFailureCount() > 0) {
            throw new InternalLogicException(ErrorCode.FB_FAIL_UNSUBSCRIBE);
        }
    }


    /**
     * Firebase message?????? ??? ?????? paylaad??? ????????????.
     * 1. notification
     * 2. data
     *
     * notification??? Message??? ????????? ????????? ????????? ????????? title, body??? ?????? ??? noti??? ??????.
     * data??? Message??? ????????? ????????? ????????? ??? ???????????????(Andriod)??? ?????????, ????????? ??? ?????? ???????????? ?????? ??? ??????.
     *
     * ????????? ????????? putData??? ???????????? ?????????, ?????????????????? ?????? ????????? ????????? ?????????.
     *
     * @param messageDTO
     * @throws FirebaseMessagingException
     */

    public void sendMessage(NoticeMessageDTO messageDTO) throws FirebaseMessagingException {

        Map<String, String> noticeMap = objectMapper.convertValue(messageDTO, Map.class);

        StringBuilder topic = new StringBuilder(messageDTO.getCategory());
        if(deployEnv.equals("dev")) {
            topic.append(DEV_SUFFIX);
        }

        Message newMessage = Message.builder()
                .putAllData(noticeMap)
                .setTopic(topic.toString())
                .build();

        firebaseMessaging.send(newMessage);
    }

    public void sendMessage(List<NoticeMessageDTO> messageDTOList) throws FirebaseMessagingException {
        for (NoticeMessageDTO messageDTO : messageDTOList) {
            sendMessage(messageDTO);
        }
    }

    public void sendMessage(String token, NoticeMessageDTO messageDTO) throws FirebaseMessagingException {

        Map<String, String> messageMap = objectMapper.convertValue(messageDTO, Map.class);

        Message newMessage = Message.builder()
                .putAllData(messageMap)
                .setToken(token)
                .build();

        firebaseMessaging.send(newMessage);
    }

    public void sendMessage(String token, AdminMessageDTO messageDTO) throws FirebaseMessagingException {

        Map<String, String> messageMap = objectMapper.convertValue(messageDTO, Map.class);

        Message newMessage = Message.builder()
                .putAllData(messageMap)
                .setToken(token)
                .build();

        firebaseMessaging.send(newMessage);
    }
}
