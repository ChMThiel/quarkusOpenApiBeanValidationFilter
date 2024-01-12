package org.acme.boundary;

import java.util.UUID;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.Operation;
import jakarta.validation.Valid;
import org.acme.AtLeastOneNotNull;
import org.acme.ExistingId;
import org.acme.ExistingName;
import org.acme.ValidCreate;

@RequestScoped
@Path("/dummy")
@Transactional
public class BeanValidationOpenApiDummyResource {

    @POST
    public BeanValidationDummy create(BeanValidationDummy data) {
        return null;
    }

    @Operation(summary = "get the Dummy for the given id")
    @GET
    public BeanValidationDummy find(@QueryParam("otherParamName") @ExistingId(of = BeanValidationDummy.class, groups = ValidCreate.class) UUID aIds) {
        return null;
    }

    @GET
    @Path("dateTime")
    public List<BeanValidationDummy> filterByDateTime(@Valid @BeanParam MyDateTimeFilter aFilter) {
        return null;
    }

    @Operation(summary = "get the Dummy for the given id")
    @GET
    @Path("all")
    public BeanValidationDummy findAll(@QueryParam("otherParamName") List<@ExistingId(of = BeanValidationDummy.class) UUID> aIds) {
        return null;
    }

    @Operation(summary = "get the Dummy for the given id")
    @GET
    @Path("beanParam")
    public BeanValidationDummy findByBeanParam(@BeanParam Param p) {
        return null;
    }

    @Operation(summary = "update Dummy")
    @RequestBody(
            content = @Content(
                    mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = BeanValidationDummy.class)))
    @PUT
    @Path("/{id}")
    public BeanValidationDummy update(@PathParam("id") UUID id, String data) {
        return null;
    }

    @AtLeastOneNotNull(ofFields = {"id", "name"})
    static class Param extends BaseParam {

        @QueryParam("id")
        @ExistingId(of = BeanValidationDummy.class)
        UUID myId;
    }

    static abstract class BaseParam {

        @QueryParam("name")
        @ExistingName(of = BeanValidationDummy.class)
        String myName;
    }

    static class MyDateTimeFilter extends DateTimeFilter<MyDateTimeFilter> {

    }

}
