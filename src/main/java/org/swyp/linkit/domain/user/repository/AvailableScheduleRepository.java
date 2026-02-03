package org.swyp.linkit.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.user.entity.AvailableSchedule;

import java.util.List;
import java.util.Optional;

public interface AvailableScheduleRepository extends JpaRepository<AvailableSchedule, Long> {

    // 멘토의 주간 가능 일정 규칙 조회
    List<AvailableSchedule> findAllByUser_Id(Long userId);

    // 스케줄 ID와 사용자 ID로 조회 (권한 체크용)
    Optional<AvailableSchedule> findByIdAndUserId(Long id, Long userId);
}