package com.kustacks.kuring.util.converter;

import com.kustacks.kuring.kuapi.notice.dto.response.CommonNoticeFormatDTO;
import com.kustacks.kuring.kuapi.notice.dto.response.KuisNoticeDTO;
import org.springframework.stereotype.Component;

@Component
public class KuisNoticeDTOToCommonFormatDTOConverter implements DTOConverter {

    @Override
    public Object convert(Object target) {

        KuisNoticeDTO kuisNoticeDTO = (KuisNoticeDTO) target;
        return CommonNoticeFormatDTO.builder()
                .articleId(kuisNoticeDTO.getArticleId())
                .postedDate(kuisNoticeDTO.getPostedDate())
                .updatedDate(null)
                .subject(kuisNoticeDTO.getSubject())
                .build();
    }
}
