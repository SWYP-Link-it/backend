package org.swyp.linkit.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

@Entity
@Table(name = "user_skill_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSkillImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_skill_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_skill_id", nullable = false)
    private UserSkill userSkill;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "image_order", nullable = false)
    private Integer imageOrder;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "file_size")
    private Long fileSize;

    @Builder(access = AccessLevel.PRIVATE)
    private UserSkillImage(UserSkill userSkill, String imageUrl, Integer imageOrder,
                           String originalFilename, Long fileSize) {
        this.userSkill = userSkill;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
    }

    public static UserSkillImage create(UserSkill userSkill, String imageUrl, Integer imageOrder,
                                        String originalFilename, Long fileSize) {
        return UserSkillImage.builder()
                .userSkill(userSkill)
                .imageUrl(imageUrl)
                .imageOrder(imageOrder)
                .originalFilename(originalFilename)
                .fileSize(fileSize)
                .build();
    }

    // 연관관계 설정
    protected void assignUserSkill(UserSkill userSkill) {
        this.userSkill = userSkill;
    }
}