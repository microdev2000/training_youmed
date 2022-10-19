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
import youmed.api.dto.StudentDto;
import youmed.api.model.Student;
import youmed.api.service.LimitService;
import youmed.api.service.StudentService;

public class StudentRouter extends AbstractVerticle {

	private StudentService studentService;
	private LimitService limitService;

	@Override
	public void start() {
		studentService = new StudentService();
		HttpServer server = vertx.createHttpServer();
		Router studentRouter = Router.router(vertx);
		studentRouter.route("/api/v1/student/*").handler(BodyHandler.create());
		studentRouter.post("/api/v1/student/add").handler(this::addStudent);

		studentRouter.post("/api/v1/student/post").handler(this::addStudent);
		studentRouter.get("/api/v1/student/:studentId").handler(this::getStudentById);

		studentRouter.get("/api/v1/student/get/all").handler(this::getAllStudent);
		studentRouter.put("/api/v1/student/:studentId").handler(this::updateStudent);

		server.requestHandler(studentRouter).listen(9999);

	}

	private void addStudent(RoutingContext rc) {
		String payload = rc.getBodyAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		Student student = null;
		String jsonPayload = null;
		try {
			student = objectMapper.readValue(payload, Student.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		if (student != null) {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			try {
				jsonPayload = ow.writeValueAsString(student);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			limitService.checkMaximum(student.getClazz().getId(), res -> {
				JsonObject jsonObject = res.result();
				int maximum = jsonObject.getInteger("maximum");
				int total = jsonObject.getInteger("total");
				if (total == maximum) {
					rc.response().setStatusCode(403).putHeader("Content-Type", "application/json ;charset=utf-8")
							.end(new JsonObject().put("Forbidden", "Total student over maximum!").encodePrettily());
					return;
				}
			});

			studentService.addStudent(jsonPayload, student.getClazz().getId(), res -> {
				if (res.succeeded()) {
					JsonObject jsonObject = res.result();
					if (jsonObject != null) {
						rc.response().setStatusCode(201).putHeader("Content-Type", "application/json ;charset=utf-8")
								.end(jsonObject.encodePrettily());
					} else {
						rc.response().setStatusCode(400).putHeader("Content-Type", "application/json ;charset=utf-8")
								.end(new JsonObject().put("error", "Student already exits!").encodePrettily());
					}
				} else {
					rc.fail(res.cause());
				}
			});
		}
	}

	private void getStudentById(RoutingContext rc) {
		String studentId = rc.request().getParam("studentId");
		studentService.getStudentById(studentId, res -> {
			System.out.println(res.result());
			if (res.succeeded()) {
				JsonObject result = res.result();
				if (result != null) {
					rc.response().setStatusCode(200).putHeader("Content-Type", "application/json; charset=utf-8")
							.end(result.encodePrettily());
				} else {
					rc.response().setStatusCode(404).putHeader("Content-Type", "application/json; charset=utf-8")
							.end(new JsonObject().put("error", "Student does not exits!").encodePrettily());
				}
			} else
				rc.fail(res.cause());
		});
	}

	private void getAllStudent(RoutingContext rc) {
		studentService.getALLStudent(res -> {
			if (res.succeeded()) {
				if (res.result() != null) {
					JsonArray arrStudent = new JsonArray();
					res.result().stream().forEach(j -> {
						arrStudent.add(j);
					});
					rc.response().setStatusCode(200).putHeader("Content-Type", "application/json ;charset=utf-8")
							.end(arrStudent.encodePrettily());
				}

			} else {
				rc.fail(res.cause());
			}
		});
	}

	private void updateStudent(RoutingContext rc) {
		String studentId = rc.request().getParam("studentId");
		String json = rc.getBodyAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		StudentDto student = null;
		try {
			student = objectMapper.readValue(json, StudentDto.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonPayload = null;
		try {
			jsonPayload = ow.writeValueAsString(student);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		studentService.updateStudent(studentId, jsonPayload, res -> {
			if (res.succeeded()) {
				JsonObject jsonObject = res.result();
				if (jsonObject != null)
					rc.response().setStatusCode(201).putHeader("Content-Type", "application/json ;charset=utf-8")
							.end(jsonObject.encodePrettily());
				else {
					rc.response().setStatusCode(404).putHeader("Content-Type", "application/json ;charset=utf-8")
							.end(new JsonObject().put("error", "Student does not exits!").encodePrettily());
				}
			} else {
				res.cause();
			}
		});

	}

	public void deleteStudent(RoutingContext rc) {
		String studentId = rc.request().getParam("studentId");
		studentService.getStudentById(studentId, res -> {
			if (res.succeeded()) {
				ObjectMapper objectMapper = new ObjectMapper();
				String result = res.result().toString();
				Student student = null;
				try {
					student = objectMapper.readValue(result, Student.class);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

				studentService.deleteStudent(studentId, student.getClazz().getId(), resp -> {
					if (resp.succeeded()) {
						rc.response().setStatusCode(204).putHeader("Content-Type", "application/json ;charset=utf-8")
								.end();
					} else {
						rc.response().setStatusCode(404).putHeader("Content-Type", "application/json ;charset=utf-8")
								.end(new JsonObject().put("error", "Student does not exits!").encodePrettily());
					}
				});
			} else {
				res.cause();
			}

		});
	}

}