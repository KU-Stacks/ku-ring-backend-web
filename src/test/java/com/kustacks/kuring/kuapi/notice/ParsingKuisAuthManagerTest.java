package com.kustacks.kuring.kuapi.notice;

import com.kustacks.kuring.config.JsonConfig;
import com.kustacks.kuring.config.RestConfig;
import com.kustacks.kuring.error.ErrorCode;
import com.kustacks.kuring.error.InternalLogicException;
import com.kustacks.kuring.kuapi.api.notice.KuisAuthManager;
import com.kustacks.kuring.kuapi.api.notice.ParsingKuisAuthManager;
import com.kustacks.kuring.kuapi.notice.dto.request.KuisLoginRequestBody;
import com.kustacks.kuring.util.encoder.RequestBodyEncoder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringJUnitConfig({
    ParsingKuisAuthManager.class, KuisLoginRequestBody.class, RequestBodyEncoder.class,
    RestConfig.class, JsonConfig.class
})
@TestPropertySource(locations = "classpath:test-constants.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ParsingKuisAuthManagerTest {

    @Value("${auth.login-url}")
    private String loginUrl;

    @Value("${auth.api-skeleton-producer-url}")
    private String apiSkeletonProducerUrl;

    @Value("${auth.session}")
    private String testCookie;

    RestTemplate restTemplate;

    private final KuisAuthManager kuisAuthManager;
    private final String apiSkeleton;
    private final String successResponseBody = "{\"_METADATA_\":{\"success\":true}}";
    private final String failResponseBody = "{\"ERRMSGINFO\":{\"ERRMSG\":\"?????????????????? ???????????? ?????? ???????????????. ??????????????? ??????????????? ?????? ?????? ??????????????? ???????????? ????????? ?????? ??????????????? ????????? ??? ??? ????????????.\",\"STATUSCODE\":-2000,\"ERRCODE\":\"?????????????????? ???????????? ?????? ???????????????. ??????????????? ??????????????? ?????? ?????? ??????????????? ???????????? ????????? ?????? ??????????????? ????????? ??? ??? ????????????.\"}}";
    private MockRestServiceServer server;

    public ParsingKuisAuthManagerTest(
            KuisAuthManager parsingKuisAuthManager,
            RestTemplate restTemplate,
            @Value("${auth.api-skeleton-file-path}") String apiSkeletonFilePath) throws IOException {

        this.kuisAuthManager = parsingKuisAuthManager;
        this.restTemplate = restTemplate;

        apiSkeleton = readApiSkeleton(apiSkeletonFilePath);
    }

    @AfterEach
    void setUpAfter() {
        kuisAuthManager.forceRenewing();
    }

    @BeforeEach
    void setUpBefore() {
        server = MockRestServiceServer.createServer(restTemplate);
        kuisAuthManager.forceRenewing();
    }

    @Test
    @Order(1)
    @DisplayName("?????? - ????????? ??? ??????ID ?????? ??????")
    void success() {

        // given
        server.expect(requestTo(apiSkeletonProducerUrl)).andRespond(withSuccess().body(apiSkeleton));
        server.expect(requestTo(loginUrl)).andExpect(method(HttpMethod.POST)).andRespond(withSuccess().body(successResponseBody));

        // when
        String sessionId = kuisAuthManager.getSessionId();

        // then
        server.verify();
        assertEquals(testCookie, sessionId);
    }

    @Test
    @Order(2)
    @DisplayName("?????? - ????????? ????????? ??????ID ??????")
    void successWithSessionCache() {

        // given
        server.expect(times(1), requestTo(apiSkeletonProducerUrl)).andRespond(withSuccess().body(apiSkeleton));
        server.expect(times(1), requestTo(loginUrl)).andExpect(method(HttpMethod.POST)).andRespond(withSuccess().body(successResponseBody));

        // when
        String sessionId = kuisAuthManager.getSessionId();
        String secondSessionId = kuisAuthManager.getSessionId();

        // then
        server.verify();
        assertEquals(testCookie, sessionId);
        assertEquals(testCookie, secondSessionId);
    }

    @Test
    @Order(3)
    @DisplayName("?????? - ?????? body??? ??????")
    void failByNoBody() {

        // given
        server.expect(requestTo(apiSkeletonProducerUrl)).andRespond(withSuccess().body(apiSkeleton));
        server.expect(requestTo(loginUrl)).andExpect(method(HttpMethod.POST)).andRespond(withSuccess());

        // when
        InternalLogicException e = assertThrows(InternalLogicException.class, kuisAuthManager::getSessionId);

        // then
        assertEquals(ErrorCode.KU_LOGIN_NO_RESPONSE_BODY, e.getErrorCode());
    }

    @Test
    @Order(4)
    @DisplayName("?????? - ?????? body??? success ???????????? ?????? (kuis ????????? ????????? ?????? or api skeleton ????????? ??????)")
    void failByNoSuccessStringInBody() {

        // given
        server.expect(requestTo(apiSkeletonProducerUrl)).andRespond(withSuccess().body(apiSkeleton));
        server.expect(requestTo(loginUrl)).andExpect(method(HttpMethod.POST)).andRespond(withSuccess().body(failResponseBody));

        // when
        InternalLogicException e = assertThrows(InternalLogicException.class, kuisAuthManager::getSessionId);

        // then
        assertEquals(ErrorCode.KU_LOGIN_BAD_RESPONSE, e.getErrorCode());
    }

    private String readApiSkeleton(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return resourceToString(resource.getInputStream());
    }

    private String resourceToString(InputStream inputStream) throws IOException {
        return FileCopyUtils.copyToString(new InputStreamReader(inputStream));
    }
}
