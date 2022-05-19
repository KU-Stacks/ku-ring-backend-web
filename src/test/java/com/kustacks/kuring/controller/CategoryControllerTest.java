package com.kustacks.kuring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.kustacks.kuring.controller.dto.CategoryHierarchyDTO;
import com.kustacks.kuring.controller.dto.SubscribeCategoriesRequestDTO;
import com.kustacks.kuring.controller.dto.CategoryNameInfoDTO;
import com.kustacks.kuring.persistence.category.Category;
import com.kustacks.kuring.persistence.user.User;
import com.kustacks.kuring.persistence.user_category.UserCategory;
import com.kustacks.kuring.error.APIException;
import com.kustacks.kuring.error.ErrorCode;
import com.kustacks.kuring.error.InternalLogicException;
import com.kustacks.kuring.service.CategoryServiceImpl;
import com.kustacks.kuring.service.FirebaseService;
import com.kustacks.kuring.service.UserServiceImpl;
import com.kustacks.kuring.util.converter.DTOConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.kustacks.kuring.ApiDocumentUtils.getDocumentRequest;
import static com.kustacks.kuring.ApiDocumentUtils.getDocumentResponse;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasValue;
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
    // gradle을 기반으로 디렉토리로 자동 구성 하는 역할
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

    @DisplayName("서버에서 제공하는 공지 카테고리 목록 제공 API - 성공")
    @Test
    public void getSubscribableCategoriesSuccessTest() throws Exception {

        String requestGroup = "kuis";
        List<CategoryHierarchyDTO> subCategories = new LinkedList<>();
        CategoryHierarchyDTO bachelor = new CategoryHierarchyDTO("bachelor", "학사", "bch");
        CategoryHierarchyDTO scholarship = new CategoryHierarchyDTO("scholarship", "장학", "sch");
        CategoryHierarchyDTO employment = new CategoryHierarchyDTO("employment", "취창업", "emp");
        CategoryHierarchyDTO national = new CategoryHierarchyDTO("national", "국제", "nat");
        CategoryHierarchyDTO student = new CategoryHierarchyDTO("student", "학생", "stu");
        CategoryHierarchyDTO industryUniversity = new CategoryHierarchyDTO("industry_university", "산학", "ind");
        CategoryHierarchyDTO normal = new CategoryHierarchyDTO("normal", "일반", "nor");
        CategoryHierarchyDTO library = new CategoryHierarchyDTO("library", "도서관", "lib");
        subCategories.add(bachelor);
        subCategories.add(scholarship);
        subCategories.add(employment);
        subCategories.add(national);
        subCategories.add(student);
        subCategories.add(industryUniversity);
        subCategories.add(normal);
        subCategories.add(library);

        // given
        given(categoryService.getSubscribableCategories(requestGroup)).willReturn(subCategories);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice/categories")
                        .queryParam("group", requestGroup)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(true))
                .andExpect(jsonPath("resultMsg").value("성공"))
                .andExpect(jsonPath("resultCode").value(200))
                .andExpect(jsonPath("type").value(requestGroup))
                .andExpect(jsonPath("categories", hasSize(8)))
                .andExpect(jsonPath("categories[0].name").value(bachelor.getName()))
                .andExpect(jsonPath("categories[0].korName").value(bachelor.getKorName()))
                .andExpect(jsonPath("categories[0].shortName").value(bachelor.getShortName()))
                .andExpect(jsonPath("categories[1].name").value(scholarship.getName()))
                .andExpect(jsonPath("categories[1].korName").value(scholarship.getKorName()))
                .andExpect(jsonPath("categories[1].shortName").value(scholarship.getShortName()))
                .andExpect(jsonPath("categories[2].name").value(employment.getName()))
                .andExpect(jsonPath("categories[2].korName").value(employment.getKorName()))
                .andExpect(jsonPath("categories[2].shortName").value(employment.getShortName()))
                .andExpect(jsonPath("categories[3].name").value(national.getName()))
                .andExpect(jsonPath("categories[3].korName").value(national.getKorName()))
                .andExpect(jsonPath("categories[3].shortName").value(national.getShortName()))
                .andExpect(jsonPath("categories[4].name").value(student.getName()))
                .andExpect(jsonPath("categories[4].korName").value(student.getKorName()))
                .andExpect(jsonPath("categories[4].shortName").value(student.getShortName()))
                .andExpect(jsonPath("categories[5].name").value(industryUniversity.getName()))
                .andExpect(jsonPath("categories[5].korName").value(industryUniversity.getKorName()))
                .andExpect(jsonPath("categories[5].shortName").value(industryUniversity.getShortName()))
                .andExpect(jsonPath("categories[6].name").value(normal.getName()))
                .andExpect(jsonPath("categories[6].korName").value(normal.getKorName()))
                .andExpect(jsonPath("categories[6].shortName").value(normal.getShortName()))
                .andExpect(jsonPath("categories[7].name").value(library.getName()))
                .andExpect(jsonPath("categories[7].korName").value(library.getKorName()))
                .andExpect(jsonPath("categories[7].shortName").value(library.getShortName()))
                .andDo(document("category-get-subscribable-categories-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("group").description("카테고리 그룹 이름")
                                        .attributes(key("Constraints").value("kuis, major"))
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("결과 메세지"),
                                fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("결과 코드"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("최상위 카테고리"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("서버에서 지원하는 공지 카테고리 목록"),
                                subsectionWithPath("categories[].name").type(JsonFieldType.STRING).description("구독 가능한 카테고리의 영문이름"),
                                subsectionWithPath("categories[].korName").type(JsonFieldType.STRING).description("구독 가능한 카테고리의 국문이름"),
                                subsectionWithPath("categories[].shortName").type(JsonFieldType.STRING).description("구독 가능한 카테고리의 짧은이름")
                        ))
                );
    }

    @DisplayName("특정 회원이 구독한 카테고리 목록 제공 API - 성공")
    @Test
    public void getUserCategoriesSuccessTest() throws Exception {
        String token = "TEST_TOKEN";

        List<Category> categories = new LinkedList<>();
        categories.add(Category.builder().name("bachelor").build());
        categories.add(Category.builder().name("employment").build());

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
                .andExpect(jsonPath("resultMsg").value("성공"))
                .andExpect(jsonPath("resultCode").value(200))
                .andExpect(jsonPath("categories", hasSize(2)))
                .andExpect(jsonPath("categories[0]").value(categoryNames.get(0)))
                .andExpect(jsonPath("categories[1]").value(categoryNames.get(1)))
                .andDo(document("category-get-user-categories-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("id").description("유효한 FCM 토큰")
                                        .attributes(key("Constraints").value(""))
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("결과 메세지"),
                                fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("결과 코드"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("해당 회원이 구독한 카테고리 목록")
                        ))
                );
    }

    @DisplayName("특정 회원이 구독한 카테고리 목록 제공 API - 실패 - 유효하지 않은 FCM 토큰")
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

    @DisplayName("특정 회원이 구독한 카테고리 목록 제공 API - 실패 - 필수 파라미터 누락")
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

    @DisplayName("특정 회원의 구독 카테고리 편집 API - 성공")
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
                .andExpect(jsonPath("resultMsg").value("성공"))
                .andExpect(jsonPath("resultCode").value(201))
                .andDo(document("category-subscribe-categories-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("id").type(JsonFieldType.STRING).description("FCM 토큰"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("알림을 받을 공지 카테고리 목록")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("결과 메세지"),
                                fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("결과 코드")
                        ))
                );
    }

    @DisplayName("특정 회원의 구독 카테고리 편집 API - 실패 - 유효하지 않은 토큰")
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

    @DisplayName("특정 회원의 구독 카테고리 편집 API - 실패 - 요청 body에 필수 json 필드 누락")
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

    @DisplayName("특정 회원의 구독 카테고리 편집 API - 실패 - 서버에서 지원하지 않는 카테고리를 수신")
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

    @DisplayName("특정 회원의 구독 카테고리 편집 API - 실패 - FCM 오류로 인한 구독 및 구독 취소 실패")
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
