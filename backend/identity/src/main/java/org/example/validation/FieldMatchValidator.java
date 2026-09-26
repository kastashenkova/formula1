package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String firstField;
    private String secondField;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        this.firstField = constraintAnnotation.first();
        this.secondField = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if (object == null) {
            return true;
        }
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(object);
        Object firstValue = beanWrapper.getPropertyValue(firstField);
        Object secondValue = beanWrapper.getPropertyValue(secondField);
        if (firstValue == null) {
            return secondValue == null;
        }
        return firstValue.equals(secondValue);
    }
}
