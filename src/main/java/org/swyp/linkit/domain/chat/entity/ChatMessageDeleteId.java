package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChatMessageDeleteId implements Serializable {

    @Column(name = "chat_message_id")
    private Long chatMessageId;

    @Column(name = "user_id")
    private Long userId;
}