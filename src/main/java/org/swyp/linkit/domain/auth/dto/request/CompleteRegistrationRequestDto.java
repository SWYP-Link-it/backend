package org.swyp.linkit.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원가입 완료 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteRegistrationRequestDto {

    @Schema(description = "닉네임", example = "코딩왕", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하여야 합니다.")
    @Pattern(
            regexp = "^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ0-9]+$",
            message = "닉네임은 영어, 한글, 숫자만 입력 가능합니다. (띄어쓰기 불가)"
    )
    private String nickname;

    @Schema(description = "프로필 이미지 URL (선택사항)", example = "https://example.com/profile.jpg")
    private String profileImageUrl;
}