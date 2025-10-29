package com.barrisense.backend.user.config;

import com.barrisense.backend.user.entity.User;
import com.barrisense.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Componente que se ejecuta al inicio de la aplicación para poblar la base de datos de usuarios
 * con datos de ejemplo si está vacía.
 * <p>
 * Implementa {@link CommandLineRunner}, lo que garantiza que el método {@code run} se ejecute
 * una sola vez después de que el contexto de la aplicación se haya cargado.
 * Su principal responsabilidad es crear un conjunto de usuarios de prueba,
 * asegurando un entorno de desarrollo con datos realistas y coherentes.
 *
 * @author El equipo de BarriSense
 * @since 2025-10-29
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (userRepository.count() > 0) {
            log.info("La base de datos de usuarios ya está poblada. No se crearán nuevos datos.");
            return;
        }

        log.info("Poblando la base de datos con usuarios de prueba...");

        List<String> sampleNames = Arrays.asList(
                "johndoe", "janedoe", "mariagarcia", "alexsmith", "lucasfernandez",
                "aureliendarbellay", "sofiaperez", "emiliocastro", "marisolrios", "andreamartin",
                "marcosrodriguez", "estefaniatorres", "mariarovira", "auremorales", "davidsoler"
        );

        List<String> domains = Arrays.asList(
                "example.com", "mail.com", "barrisense.org", "test.org", "demo.net"
        );

        List<String> profilePics = Arrays.asList(
                "https://i.pravatar.cc/150?img=1",
                "https://i.pravatar.cc/150?img=2",
                "https://i.pravatar.cc/150?img=3",
                "https://i.pravatar.cc/150?img=4",
                "https://i.pravatar.cc/150?img=5",
                "https://i.pravatar.cc/150?img=6",
                "https://i.pravatar.cc/150?img=7",
                "https://i.pravatar.cc/150?img=8",
                "https://i.pravatar.cc/150?img=9",
                "https://i.pravatar.cc/150?img=10"
        );

        Random random = new Random();

        for (String username : sampleNames) {
            String domain = domains.get(random.nextInt(domains.size()));
            String email = username + "@" + domain;
            String profileUrl = profilePics.get(random.nextInt(profilePics.size()));
            boolean active = random.nextBoolean();

            User user = User.builder()
                    .username(username)
                    .email(email)
                    .profilePictureUrl(profileUrl)
                    .createdAt(LocalDateTime.now().minusDays(random.nextInt(100)))
                    .active(active)
                    .build();

            userRepository.save(user);
        }

        log.info("✅ Se han creado {} usuarios de prueba en la base de datos.", sampleNames.size());
    }
}
