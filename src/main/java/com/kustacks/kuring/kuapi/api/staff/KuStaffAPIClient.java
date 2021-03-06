package com.kustacks.kuring.kuapi.api.staff;

import com.kustacks.kuring.error.ErrorCode;
import com.kustacks.kuring.error.InternalLogicException;
import com.kustacks.kuring.kuapi.staff.deptinfo.DeptInfo;
import com.kustacks.kuring.kuapi.staff.deptinfo.art_design.CommunicationDesignDept;
import com.kustacks.kuring.kuapi.staff.deptinfo.art_design.LivingDesignDept;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class KuStaffAPIClient implements StaffAPIClient {

    private final Map<DeptInfo, String> urlMap;
    private final JsoupClient jsoupClient;

    public KuStaffAPIClient(
            @Value("${staff.living-design-url}") String livingDesignUrl,
            @Value("${staff.communication-design-url}") String communicationDesignUrl,
            DeptInfo communicationDesignDept,
            DeptInfo livingDesignDept,
            JsoupClient normalJsoupClient) {

        urlMap = new HashMap<>();
        urlMap.put(communicationDesignDept, communicationDesignUrl);
        urlMap.put(livingDesignDept, livingDesignUrl);

        this.jsoupClient = normalJsoupClient;
    }

    @Override
    public boolean support(DeptInfo deptInfo) {
        return deptInfo instanceof LivingDesignDept || deptInfo instanceof CommunicationDesignDept;
    }

    @Override
    public List<Document> getHTML(DeptInfo deptInfo) throws InternalLogicException {

        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(urlMap.get(deptInfo));
        String url = urlBuilder.toUriString();

        Document document;
        try {
            document = jsoupClient.get(url, SCRAP_TIMEOUT);
        } catch(IOException e) {
            throw new InternalLogicException(ErrorCode.STAFF_SCRAPER_CANNOT_SCRAP, e);
        }

        List<Document> documents = new LinkedList<>();
        documents.add(document);

        return documents;
    }
}
