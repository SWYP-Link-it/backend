package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SchedulesRequiredWithSkillsException extends BusinessException {

  public SchedulesRequiredWithSkillsException() {
    super(ErrorCode.SCHEDULES_REQUIRED_WITH_SKILLS);
  }

  public SchedulesRequiredWithSkillsException(String message) {
    super(ErrorCode.SCHEDULES_REQUIRED_WITH_SKILLS, message);
  }
}