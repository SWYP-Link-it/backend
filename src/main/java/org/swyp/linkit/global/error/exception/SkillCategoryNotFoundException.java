package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SkillCategoryNotFoundException extends BusinessException {

  public SkillCategoryNotFoundException() {
    super(ErrorCode.SKILL_CATEGORY_NOT_FOUND);
  }

  public SkillCategoryNotFoundException(String message) {
    super(ErrorCode.SKILL_CATEGORY_NOT_FOUND, message);
  }
}