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
import youmed.api.dto.SpecialityDto;
import youmed.api.model.Speciality;
import youmed.api.service.SpecialityService;

public class SpecialityRouter extends AbstractVerticle {
	public SpecialityService specialityService;

	@Override
	public void start() {
		specialityService = new SpecialityService();
		HttpServer server = vertx.createHttpServer();
		Router specialRouter = Router.router(vertx);
		specialRouter.route("/api/v1/speciality/*").handler(BodyHandler.create());
		specialRouter.post("/api/v1/speciality/post").handler(this::addSpeciality);
		specialRouter.put("/api/v1/speciality/:specialityId").handler(this::updateSpeciality);
		specialRouter.get("/api/v1/speciality/get/all").handler(this::getAllSpeciality);
		specialRouter.get("/api/v1/speciality/get/:specialityId").handler(this::getSpecialityById);
		specialRouter.delete("/api/v1/speciality/delete/:").handler(this::deleteSpeciality);

		server.requestHandler(specialRouter).listen(9999);

	}

	private void addSpeciality(RoutingContext rc) {
		String payload = rc.getBodyAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		Speciality speciality = null;
		String jsonPayload = null;
		try {
			speciality = objectMapper.readValue(payload, Speciality.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		if (speciality != null) {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			try {
				jsonPayload = ow.writeValueAsString(speciality);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			specialityService.addSpeciality(jsonPayload, res -> {
				if (res.succeeded()) {
					JsonObject result = res.result();
					if (result != null) {
						rc.response().setStatusCode(201).putHeader("Content-Type", "application/json ;charset=utf-8")
						.end(result.encodePrettily());
					}
				} else {
					rc.fail(res.cause());
				}
			});
		}
	}

	private void getAllSpeciality(RoutingContext rc) {
		specialityService.getAllSpeciality(res -> {
			if (res.succeeded()) {
				JsonArray arrSpeciality = new JsonArray();
				res.result().stream().forEach(j -> {
					arrSpeciality.add(j);
				});
				rc.response().setStatusCode(200).putHeader("Content-Type", "application/json ;charset=utf-8")
						.end(arrSpeciality.encodePrettily());
			} else {
				rc.fail(res.cause());
			}
		});
	}

	private void getSpecialityById(RoutingContext rc) {
		String specialityId = rc.request().getParam("specialityId");
		specialityService.getSpecialityById(specialityId, res -> {
			System.out.println(res.result());
			if (res.succeeded()) {
				JsonObject result = res.result();
				if (result != null) {
					rc.response().setStatusCode(200).putHeader("Content-Type", "application/json; charset=utf-8")
							.end(result.encodePrettily());
				} else {
					rc.response().setStatusCode(404).putHeader("Content-Type", "application/json; charset=utf-8")
							.end("NOT FOUND");
				}
			} else
				rc.fail(res.cause());
		});

	}

	private void updateSpeciality(RoutingContext rc) {
		String specialityId = rc.request().getParam("specialityId");
		String json = rc.getBodyAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		SpecialityDto specialityDto = null;
		try {
			specialityDto = objectMapper.readValue(json, SpecialityDto.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonPayload = null;
		try {
			jsonPayload = ow.writeValueAsString(specialityDto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		specialityService.updateSpeciality(specialityId, jsonPayload, res -> {
			if (res.succeeded()) {
			} else {
				res.cause();
			}
		});

	}

	private void deleteSpeciality(RoutingContext rc) {
		String specialityId = rc.request().getParam("specialityId");
		specialityService.deleteSpeciality(specialityId, res -> {
			if (res.succeeded()) {
				rc.response().setStatusCode(204).putHeader("Content-Type", "application/json; charset=utf-8")
				.end();
			}
			else {
				res.cause();
			}
		});
	}

}
