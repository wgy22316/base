package cn.mwee.base_common.utils.validator;

import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;


public final class MwParamValidatorUtil {

    private MwParamValidatorUtil() {
    }

    private final static Validator validator = Validation
            .byProvider(HibernateValidator.class)
            .configure()
            .buildValidatorFactory()
            .getValidator();

    public static <T> Map<String, ArrayList<String>> validate(T t, HashSet<String> skipFields) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);
        if (constraintViolations != null && constraintViolations.size() > 0) {
            Map<String, ArrayList<String>> mapErr = new HashMap<>();
            for (ConstraintViolation<T> constraintViolation : constraintViolations) {
                for (Path.Node node : constraintViolation.getPropertyPath()) {
                    String fieldName = node.getName();
                    if (skipFields == null || !skipFields.contains(fieldName)) {
                        ArrayList<String> lst = mapErr.get(fieldName);
                        if (lst == null) {
                            lst = new ArrayList<>();
                        }
                        lst.add(constraintViolation.getMessage());
                        mapErr.put(node.getName(), lst);
                    }
                }
            }
            return mapErr;
        }
        return null;
    }

    public static <T> Map<String, ArrayList<String>> validate(T t) {
        return validate(t, null);
    }
}
