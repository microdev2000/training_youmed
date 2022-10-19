package youmed.api.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import youmed.api.dto.LimitDto;
import youmed.api.service.LimitService;

public class LimitRouter extends AbstractVerticle {

	public LimitService limitService;

	@Override
	public void start() {
		limitService = new LimitService();
		HttpServer server = vertx.createHttpServer();
		Router classRouter = Router.router(vertx);
		classRouter.route("/api/v1/limit/*").handler(BodyHandler.create());
		classRouter.put("/api/v1/limit/:limitId").handler(this::updateLimit);
		server.requestHandler(classRouter).listen(9999);
	}

	private void updateLimit(RoutingContext rc) {
		String limitId = rc.request().getParam("limitId");
		String payload = rc.getBodyAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		LimitDto limitDto = null;
		try {
			limitDto = objectMapper.readValue(payload, LimitDto.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		limitService.updateMaximumStudent(limitId, limitDto.getMaximum(), res -> {
			if (res.succeeded()) {
				JsonObject result = res.result();
				rc.response().setStatusCode(201).putHeader("Content-Type", "application/json ;charset=utf-8")
						.end(result.encodePrettily());
			} else {
				res.cause();
			}
		});
	}

}
