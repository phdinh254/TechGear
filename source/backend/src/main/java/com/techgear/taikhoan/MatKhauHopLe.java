package com.techgear.taikhoan;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;

/** Mật khẩu 8–72 ký tự, có cả chữ và số; BCrypt chỉ nhận tối đa 72 byte nên chữ có dấu tính theo byte UTF-8. */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatKhauHopLe.KiemTra.class)
public @interface MatKhauHopLe {

    String message() default "Mật khẩu phải từ 8 đến 72 ký tự, có cả chữ và số";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class KiemTra implements ConstraintValidator<MatKhauHopLe, String> {
        @Override
        public boolean isValid(String s, ConstraintValidatorContext context) {
            return s != null && s.length() >= 8 && s.getBytes(StandardCharsets.UTF_8).length <= 72
                    && s.codePoints().anyMatch(Character::isLetter) && s.codePoints().anyMatch(Character::isDigit);
        }
    }
}
