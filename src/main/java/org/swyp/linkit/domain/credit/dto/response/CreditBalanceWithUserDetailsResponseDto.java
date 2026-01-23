package org.swyp.linkit.domain.credit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.linkit.domain.credit.dto.CreditWithUserDetailsDto;

@Getter
@AllArgsConstructor
@Schema(description = "크레딧 잔액 및 유저 정보 조회 응답")
public class CreditBalanceWithUserDetailsResponseDto {

    @Schema(description = "유저 식별자(ID)", example = "1")
    private Long userId;

    @Schema(description = "유저 프로필 이미지 url", example = "https://image-example")
    private String userProfileImageUrl;

    @Schema(description = "유저 닉네임", example = "홍길동")
    private String userNickname;

    @Schema(description = "현재 보유 크레딧 잔액", example = "5")
    private int creditBalance;

    public static CreditBalanceWithUserDetailsResponseDto from(CreditWithUserDetailsDto cd){
        return new CreditBalanceWithUserDetailsResponseDto(
                cd.getUserId(),
                cd.getUserProfileImageUrl(),
                cd.getUserNickname(),
                cd.getBalance()
        );
    }
}
