package youmed.api.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import youmed.api.dto.ClazzDto;
import youmed.api.model.Clazz;
import youmed.api.service.ClassService;

public class ClassRouter extends AbstractVerticle {
	public ClassService classService;

	@Override
	public void start() {
		classService = new ClassService();
		HttpServer server = vertx.createHttpServer();
		Router classRouter = Router.router(vertx);
		classRouter.route("/api/v1/class/*").handler(BodyHandler.create());
		classRouter.post("/api/v1/class/post").handler(this::addClazz);
		classRouter.put("/api/v1/class/:clazzId").handler(this::updateClazz);
		classRouter.get("/api/v1/class/get/all").handler(this::getAllClazz);
		classRouter.get("/api/v1/class/get/:clazzId").handler(this::getClazzById);
		classRouter.delete("/api/v1/class/delete/:clazzId").handler(this::deleteClazz);
		server.requestHandler(classRouter).listen(9999);

	}

	private void addClazz(RoutingContext rc) {
		String payload = rc.getBodyAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		Clazz clazz = null;
		String jsonPayload = null;
		try {
			clazz = objectMapper.readValue(payload, Clazz.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		if (clazz != null) {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			try {
				jsonPayload = ow.writeValueAsString(clazz);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			classService.addClazz(jsonPayload, res -> {
				if (res.succeeded()) {
					JsonObject result = res.result();
					rc.response().setStatusCode(201).putHeader("Content-Type", "application/json ;charset=utf-8")
							.end(result.encodePrettily());
				} else {
					rc.fail(res.cause());
				}

			});
		}
	}

	private void getAllClazz(RoutingContext rc) {
		classService.getAllClazz(res -> {
			if (res.succeeded()) {
				JsonArray arrClazz = new JsonArray();
				res.result().stream().forEach(j -> {
					arrClazz.add(j);
				});
				rc.response().setStatusCode(200).putHeader("Content-Type", "application/json ;charset=utf-8")
						.end(arrClazz.encodePrettily());
			} else {
				rc.fail(res.cause());
			}
		});

	}

	private void getClazzById(RoutingContext rc) {
		String clazzId = rc.request().getParam("clazzId");
		classService.getClazzById(clazzId, res -> {
			System.out.println(res.result());
			if (res.succeeded()) {
				JsonObject result = res.result();
				if (result != null) {
					rc.response().setStatusCode(200).putHeader("Content-Type", "application/json; charset=utf-8")
							.end(result.encodePrettily());
				} else {
					rc.response().setStatusCode(400).putHeader("Content-Type", "application/json; charset=utf-8")
							.end(new JsonObject().put("error", "Class not found!").encodePrettily());
				}
			} else
				rc.fail(res.cause());
		});

	}

	private void updateClazz(RoutingContext rc) {
		String clazzId = rc.request().getParam("clazzId");
		String json = rc.getBodyAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		ClazzDto clazzDto = null;
		try {
			clazzDto = objectMapper.readValue(json, ClazzDto.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonPayload = null;
		try {
			jsonPayload = ow.writeValueAsString(clazzDto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		classService.updateClazz(clazzId, jsonPayload, res -> {
			if (res.succeeded()) {
				JsonObject result = res.result();
				if (result != null) {
					rc.response().setStatusCode(200).putHeader("Content-Type", "application/json; charset=utf-8")
							.end(result.encodePrettily());
				} else {
					rc.response().setStatusCode(404).putHeader("Content-Type", "application/json; charset=utf-8")
					.end(new JsonObject().put("error", "Class does not exist!").encodePrettily());
				}
			} else {
				res.cause();
			}
		});

	}

	private void deleteClazz(RoutingContext rc) {
		String clazzId = rc.request().getParam("clazzId");
		classService.deleteClazz(clazzId, res -> {
			if (res.succeeded()) {
				rc.response().setStatusCode(204).putHeader("Content-Type", "application/json; charset=utf-8").end();
			} else {
				res.cause();
			}
		});
	}

}
