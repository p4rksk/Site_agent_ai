package com.siteagent.backend.chatlog;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.siteagent.backend.site.Site;
import com.siteagent.backend.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_log_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ChatLog(Site site, User user, String question, String answer) {
        this.site = site;
        this.user = user;
        this.question = question;
        this.answer = answer;
    }
}