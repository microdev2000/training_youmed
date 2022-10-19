package youmed.api;

import io.vertx.core.Vertx;
import youmed.api.builder.DBConfigBuilder;
import youmed.api.router.ClassRouter;
import youmed.api.router.SpecialityRouter;
import youmed.api.router.StudentRouter;
import youmed.api.service.ClassService;
import youmed.api.service.SpecialityService;
import youmed.api.service.StudentService;

public class Main {
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(DBConfigBuilder.class.getName(), res -> {
			if (res.succeeded()) {
				vertx.deployVerticle(StudentRouter.class.getName());
				vertx.deployVerticle(ClassRouter.class.getName());
				vertx.deployVerticle(SpecialityRouter.class.getName());
				vertx.deployVerticle(new StudentService());
				vertx.deployVerticle(new ClassService());
				vertx.deployVerticle(new SpecialityService());
			}
			else {
				res.cause();
			}
		});
		
	}
}
