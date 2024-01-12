package org.acme.boundary;

import java.util.UUID;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RequestScoped
@Path("/views/beanvalidation/openapi/dummy")
public class BeanValidationOpenApiDummyViewResource {

    @Operation(summary = "get the DummyView for the given id")
    @GET
    @Path("/{id}")
    public BeanValidationDummyView find(@PathParam("id") UUID id) {
        return null;
    }

    @Schema(readOnly = true)
    public class BeanValidationDummyView extends BeanValidationDummy {
    }
}
