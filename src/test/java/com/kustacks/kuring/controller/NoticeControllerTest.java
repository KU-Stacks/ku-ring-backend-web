package com.kustacks.kuring.controller;

import com.kustacks.kuring.controller.dto.NoticeDTO;
import com.kustacks.kuring.error.ErrorCode;
import com.kustacks.kuring.service.NoticeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static com.kustacks.kuring.ApiDocumentUtils.getDocumentRequest;
import static com.kustacks.kuring.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(NoticeController.class)
public class NoticeControllerTest {

    // gradle??? ???????????? ??????????????? ?????? ?????? ?????? ??????
//    @Rule
//    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    private MockMvc mockMvc;

    @MockBean
    private NoticeServiceImpl noticeService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final String articleId = "5cw2e1";
    private final String postedDate = "20211016";
    private final String subject = "[??????] 2021??? ?????? ?????? ??????";
    private final String categoryName = "bachelor";

    private String type;
    private int offset;
    private int max;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @DisplayName("?????? API - ??????")
    @Test
    public void getNoticesSuccessTest() throws Exception {

        type = "bch";
        offset = 0;
        max = 10;

        List<NoticeDTO> noticeDTOList = new LinkedList<>();
        noticeDTOList.add(NoticeDTO.builder()
                .articleId(articleId)
                .postedDate(postedDate)
                .subject(subject)
                .categoryName(categoryName)
                .build());

        given(noticeService.getNotices(categoryName, offset, max)).willReturn(noticeDTOList);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", type)
                .queryParam("offset", String.valueOf(offset))
                .queryParam("max", String.valueOf(max)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(true))
                .andExpect(jsonPath("resultMsg").value("??????"))
                .andExpect(jsonPath("resultCode").value(200))
                .andExpect(jsonPath("baseUrl").exists())
                .andExpect(jsonPath("noticeList").exists())
                .andDo(document("notice-success",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(
                                        parameterWithName("type").description("?????? ???????????? ?????????")
                                                .attributes(key("Constraints").value("bch, sch, emp, nat, stu, ind, nor, lib")),
                                        parameterWithName("offset").description("????????? ????????? ?????? ?????????")
                                                .attributes(key("Constraints").value("0 ????????? ??????")),
                                        parameterWithName("max").description("????????? ?????? ?????? ??????")
                                                .attributes(key("Constraints").value("1 ?????? 30 ????????? ??????"))
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                        fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("?????? ?????????"),
                                        fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("baseUrl").type(JsonFieldType.STRING).description("?????? ????????? ??? ?????? url??? ?????? ??????"),
                                        fieldWithPath("noticeList[].articleId").type(JsonFieldType.STRING).description("?????? ID"),
                                        fieldWithPath("noticeList[].postedDate").type(JsonFieldType.STRING).description("?????? ?????????"),
                                        fieldWithPath("noticeList[].subject").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("noticeList[].category").type(JsonFieldType.STRING).description("?????? ???????????????")
                                ))

                );
    }

    @DisplayName("?????? API - ?????? - ????????? ?????? ????????????")
    @Test
    public void getNoticesFailByInvalidTypeTest() throws Exception {
        type = "invalid-type";
        offset = 0;
        max = 20;

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", type)
                .queryParam("offset", String.valueOf(offset))
                .queryParam("max", String.valueOf(max)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_NOTICE_NOT_EXIST_CATEGORY.getMessage()))
                .andExpect(jsonPath("resultCode").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(document("notice-fail-invalid-category",
                        getDocumentRequest(),
                        getDocumentResponse())
                );
    }

    @DisplayName("?????? API - ?????? - ????????? offset ?????? max ???????????? ???")
    @Test
    public void getNoticesFailByInvalidOffsetTest() throws Exception {
        type = "bch";
        offset = -1;
        max = 20;

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", type)
                .queryParam("offset", String.valueOf(offset))
                .queryParam("max", String.valueOf(max)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_INVALID_PARAM.getMessage()))
                .andExpect(jsonPath("resultCode").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(document("notice-fail-invalid-param",
                        getDocumentRequest(),
                        getDocumentResponse())
                );
    }
}
