package edu.ies63.rcc2026_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Rcc2026BackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("Debe ejecutar el método main correctamente para completar la cobertura de la clase principal")
	void mainMethod_ShouldExecuteSuccessfully() {
		// Ejecutar el main sin argumentos levanta y cierra el contexto rápidamente en el test
		Rcc2026BackendApplication.main(new String[]{});
	}
}
