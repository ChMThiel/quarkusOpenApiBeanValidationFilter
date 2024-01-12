package org.acme.validation;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import static io.quarkus.smallrye.openapi.OpenApiFilter.RunStage.BUILD;
import jakarta.validation.Constraint;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.lang.annotation.Repeatable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.function.Predicate.not;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenIterator;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationInstanceEquivalenceProxy;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import static org.jboss.jandex.AnnotationValue.Kind.ARRAY;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

/**
 * @since 05.12.2023
 */
@OpenApiFilter(BUILD)
public class OpenApiBeanValidationFilter implements OASFilter {

    private static final String COMPONENTS_SCHEMAS = "#/components/schemas/";
    private static final AtomicInteger RUN = new AtomicInteger();
    protected static final Logger LOGGER = Logger.getLogger(OpenApiBeanValidationFilter.class);
    protected IndexView jandex;

    public OpenApiBeanValidationFilter(IndexView aJandex) {
        jandex = aJandex;
    }

    Optional<MethodInfo> getMethod(Operation aOperation) {
        String[] operationId = aOperation.getOperationId().split("_");
        String methodName = operationId[1];
        return Optional.ofNullable(jandex)
                .map(i -> i.getClassByName(operationId[0]))
                .map(ClassInfo::methods)
                .stream()
                .flatMap(List::stream)
                .filter(method -> method.name().equals(methodName))
                .findAny();
    }

    @Override
    public void filterOpenAPI(OpenAPI aOpenAPI) {
        if (RUN.incrementAndGet() > 1) {
            LOGGER.info("OpenApiBeanValidationFilter.filterOpenAPI() NOT RUNNING MULTIPLE TIMES, Attempt no " + RUN);
            return;
        }
        LOGGER.info("OpenApiBeanValidationFilter.filterOpenAPI()");
        Set<DotName> beanValidationAnnotations = findBeanValidationAnnotations();
        Map<String, Schema> refSchemaMap = aOpenAPI.getComponents().getSchemas().entrySet().stream()
                .collect(Collectors.toMap(e -> COMPONENTS_SCHEMAS + e.getKey(), Map.Entry::getValue));
        //REST-API's parameters
        aOpenAPI.getPaths().getPathItems().entrySet().stream()
                .filter(p -> !p.getKey().contains("/q/")) //not the quarkus-dev APIs
                .map(Map.Entry<String, PathItem>::getValue)
                .map(PathItem::getOperations)
                .map(Map::values)
                .flatMap(Collection::stream)
                .forEach(e -> addBeanValidationDescriptionToOperation(e, beanValidationAnnotations));
        //classes/fields/properties
        refSchemaMap.entrySet().stream()
                .filter(e -> !Optional.ofNullable(e.getValue().getReadOnly()).orElse(false))
                .forEach(e -> addBeanValidationDescriptionToSchema(e.getKey(), e.getValue(), beanValidationAnnotations));
    }

    Set<DotName> findBeanValidationAnnotations() {
        return jandex.getKnownClasses().stream()
                .filter(ClassInfo::isAnnotation)
                .filter(i -> i.hasDeclaredAnnotation(Constraint.class))
                .distinct()
                .map(ClassInfo::name)
                .collect(Collectors.toSet());
    }

    void addBeanValidationDescriptionToOperation(Operation aOperation, Set<DotName> aKnownBeanValidationAnnotations) {
        Optional<MethodInfo> method = getMethod(aOperation);
        if (method.isPresent() && aOperation.getParameters() != null) {
            List<MethodParameterInfo> methodParameters = method.get().parameters();
            Optional<MethodParameterInfo> beanParameter = methodParameters.stream()
                    .filter(mp -> mp.hasAnnotation(DotName.createSimple(BeanParam.class)))
                    .findFirst();
            if (beanParameter.isPresent()) {
                ClassInfo beanParamClass = jandex.getClassByName(beanParameter.get().type().name());
                Set<AnnotationInstance> annotations = new HashSet<>();
                getAnnotationsOnClass(beanParamClass, aKnownBeanValidationAnnotations, annotations);
                String annotationsOnBeanParamClass = annotations.stream()
                        .map(this::toBeanValidationInfo)
                        .sorted()
                        .collect(Collectors.joining("  \n"));
                if (!annotationsOnBeanParamClass.isEmpty()) {
                    String text = "  \n### BeanValidation at parameter:  \n" + annotationsOnBeanParamClass;
                    aOperation.setDescription(aOperation.getDescription() == null
                            ? text
                            : aOperation.getDescription() + text);
                }
                aOperation.getParameters().stream()
                        .filter(openApiParameter -> openApiParameter.getName() != null)
                        .forEach(openApiParameter -> addBeanValidationDescriptionOfBeanParamToParameter(beanParamClass, openApiParameter, aKnownBeanValidationAnnotations));
            }
            aOperation.getParameters().stream()
                    .filter(openApiParameter -> openApiParameter.getName() != null)
                    .forEach(openApiParameter -> {
                        methodParameters.stream()
                                .filter(javaParameter -> isMatchingParameter(javaParameter, openApiParameter.getName()))
                                .findFirst()
                                .ifPresent(matchingJavaParameter
                                        -> addBeanValidationToParameter(openApiParameter, matchingJavaParameter, aKnownBeanValidationAnnotations));
                    });
        }
    }

    boolean isMatchingParameter(AnnotationTarget aJavaParameter, String aOpenApiParameterName) {
        return Stream
                .of(aJavaParameter.declaredAnnotation(QueryParam.class),
                        aJavaParameter.declaredAnnotation(PathParam.class),
                        aJavaParameter.declaredAnnotation(HeaderParam.class))
                .filter(Objects::nonNull)
                .map(AnnotationInstance::value)
                .map(AnnotationValue::asString)
                .anyMatch(aOpenApiParameterName::equals);
    }

    /**
     * recursiv
     *
     * @param aBeanParamClassInfo
     * @param aName
     * @return
     */
    FieldInfo getFieldForBeanParam(ClassInfo aBeanParamClassInfo, String aName) {
        if (aBeanParamClassInfo == null) {
            return null;
        }
        Optional<FieldInfo> matchingField = aBeanParamClassInfo.fields().stream()
                .filter(field -> isMatchingParameter(field, aName))
                .findAny();
        if (matchingField.isPresent()) {
            return matchingField.get();
        }
        DotName superName = aBeanParamClassInfo.superName();
        if (superName != null) {
            return getFieldForBeanParam(jandex.getClassByName(superName), aName);
        }
        return null;
    }

    void addBeanValidationDescriptionOfBeanParamToParameter(ClassInfo aBeanParamClassInfo, Parameter aParameter, Set<DotName> aKnownBeanValidationAnnotations) {
        FieldInfo field = getFieldForBeanParam(aBeanParamClassInfo, aParameter.getName());
        if (field != null) {
            //beanValidation annotated at field
            String beanValidationDescription = getDescriptionForField(aKnownBeanValidationAnnotations, field, aBeanParamClassInfo, null);
            addDescription(aParameter, beanValidationDescription);
        } else {
            LOGGER.trace("OpenApiBeanValidationFilter.addBeanValidationDescriptionOfBeanParamToParameter() "
                    + "parameter " + aParameter.getName() + " not found in class " + aBeanParamClassInfo);
        }
    }

    void addBeanValidationToParameter(Parameter aParameter, AnnotationTarget aParameterInfo, Set<DotName> aBeanValidations) {
        aBeanValidations.stream()
                .map(aParameterInfo::annotation)
                .filter(Objects::nonNull)
                .map(this::toBeanValidationInfo)
                .sorted()
                .forEach(info -> addDescription(aParameter, info));
    }

    void addDescription(Parameter aParameter, String aBeanValidationDescription) {
        if (aBeanValidationDescription != null && !aBeanValidationDescription.isEmpty()) {
            String text = "__BeanValidation at parameter:__  \n" + aBeanValidationDescription;
            aParameter.setDescription(aParameter.getDescription() == null
                    ? text
                    : aParameter.getDescription() + "  \n" + text);
        }
    }

    void addBeanValidationDescriptionToSchema(String aName, Schema aClassSchema, Set<DotName> aKnownBeanValidationAnnotations) {
        try {
            String simpleClassName = aName.substring(aName.lastIndexOf('/') + 1);
            jandex.getKnownClasses().stream()
                    .filter(c -> c.name().withoutPackagePrefix().equals(simpleClassName))
                    .findAny()
                    .ifPresent(c -> {
                        //BeanValidations on Class
                        addBeanValidationDescriptionToClass(aClassSchema, c, aKnownBeanValidationAnnotations);
                        //BeanValidations on Properties
                        Optional.ofNullable(aClassSchema.getProperties())
                                .map(Map<String, Schema>::entrySet)
                                .stream()
                                .flatMap(Set::stream)
                                .forEach(e -> addBeanValidationDescriptionToProperty(c, e.getKey(), e.getValue(), aKnownBeanValidationAnnotations));
                    });
        } catch (Exception e) {
            LOGGER.error("OpenApiBeanValidationFilter.setBeanValidationDescriptions()", e);
        }
    }

    /**
     * Considers annotations at field & getter
     *
     * @param aClassInfo
     * @param aFieldName
     * @param aFieldSchema
     * @param aKnownBeanValidationAnnotations
     */
    void addBeanValidationDescriptionToProperty(ClassInfo aClassInfo, String aFieldName, Schema aFieldSchema, Set<DotName> aKnownBeanValidationAnnotations) {
        FieldInfo field = getField(aClassInfo, aFieldName);
        if (field != null) {
            String beanValidationDescription = getDescriptionForField(aKnownBeanValidationAnnotations, field, aClassInfo, aFieldSchema);
            addDescription(aFieldSchema, "BeanValidation at Field/Getter-level", beanValidationDescription);
        } else {
            LOGGER.trace("OpenApiBeanValidationFilter.setBeanValidationDescriptions() field " + aFieldName + " not found in class " + aClassInfo);
        }
    }

    /**
     * combines Field- an Getter-BeanValidations
     *
     * @param aKnownBeanValidationAnnotations
     * @param aField
     * @param aClassInfo
     * @param aFieldSchema
     * @return
     */
    String getDescriptionForField(Set<DotName> aKnownBeanValidationAnnotations, FieldInfo aField, ClassInfo aClassInfo, Schema aFieldSchema) {
        //beanValidation annotated at field
        Map<DotName, Set<AnnotationInstance>> annotationOnFields = aKnownBeanValidationAnnotations.stream()
                .map(aField::annotations)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(AnnotationInstance::name, HashMap::new, Collectors.toCollection(LinkedHashSet::new)));
        Set<AnnotationInstance> annotationsOnGetter = getAnnotationsOnGetter(aClassInfo, aField, aKnownBeanValidationAnnotations);
        //getter-annotation overrides field-annotation
        annotationsOnGetter.stream()
                .filter(not(this::isRepeatable))
                .map(AnnotationInstance::name)
                .forEach(annotationOnFields::remove);
        Map<DotName, Set<AnnotationInstance>> allAnnotations = new HashMap<>(annotationOnFields);
        annotationsOnGetter.forEach(annotationAtGetter -> allAnnotations.merge(
                annotationAtGetter.name(),
                new HashSet<>(List.of(annotationAtGetter)),
                (a, b) -> {
                    a.addAll(b);
                    return a;
                }));
        //key:
        //DIRECT:  annotation on field/getter                 (e.g. @Size(max = 10) List<String> strings)
        //INNER:   annotation on generic type of field/getter (e.g. List<@Size(max = 10) String> strings)
        Map<String, List<AnnotationInstance>> groupedByTarget = allAnnotations.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(
                        ai -> AnnotationTarget.Kind.TYPE.equals(ai.target().kind()) ? "INNER" : "DIRECT"));
        List<AnnotationInstance> direct = groupedByTarget.getOrDefault("DIRECT", List.of());
        //in INNER there are all DIRECT's too, but with EMPTY target...there is no way to hide them ;(
        //filter all inner, that are 'equivalent' to outer
        List<AnnotationInstanceEquivalenceProxy> tempDirect = direct
                .stream()
                .map(AnnotationInstance::createEquivalenceProxy)
                .collect(Collectors.toList());
        List<AnnotationInstance> inner = groupedByTarget
                .getOrDefault("INNER", List.of())
                .stream()
                .map(AnnotationInstance::createEquivalenceProxy)
                .filter(i -> {
                    if (tempDirect.stream().anyMatch(i::equals)) {
                        tempDirect.remove(i);
                        return false;
                    }
                    return true;
                })
                .map(AnnotationInstanceEquivalenceProxy::get)
                .toList();
        String beanValidationDescription = direct
                .stream()
                .map(annotationInstance -> handlePositiveOrZero(aFieldSchema, annotationInstance))
                .map(this::toBeanValidationInfo)
                .sorted()
                .collect(Collectors.joining("  \n"));
        if (!inner.isEmpty()) {
            String innerDescription = inner
                    .stream()
                    .map(annotationInstance -> handlePositiveOrZero(aFieldSchema, annotationInstance))
                    .map(this::toBeanValidationInfo)
                    .map(d -> "  " + d) //indent for sub-list in generated markup
                    .sorted()
                    .collect(Collectors.joining("  \n"));
            beanValidationDescription = (beanValidationDescription.isEmpty() ? "" : beanValidationDescription + "  \n")
                    + "* For each item:  \n" + innerDescription;
        }
        if (beanValidationDescription.lines().map(String::strip).count() != beanValidationDescription.lines().map(String::strip).distinct().count()) {
            LOGGER.warn("OpenApiBeanValidationFilter.getDescriptionForField: "
                    + "BeanValidation-doublettes found at field " + aField + ". (Same BeanValidation-annoation at field, getter or interface?)");
        }
        return beanValidationDescription;
    }

    AnnotationInstance handlePositiveOrZero(Schema aFieldSchema, AnnotationInstance aAnnotationInstance) {
        if (aFieldSchema != null && aFieldSchema.getMinimum() == null) {
            String annotationName = aAnnotationInstance.name().withoutPackagePrefix();
            BigDecimal min;
            if (annotationName.contains("PositiveOrZero")) {
                min = BigDecimal.ZERO;
            } else if (annotationName.contains("Positive")) {
                min = BigDecimal.ONE;
            } else {
                min = null;
            }
            if (min != null) {
                if (aFieldSchema.getRef() == null) {
                    aFieldSchema.setMinimum(min);
                } else {
                    aFieldSchema.addAllOf(OASFactory.createSchema().minimum(min));
                    aFieldSchema.addAllOf(OASFactory.createSchema().ref(aFieldSchema.getRef()));
                    aFieldSchema.setRef(null);
                }
            }
        }
        return aAnnotationInstance;
    }

    boolean isRepeatable(AnnotationInstance aAnnotationInstance) {
        return jandex
                .getClassByName(aAnnotationInstance.name())
                .annotation(DotName.createSimple(Repeatable.class)) != null;
    }

    void addDescription(Schema aSchema, String aHeader, String aBeanValidationDescription) {
        if (aBeanValidationDescription != null && !aBeanValidationDescription.isEmpty()) {
            String text = "__" + aHeader + ":__  \n" + aBeanValidationDescription;
            aSchema.setDescription(aSchema.getDescription() == null ? text : aSchema.getDescription() + "  \n" + text);
            if (aSchema.getRef() != null) {
                aSchema.addAllOf(OASFactory.createSchema().ref(aSchema.getRef()));
                aSchema.setRef(null);
            }
        }
    }

    /**
     * recursiv
     *
     * @param aClassInfo
     * @param aName
     * @return
     */
    FieldInfo getField(ClassInfo aClassInfo, String aName) {
        if (aClassInfo == null) {
            return null;
        }
        FieldInfo field = aClassInfo.field(aName);
        if (field != null) {
            return field;
        }
        DotName superName = aClassInfo.superName();
        if (superName != null) {
            return getField(jandex.getClassByName(superName), aName);
        }
        return null;
    }

    static final Set<DotName> BOOLEANS = Set.of(DotName.createSimple(Boolean.class), DotName.createSimple(boolean.class));

    /**
     * recurse all interfaces and superclasses
     *
     * @param aClassInfo
     * @param aFieldInfo
     * @param aKnownBeanValidationAnnotations
     * @return
     */
    Set<AnnotationInstance> getAnnotationsOnGetter(ClassInfo aClassInfo, FieldInfo aFieldInfo, Set<DotName> aKnownBeanValidationAnnotations) {
        String methodNamePrefix = BOOLEANS.contains(aFieldInfo.type().name())
                ? "is"
                : "get";
        String fieldName = aFieldInfo.name();
        String getterName = methodNamePrefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Set<AnnotationInstance> annotations = new HashSet<>();
        addGetter(aClassInfo, getterName, aKnownBeanValidationAnnotations, annotations);
        return annotations;
    }

    void addGetter(ClassInfo aClassInfo, String aGetterName, Set<DotName> aKnownBeanValidationAnnotations, Set<AnnotationInstance> aAnnotations) {
        if (aClassInfo == null) {
            return;
        }
        Set<DotName> alreadyKnown = aAnnotations.stream().map(AnnotationInstance::name).collect(Collectors.toSet());
        //given class
        Optional.ofNullable(aClassInfo.method(aGetterName))
                .stream()
                .map(MethodInfo::annotations)
                .flatMap(List::stream)
                .filter(a -> aKnownBeanValidationAnnotations.contains(a.name()))
                .filter(a -> !alreadyKnown.contains(a.name()) || isRepeatable(a))
                .forEach(aAnnotations::add);
        //recurse into all implemented interfaces
        aClassInfo.interfaceNames().stream()
                .map(jandex::getClassByName)
                .filter(Objects::nonNull)
                .forEach(interfaceInfo -> addGetter(interfaceInfo, aGetterName, aKnownBeanValidationAnnotations, aAnnotations));
        //recurse into superclass
        addGetter(jandex.getClassByName(aClassInfo.superName()), aGetterName, aKnownBeanValidationAnnotations, aAnnotations);
    }

    void addBeanValidationDescriptionToClass(Schema aClassSchema, ClassInfo aClassInfo, Set<DotName> aKnownBeanValidationAnnotations) {
        Set<AnnotationInstance> annotations = new HashSet<>();
        getAnnotationsOnClass(aClassInfo, aKnownBeanValidationAnnotations, annotations);
        String beanValidationDescription = annotations.stream()
                .map(this::toBeanValidationInfo)
                .sorted()
                .collect(Collectors.joining("  \n"));
        addDescription(aClassSchema, "BeanValidation at Class-level", beanValidationDescription);
    }

    /**
     * recurse all interfaces and superclasses
     *
     * @param aClassInfo
     * @param aKnownBeanValidationAnnotations
     * @param aAnnotations
     */
    void getAnnotationsOnClass(ClassInfo aClassInfo, Set<DotName> aKnownBeanValidationAnnotations, Set<AnnotationInstance> aAnnotations) {
        if (aClassInfo == null) {
            return;
        }
        Set<DotName> alreadyKnown = aAnnotations.stream().map(AnnotationInstance::name).collect(Collectors.toSet());
        //given class
        aClassInfo.declaredAnnotations().stream()
                .filter(a -> a.target().equals(aClassInfo))
                .filter(a -> aKnownBeanValidationAnnotations.contains(a.name()))
                .filter(a -> !alreadyKnown.contains(a.name()) || isRepeatable(a))
                .forEach(aAnnotations::add);
        //recurse into all implemented interfaces
        aClassInfo.interfaceNames().stream()
                .map(jandex::getClassByName)
                .filter(Objects::nonNull)
                .forEach(interfaceInfo -> getAnnotationsOnClass(interfaceInfo, aKnownBeanValidationAnnotations, aAnnotations));
        //recurse into superclass
        getAnnotationsOnClass(jandex.getClassByName(aClassInfo.superName()), aKnownBeanValidationAnnotations, aAnnotations);
    }

    String toBeanValidationInfo(AnnotationInstance aAnnotationInstance) {
        return Optional
                .ofNullable(aAnnotationInstance.valueWithDefault(jandex, "openApiDescription"))
                .map(AnnotationValue::asString)
                .or(()//no openApiDescription present...try to get translated message
                        -> Optional
                        .ofNullable(aAnnotationInstance.valueWithDefault(jandex, "message"))
                        .map(AnnotationValue::asString)
                        .map(this::removeCurlyBraces)
                        .map(resourceKey -> MessageInterpolator.LOCATOR.getResourceBundle(Locale.ENGLISH).getString(resourceKey)))
                .map(desc -> "* " + interpolateMessage(desc, aAnnotationInstance) + getValidationGroups(aAnnotationInstance))
                .orElse("?");
    }

    String getValidationGroups(AnnotationInstance aAnnotationInstance) {
        String validationGroupDescription = Optional
                .ofNullable(aAnnotationInstance.value("groups")) //ignore default-value, only explictly set values are documented
                .map(AnnotationValue::asClassArray)
                .stream()
                .flatMap(Arrays::stream)
                .map(Type::name)
                .map(DotName::withoutPackagePrefix)
                .map(n -> n.contains("$") ? n.substring(n.indexOf('$') + 1) : n) //remove inner-class prefix: e.g. foo$confirm
                .map(String::toLowerCase)
                .map(x -> x.replace("valid", "")) //my validationGroup-classes all begin with 'valid'
                .collect(Collectors.joining(" and "));
        if (!validationGroupDescription.isEmpty()) {
            validationGroupDescription = " on " + validationGroupDescription;
        }
        return validationGroupDescription;
    }

    /**
     * replace placeholder in beanValidation-description. Only properties present in current Annotation are allowed (no
     * expression/functions/etc.)
     *
     * @param aMessage
     * @param aAnnotationInstance
     * @return
     */
    String interpolateMessage(String aMessage, AnnotationInstance aAnnotationInstance) {
        TokenCollector tokenCollector = new TokenCollector(aMessage, InterpolationTermType.PARAMETER);
        TokenIterator tokenIterator = new TokenIterator(tokenCollector.getTokenList());
        while (tokenIterator.hasMoreInterpolationTerms()) {
            String term = tokenIterator.nextInterpolationTerm();
            AnnotationValue annotationValue = aAnnotationInstance.valueWithDefault(jandex, removeCurlyBraces(term));
            Object resolvedParameterValue = switch (annotationValue.kind()) {
                case ARRAY ->
                    annotationValue.asArrayList();
                case STRING ->
                    annotationValue.asString();
                case LONG ->
                    annotationValue.asLong();
                case INTEGER ->
                    annotationValue.asInt();
                case SHORT ->
                    annotationValue.asShort();
                case DOUBLE ->
                    annotationValue.asDouble();
                case FLOAT ->
                    annotationValue.asFloat();
                case CHARACTER ->
                    annotationValue.asChar();
                case ENUM ->
                    annotationValue.asEnum();
                case BOOLEAN ->
                    annotationValue.asBoolean();
                case CLASS ->
                    annotationValue.asClass().name().withoutPackagePrefix();
                case UNKNOWN ->
                    annotationValue.asString();
                default ->
                    annotationValue.asString();
            };
            tokenIterator.replaceCurrentInterpolationTerm(resolvedParameterValue.toString());
        }
        return tokenIterator.getInterpolatedMessage();
    }

    String removeCurlyBraces(String parameter) {
        return parameter.substring(1, parameter.length() - 1);
    }
}
