package com.example.simple_ai_project.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

	@Value("${prox.app.passwordPattern}")
	private String passwordPattern;

	private Pattern pattern;

	@Override
	public void initialize(ValidPassword constraintAnnotation) {
		pattern = Pattern.compile(passwordPattern);
	}

	@Override
	public boolean isValid(String password, ConstraintValidatorContext context) {
		return password != null && pattern.matcher(password).matches();
	}
}
