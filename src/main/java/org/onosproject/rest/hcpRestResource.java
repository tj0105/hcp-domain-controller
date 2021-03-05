package org.onosproject.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @Author ldy
 * @Date: 2021/3/3 下午3:00
 * @Version 1.0
 */
@Path("hcp")
public class hcpRestResource extends AbstractWebResource{

    @GET
    @Path("/fnl")
    public Response fnl(){
        ObjectNode root = mapper().createObjectNode();
        ArrayNode array = root.putArray("Hefei Tower");
        array.add(110.5).add(120.3);
        root.put("708","202.38.75.*").put("738","202.38.76.*");
        return ok(root).build();
    }

}
