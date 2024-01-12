package org.acme.boundary;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import jakarta.ws.rs.core.Response;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;

/**
 *
 * @since 06.12.2023
 */
@QuarkusTest
class BeanValidationOpenApiDummyResourceIT {

    @Test
    void shouldGetOpenApiWithBeanValidations() throws Exception {
        //given
        given()
                //when
                .get("/q/openapi.json")
                //then
                .then()
                .log().ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                //Entity
                .rootPath("components.schemas.BeanValidationDummy")
                .body("description", allOf(
                        containsString("At least one of fields [\"a\", \"b\"] has to be not null"),
                        containsString("At least one of fields [\"superField1\", \"superField2\"] has to be not null"), //declared on superclass
                        containsString("DummyConstraint desc"))) //declared at interface
                .appendRootPath("properties")
                .body("a.description", allOf(
                        containsString("Must be greater than or equal to 11 SECONDS"),
                        containsString("Must be lesser than or equal to 21 SECONDS")))
                .body("b.description", allOf(
                        containsString("Must be greater than or equal to 3 HOURS"),
                        containsString("Must be lesser than or equal to 4 SECONDS")))
                .body("c.allOf.minimum", hasItem(
                        is(1)))
                .body("c.description",
                        containsString("Must be greater than zero"))
                .body("d.allOf.minimum", hasItem(
                        is(0)))
                .body("d.description",
                        containsString("Must be greater than or equal to zero"))
                .body("x.description",
                        containsString("DummyConstraint desc"))
                .body("sampleId.description",
                        containsString("Sample with given id has to exist"))
                .body("superField1.description",
                        containsString("Sample with given id has to exist"))
                //View is readOnly -> no beanValidations here
                .rootPath("components.schemas.BeanValidationDummyView")
                .body("description", is(nullValue()))
                .appendRootPath("properties")
                .body("a.description", is(nullValue()))
                .body("b.description", is(nullValue()))
                .body("c.minimum", is(nullValue()))
                .body("c.description", is(nullValue()))
                .body("d.minimum", is(nullValue()))
                .body("d.description", is(nullValue()))
                .body("x.description", is(nullValue()))
                .body("sampleId.description", is(nullValue()))
                .body("superField1.description", is(nullValue()))
                //API Parameter
                .rootPath("paths.'/dummy'.get")
                .body("parameters.description", hasItem(
                        containsString("BeanValidationDummy with given id has to exist on create")))
                .rootPath("paths.'/dummy/all'.get")
                .body("parameters.description", hasItem(
                        containsString("BeanValidationDummy with given id has to exist")))
                .rootPath("paths.'/dummy/beanParam'.get")
                .body("description",
                        containsString("At least one of fields [\"id\", \"name\"] has to be not null"))
                .body("parameters.description", allOf(
                        hasItem(containsString("BeanValidationDummy with given id has to exist")),
                        hasItem(containsString("BeanValidationDummy with given name has to exist"))))
                .rootPath("paths.'/dummy/dateTime'.get")
                .body("parameters.find{it.name == 'from'}.description", containsString("""
                        __BeanValidation at parameter:__ \s
                        * must be a date in the past or in the present \s
                        * must not be null"""))
                .noRootPath()
                .body("components.schemas.BeanValidationDummy.properties.strings.description",
                        is("""
                        __BeanValidation at Field/Getter-level:__ \s
                        * size must be between 0 and 20 \s
                        * For each item: \s
                          * size must be between 0 and 10"""));
    }
}
