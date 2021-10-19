package com.kustacks.kuring.kuapi.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kustacks.kuring.domain.category.Category;
import com.kustacks.kuring.domain.notice.Notice;
import lombok.Getter;

import java.util.List;

@Getter
public class LibraryNoticeDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("seqNo")
    private String seqNo;

    @JsonProperty("bulletinCategory")
    private String bulletinCategory;

    @JsonProperty("bulletinTextHead")
    private String bulletinTextHead;

    @JsonProperty("bulletinState")
    private String bulletinState;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    @JsonProperty("title")
    private String title;

    @JsonProperty("writer")
    private String writer;

    @JsonProperty("dateCreated")
    private String dateCreated;

    @JsonProperty("lastUpdated")
    private String lastUpdated;

    @JsonProperty("hitCnt")
    private String hitCnt;

    @JsonProperty("replyCnt")
    private String replyCnt;

    @JsonProperty("commentCnt")
    private String commentCnt;

//    @JsonProperty("attachmentCnt")
//    private List<String> attachmentCnt;
//
//    @JsonProperty("attachments")
//    private List<String> attachments;

    @JsonProperty("isPersonal")
    private boolean isPersonal;

    @JsonProperty("likeCount")
    private int likeCount;

    @JsonProperty("isMyLike")
    private boolean isMyLike;

    public Notice toEntity(Category category) {
        return Notice.builder()
                .articleId(this.id)
                .postedDate(this.dateCreated)
                .updatedDate(this.lastUpdated)
                .subject(this.title)
                .category(category)
                .build();
    }
}