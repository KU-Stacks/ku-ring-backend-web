package com.kustacks.kuring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.kustacks.kuring.controller.dto.SubscribeCategoriesRequestDTO;
import com.kustacks.kuring.domain.category.Category;
import com.kustacks.kuring.domain.user.User;
import com.kustacks.kuring.domain.user_category.UserCategory;
import com.kustacks.kuring.error.APIException;
import com.kustacks.kuring.error.ErrorCode;
import com.kustacks.kuring.error.InternalLogicException;
import com.kustacks.kuring.kuapi.user.UserUpdater;
import com.kustacks.kuring.service.CategoryServiceImpl;
import com.kustacks.kuring.service.FirebaseService;
import com.kustacks.kuring.service.UserServiceImpl;
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

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.kustacks.kuring.ApiDocumentUtils.getDocumentRequest;
import static com.kustacks.kuring.ApiDocumentUtils.getDocumentResponse;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {
    // gradle??? ???????????? ??????????????? ?????? ?????? ?????? ??????
//    @Rule
//    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    private MockMvc mockMvc;

    @MockBean
    private CategoryServiceImpl categoryService;

    @MockBean
    private FirebaseService firebaseService;

    @MockBean
    private UserServiceImpl userService;

    @Mock
    private FirebaseMessagingException firebaseMessagingException;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @DisplayName("???????????? ???????????? ?????? ???????????? ?????? ?????? API - ??????")
    @Test
    public void getSupportedCategoriesSuccessTest() throws Exception {

        List<Category> categories = new LinkedList<>();
        categories.add(new Category("bachelor"));
        categories.add(new Category("employment"));

        List<String> categoryNames = new LinkedList<>();
        categoryNames.add("bachelor");
        categoryNames.add("employment");

        // given
        given(categoryService.getCategories()).willReturn(categories);
        given(categoryService.getCategoryNamesFromCategories(categories)).willReturn(categoryNames);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice/categories")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(true))
                .andExpect(jsonPath("resultMsg").value("??????"))
                .andExpect(jsonPath("resultCode").value(200))
                .andExpect(jsonPath("categories", hasSize(2)))
                .andExpect(jsonPath("categories[0]").value(categoryNames.get(0)))
                .andExpect(jsonPath("categories[1]").value(categoryNames.get(1)))
                .andDo(document("category-get-all-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(),
                        responseFields(
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("???????????? ???????????? ?????? ???????????? ??????")
                        ))
                );
    }

    @DisplayName("?????? ????????? ????????? ???????????? ?????? ?????? API - ??????")
    @Test
    public void getUserCategoriesSuccessTest() throws Exception {
        String token = "TEST_TOKEN";

        List<Category> categories = new LinkedList<>();
        categories.add(new Category("bachelor"));
        categories.add(new Category("employment"));

        List<String> categoryNames = new LinkedList<>();
        categoryNames.add("bachelor");
        categoryNames.add("employment");

        // given
        given(categoryService.getUserCategories(token)).willReturn(categories);
        given(categoryService.getCategoryNamesFromCategories(categories)).willReturn(categoryNames);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice/subscribe")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("id", token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(true))
                .andExpect(jsonPath("resultMsg").value("??????"))
                .andExpect(jsonPath("resultCode").value(200))
                .andExpect(jsonPath("categories", hasSize(2)))
                .andExpect(jsonPath("categories[0]").value(categoryNames.get(0)))
                .andExpect(jsonPath("categories[1]").value(categoryNames.get(1)))
                .andDo(document("category-get-user-categories-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("id").description("????????? FCM ??????")
                                        .attributes(key("Constraints").value(""))
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("?????? ????????? ????????? ???????????? ??????")
                        ))
                );
    }

    @DisplayName("?????? ????????? ????????? ???????????? ?????? ?????? API - ?????? - ???????????? ?????? FCM ??????")
    @Test
    public void getUserCategoriesFailByInvalidTokenTest() throws Exception {
        String token = "INVALID_TOKEN";

        // given
        doThrow(new APIException(ErrorCode.API_FB_INVALID_TOKEN)).when(firebaseService).verifyToken(token);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice/subscribe")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("id", token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_FB_INVALID_TOKEN.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_FB_INVALID_TOKEN.getHttpStatus().value()))
                .andExpect(jsonPath("categories").doesNotExist())
                .andDo(document("category-get-user-categories-fail-invalid-token",
                        getDocumentRequest(),
                        getDocumentResponse()
                ));
    }

    @DisplayName("?????? ????????? ????????? ???????????? ?????? ?????? API - ?????? - ?????? ???????????? ??????")
    @Test
    public void getUserCategoriesFailByMissingParamTest() throws Exception {

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice/subscribe")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_MISSING_PARAM.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_MISSING_PARAM.getHttpStatus().value()))
                .andExpect(jsonPath("categories").doesNotExist())
                .andDo(document("category-get-user-categories-fail-missing-param",
                        getDocumentRequest(),
                        getDocumentResponse()
                ));
    }

    @DisplayName("?????? ????????? ?????? ???????????? ?????? API - ??????")
    @Test
    public void subscribeCategoriesSuccessTest() throws Exception {

        String token = "TEST_TOKEN";

        List<String> categories = new LinkedList<>();
        categories.add("bachelor");
        categories.add("student");

        SubscribeCategoriesRequestDTO requestDTO = new SubscribeCategoriesRequestDTO(token, categories);

        Map<String, List<UserCategory>> compareCategoriesResult = new HashMap<>();
        compareCategoriesResult.put("new", new LinkedList<>());
        compareCategoriesResult.put("remove", new LinkedList<>());

        Category bachelorCategory = Category.builder().name("bachelor").build();
        Category studentCategory = Category.builder().name("student").build();

        User user = User.builder()
                .token(token)
                .build();

        compareCategoriesResult.get("new").add(UserCategory.builder()
                .user(user)
                .category(bachelorCategory)
                .build());
        compareCategoriesResult.get("new").add(UserCategory.builder()
                .user(user)
                .category(studentCategory)
                .build());


        // given
        given(userService.getUserByToken(token)).willReturn(null);
        given(userService.insertUserToken(token)).willReturn(user);
        given(categoryService.compareCategories(categories, new LinkedList<>(), user)).willReturn(compareCategoriesResult);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/notice/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(true))
                .andExpect(jsonPath("resultMsg").value("??????"))
                .andExpect(jsonPath("resultCode").value(201))
                .andDo(document("category-subscribe-categories-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("id").type(JsonFieldType.STRING).description("FCM ??????"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("????????? ?????? ?????? ???????????? ??????")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("?????? ??????")
                        ))
                );
    }

    @DisplayName("?????? ????????? ?????? ???????????? ?????? API - ?????? - ???????????? ?????? ??????")
    @Test
    public void subscribeCategoriesFailByInvalidToken() throws Exception {

        String token = "INVALID_TOKEN";

        List<String> categories = new LinkedList<>();
        categories.add("bachelor");
        categories.add("student");

        SubscribeCategoriesRequestDTO requestDTO = new SubscribeCategoriesRequestDTO(token, categories);

        // given
        given(userService.getUserByToken(token)).willReturn(null);
        doThrow(firebaseMessagingException).when(firebaseService).verifyToken(token);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/notice/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_FB_INVALID_TOKEN.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_FB_INVALID_TOKEN.getHttpStatus().value()))
                .andDo(document("category-subscribe-categories-fail-invalid-token",
                        getDocumentRequest(),
                        getDocumentResponse())
                );
    }

    @DisplayName("?????? ????????? ?????? ???????????? ?????? API - ?????? - ?????? body??? ?????? json ?????? ??????")
    @Test
    public void subscribeCategoriesFailByMissingJsonField() throws Exception {

        String requestBody = "{\"categories\": [\"bachelor\", \"student\"]}";

        // given

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/notice/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_MISSING_PARAM.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_MISSING_PARAM.getHttpStatus().value()))
                .andDo(document("category-subscribe-categories-fail-missing-json-field",
                        getDocumentRequest(),
                        getDocumentResponse())
                );
    }

    @DisplayName("?????? ????????? ?????? ???????????? ?????? API - ?????? - ???????????? ???????????? ?????? ??????????????? ??????")
    @Test
    public void subscribeCategoriesFailByNotSupportedCategory() throws Exception {

        String token = "TEST_TOKEN";

        List<String> categories = new LinkedList<>();
        categories.add("bachelor");
        categories.add("invalid-category");

        SubscribeCategoriesRequestDTO requestDTO = new SubscribeCategoriesRequestDTO(token, categories);

        // given
        doThrow(new InternalLogicException(ErrorCode.CAT_NOT_EXIST_CATEGORY)).when(categoryService).verifyCategories(categories);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/notice/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_INVALID_PARAM.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_INVALID_PARAM.getHttpStatus().value()))
                .andDo(document("category-subscribe-categories-fail-not-supported-category",
                        getDocumentRequest(),
                        getDocumentResponse())
                );

    }

    @DisplayName("?????? ????????? ?????? ???????????? ?????? API - ?????? - FCM ????????? ?????? ?????? ??? ?????? ?????? ??????")
    @Test
    public void subscribeCategoriesFailByFCMError() throws Exception {

        String token = "TEST_TOKEN";

        List<String> categories = new LinkedList<>();
        categories.add("bachelor");
        categories.add("student");

        SubscribeCategoriesRequestDTO requestDTO = new SubscribeCategoriesRequestDTO(token, categories);

        Map<String, List<UserCategory>> compareCategoriesResult = new HashMap<>();
        compareCategoriesResult.put("new", new LinkedList<>());
        compareCategoriesResult.put("remove", new LinkedList<>());

        Category bachelorCategory = Category.builder().name("bachelor").build();
        Category studentCategory = Category.builder().name("student").build();

        User user = User.builder()
                .token(token)
                .build();

        compareCategoriesResult.get("new").add(UserCategory.builder()
                .user(user)
                .category(bachelorCategory)
                .build());
        compareCategoriesResult.get("new").add(UserCategory.builder()
                .user(user)
                .category(studentCategory)
                .build());


        // given
        given(categoryService.verifyCategories(categories)).willReturn(categories);
        given(userService.getUserByToken(token)).willReturn(null);
        given(userService.insertUserToken(token)).willReturn(user);
        given(categoryService.compareCategories(categories, new LinkedList<>(), user)).willReturn(compareCategoriesResult);
        doThrow(firebaseMessagingException).when(categoryService).updateUserCategory(token, compareCategoriesResult);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/notice/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_FB_CANNOT_EDIT_CATEGORY.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_FB_CANNOT_EDIT_CATEGORY.getHttpStatus().value()))
                .andDo(document("category-subscribe-categories-fail-firebase-error",
                        getDocumentRequest(),
                        getDocumentResponse())
                );
    }
}
