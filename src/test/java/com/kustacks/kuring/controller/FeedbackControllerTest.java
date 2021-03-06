package com.kustacks.kuring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.kustacks.kuring.controller.dto.SaveFeedbackRequestDTO;
import com.kustacks.kuring.error.ErrorCode;
import com.kustacks.kuring.service.FeedbackServiceImpl;
import com.kustacks.kuring.service.FirebaseService;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.kustacks.kuring.ApiDocumentUtils.getDocumentRequest;
import static com.kustacks.kuring.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(FeedbackController.class)
public class FeedbackControllerTest {

//    @Rule
//    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @MockBean
    private FirebaseService firebaseService;

    @MockBean
    private FeedbackServiceImpl feedbackService;

    @Mock
    private FirebaseMessagingException firebaseMessagingException;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }
    
    @DisplayName("????????? ?????? API - ??????")
    @Test
    public void saveFeedbackSuccessTest() throws Exception {

        String token = "TEST_TOKEN";
        String content = "????????? ??????????????????.";

        SaveFeedbackRequestDTO requestDTO = SaveFeedbackRequestDTO.builder()
                .token(token)
                .content(content)
                .build();

        String requestBody = objectMapper.writeValueAsString(requestDTO);

        // given
        /*
            firebaseService.verifyToken ??? feedbackService.insertFeedback??? ?????? ??? void ??????.
         */

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/feedback")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(true))
                .andExpect(jsonPath("resultMsg").value("??????"))
                .andExpect(jsonPath("resultCode").value(201))
                .andDo(document("save-feedback-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("id").type(JsonFieldType.STRING).description("FCM ??????"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("????????? ??????. 5??? ?????? 256??? ??????")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("?????? ??????")
                        ))
                );
    }

    @DisplayName("????????? ?????? API - ?????? - ???????????? ?????? ??????")
    @Test
    public void saveFeedbackFailByInvalidTokenTest() throws Exception {

        String token = "INVALID_TOKEN";
        String content = "????????? ??????????????????.";

        SaveFeedbackRequestDTO requestDTO = SaveFeedbackRequestDTO.builder()
                .token(token)
                .content(content)
                .build();

        String requestBody = objectMapper.writeValueAsString(requestDTO);

        // given
        doThrow(firebaseMessagingException).when(firebaseService).verifyToken(token);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/feedback")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_FB_INVALID_TOKEN.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_FB_INVALID_TOKEN.getHttpStatus().value()))
                .andDo(document("save-feedback-fail-invalid-token",
                        getDocumentRequest(),
                        getDocumentResponse())
                );
    }

    @DisplayName("????????? ?????? API - ?????? - ???????????? ?????? ??????")
    @Test
    public void saveFeedbackFailByInvalidContentLength() throws Exception {

        String token = "TEST_TOKEN";
        String content = "5?????????";

        SaveFeedbackRequestDTO requestDTO = SaveFeedbackRequestDTO.builder()
                .token(token)
                .content(content)
                .build();

        String requestBody = objectMapper.writeValueAsString(requestDTO);

        // given

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/feedback")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_FD_INVALID_CONTENT.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_FD_INVALID_CONTENT.getHttpStatus().value()))
                .andDo(document("save-feedback-fail-invalid-content-length",
                        getDocumentRequest(),
                        getDocumentResponse())
                );
    }
}
