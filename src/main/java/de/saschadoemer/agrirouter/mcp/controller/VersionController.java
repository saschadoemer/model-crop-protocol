package de.saschadoemer.agrirouter.mcp.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

/**
 * Version controller.
 */
@Controller("/version")
public class VersionController {

    /**
     * Simple version endpoint.
     *
     * @return the version
     */
    @Get
    @Produces(MediaType.TEXT_PLAIN)
    public String version() {
        return "1.0.0";
    }
}
