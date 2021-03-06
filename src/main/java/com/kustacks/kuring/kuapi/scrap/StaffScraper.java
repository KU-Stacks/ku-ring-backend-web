package com.kustacks.kuring.kuapi.scrap;

import com.kustacks.kuring.controller.dto.StaffDTO;
import com.kustacks.kuring.error.ErrorCode;
import com.kustacks.kuring.error.InternalLogicException;
import com.kustacks.kuring.kuapi.api.staff.StaffAPIClient;
import com.kustacks.kuring.kuapi.scrap.parser.HTMLParser;
import com.kustacks.kuring.kuapi.staff.deptinfo.DeptInfo;
import com.kustacks.kuring.kuapi.staff.deptinfo.real_estate.RealEstateDept;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class StaffScraper implements KuScraper<StaffDTO> {

    private final List<StaffAPIClient> staffAPIClients;
    private final List<HTMLParser> htmlParsers;

    public StaffScraper(List<HTMLParser> htmlParsers, List<StaffAPIClient> staffAPIClients) {

        this.staffAPIClients = staffAPIClients;
        this.htmlParsers = htmlParsers;
    }

    @Retryable(value = {InternalLogicException.class}, backoff = @Backoff(delay = RETRY_PERIOD))
    public List<StaffDTO> scrap(DeptInfo deptInfo) throws InternalLogicException {

        List<Document> documents = null;
        List<StaffDTO> staffDTOList = new LinkedList<>();

        for (StaffAPIClient staffAPIClient : staffAPIClients) {
            if(staffAPIClient.support(deptInfo)) {
                log.info("{} HTML 요청", deptInfo.getDeptName());
                documents = staffAPIClient.getHTML(deptInfo);
                log.info("{} HTML 수신", deptInfo.getDeptName());
            }
        }

        // 수신한 documents HTML 파싱
        List<String[]> parseResult = new LinkedList<>();
        for (HTMLParser htmlParser : htmlParsers) {
            if(htmlParser.support(deptInfo)) {
                log.info("{} HTML 파싱 시작", deptInfo.getDeptName());
                for (Document document : documents) {
                    parseResult.addAll(htmlParser.parse(document));
                }
                log.info("{} HTML 파싱 완료", deptInfo.getDeptName());
            }
        }

        // 파싱 결과를 staffDTO로 변환
        for (String[] oneStaffInfo : parseResult) {
            staffDTOList.add(StaffDTO.builder()
                    .name(oneStaffInfo[0])
                    .major(oneStaffInfo[1])
                    .lab(oneStaffInfo[2])
                    .phone(oneStaffInfo[3])
                    .email(oneStaffInfo[4])
                    .deptName(deptInfo.getDeptName())
                    .collegeName(deptInfo.getCollegeName()).build());
        }

        if(staffDTOList.size() == 0) {
            throw new InternalLogicException(ErrorCode.STAFF_SCRAPER_CANNOT_SCRAP);
        }

        return staffDTOList;
    }
}
