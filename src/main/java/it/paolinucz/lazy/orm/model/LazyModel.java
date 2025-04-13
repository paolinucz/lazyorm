package it.paolinucz.lazy.orm.model;

import it.paolinucz.lazy.orm.annotations.LazyMap;
import it.paolinucz.lazy.orm.annotations.Mapped;
import it.paolinucz.lazy.orm.exceptions.LazyMapperException;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public abstract class LazyModel {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ObjectInfo {
        private Class<?> type;
        private String attributeName;
        private Object value;
    }

    public List<LazyModel.ObjectInfo> extractPersistenceInfo() {
        return extractInfo(this);
    }

    private List<LazyModel.ObjectInfo> extractInfo(LazyModel model) {
        Stream<Field> objectFields = Arrays.stream(model.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .filter(field -> {
                    try {
                        return field.get(model) != null;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

        return objectFields.map(field -> {

            Object fieldContent = null;
            try {
                fieldContent = field.get(model);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            return LazyModel.ObjectInfo
                    .builder()
                    .type(field.getType())
                    .attributeName(field.getName())
                    .value(fieldContent)
                    .build();
        }).toList();

    }


    public <DTO> DTO mapToDto(LazyModel model, Class<DTO> dtoClass) {
        final Class<?> modelClass = model.getClass();

        if (!dtoClass.isAnnotationPresent(LazyMap.class)) {
            throw new LazyMapperException(dtoClass.getName() + " is not annotated with @LazyMap");
        }

        LazyMap lazyAnnotation = dtoClass.getAnnotation(LazyMap.class);
        if (!lazyAnnotation.fromEntity().equals(modelClass)) {
            throw new LazyMapperException("@LazyMap annotation has not declared " + modelClass.getName() + " as fromEntity");
        }

        final Map<String, Field> modelMapping = mapLazyMapper(modelClass.getDeclaredFields());
        final Map<String, Field> dtoMapping = mapLazyMapper(dtoClass.getDeclaredFields());

        DTO outputDto;
        try {
            outputDto = dtoClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new LazyMapperException("Could not instantiate " + dtoClass.getName(), e);
        }

        for (Map.Entry<String, Field> entry : dtoMapping.entrySet()) {
            final String dtoFieldName = entry.getKey();
            final Field dtoField = entry.getValue();
            dtoField.setAccessible(true);

            if (modelMapping.containsKey(dtoFieldName)) {

                Field modelField = modelMapping.get(dtoFieldName);
                modelField.setAccessible(true);

                if (isCompatibleFieldTypes(dtoField, modelField)) {
                    try {
                        Object modelValue = modelField.get(model);
                        dtoField.set(outputDto, modelValue);
                    } catch (IllegalAccessException e) {
                        throw new LazyMapperException("Failed to set value for field: " + dtoFieldName,e);
                    }
                } else {
                    log.info("Skipping field due to type mismatch: {}", dtoFieldName);
                }
            } else {
                log.warn("No corresponding field in model for DTO field: {}", dtoFieldName);
            }
        }

        return outputDto;
    }

    private boolean isCompatibleFieldTypes(Field dtoField, Field modelField) {
        Class<?> dtoFieldType = dtoField.getType();
        Class<?> modelFieldType = modelField.getType();

        return dtoFieldType.equals(modelFieldType);
    }

    private Map<String, Field> mapLazyMapper(Field[] declaredFields) {
        return Arrays.stream(declaredFields)
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toMap(field -> {

                    Mapped mappedAnnotation = field.getAnnotation(Mapped.class);
                    if (mappedAnnotation != null) {
                        return mappedAnnotation.reference();
                    }
                    return field.getName();
                }, field -> field));
    }


}
