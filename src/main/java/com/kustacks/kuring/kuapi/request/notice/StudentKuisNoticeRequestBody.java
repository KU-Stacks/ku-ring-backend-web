package com.kustacks.kuring.kuapi.request.notice;

import org.springframework.stereotype.Component;

@Component
public class StudentKuisNoticeRequestBody extends KuisNoticeRequestBody {
    public StudentKuisNoticeRequestBody() {
        super("notice", "0000300003");
    }
}