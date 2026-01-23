package org.swyp.linkit.domain.credit.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.user.entity.User;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CreditWithUserDetailsDto {

    private Long userId;
    private String userProfileImageUrl;
    private String userNickname;
    private Long creditId;
    private int balance;

    public static CreditWithUserDetailsDto from(Credit credit, User user){
        return CreditWithUserDetailsDto.builder()
                .userId(user.getId())
                .userProfileImageUrl(user.getProfileImageUrl())
                .userNickname(user.getNickname())
                .creditId(credit.getId())
                .balance(credit.getBalance())
                .build();
    }
}
