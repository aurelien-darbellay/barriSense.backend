package com.barrisense.backend.complaint.config;

import com.barrisense.backend.complaint.entity.Complaint;
import com.barrisense.backend.complaint.entity.Neighborhood;
import com.barrisense.backend.complaint.repository.ComplaintRepository;
import com.barrisense.backend.complaint.repository.NeighborhoodRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;


/**
 * Componente que se ejecuta al inicio de la aplicación para poblar la base de datos con datos de prueba.
 * <p>
 * Implementa {@link CommandLineRunner}, lo que garantiza que el método {@code run} se ejecute una sola vez
 * después de que el contexto de la aplicación se haya cargado.
 * Su principal responsabilidad es crear un conjunto de feedbacks (quejas) de ejemplo si la base de datos está vacía,
 * asegurando un entorno de desarrollo con datos realistas.
 *
 * @author El equipo de BarriSense
 * @since 2025-10-15
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ComplaintRepository complaintRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    @Autowired
    public DataInitializer(ComplaintRepository complaintRepository, NeighborhoodRepository neighborhoodRepository) {
        this.complaintRepository = complaintRepository;
        this.neighborhoodRepository = neighborhoodRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {


        if (neighborhoodRepository.count() == 0) {
            log.info("No se encontraron barrios. Poblando la tabla 'barrios'...");
            List<Neighborhood> barrios = Arrays.asList(
                    Neighborhood.builder().id(1L).name("El Raval").build(),
                    Neighborhood.builder().id(2L).name("El Gòtic").build(),
                    Neighborhood.builder().id(3L).name("La Barceloneta").build(),
                    Neighborhood.builder().id(4L).name("Sant Pere, Santa Caterina i la Ribera").build(),
                    Neighborhood.builder().id(5L).name("El Fort Pienc").build(),
                    Neighborhood.builder().id(6L).name("La Sagrada Família").build(),
                    Neighborhood.builder().id(7L).name("La Dreta de l'Eixample").build(),
                    Neighborhood.builder().id(8L).name("L'Antiga Esquerra de l'Eixample").build(),
                    Neighborhood.builder().id(9L).name("La Nova Esquerra de l'Eixample").build(),
                    Neighborhood.builder().id(10L).name("Sant Antoni").build(),
                    Neighborhood.builder().id(11L).name("El Poble-sec").build(),
                    Neighborhood.builder().id(12L).name("La Marina del Prat Vermell").build(),
                    Neighborhood.builder().id(13L).name("La Marina de Port").build(),
                    Neighborhood.builder().id(14L).name("La Font de la Guatlla").build(),
                    Neighborhood.builder().id(15L).name("Hostafrancs").build(),
                    Neighborhood.builder().id(16L).name("La Bordeta").build(),
                    Neighborhood.builder().id(17L).name("Sants-Badal").build(),
                    Neighborhood.builder().id(18L).name("Sants").build(),
                    Neighborhood.builder().id(19L).name("Les Corts").build(),
                    Neighborhood.builder().id(20L).name("La Maternitat i Sant Ramon").build(),
                    Neighborhood.builder().id(21L).name("Pedralbes").build(),
                    Neighborhood.builder().id(22L).name("Vallvidrera, el Tibidabo i les Planes").build(),
                    Neighborhood.builder().id(23L).name("Sarrià").build(),
                    Neighborhood.builder().id(24L).name("Les Tres Torres").build(),
                    Neighborhood.builder().id(25L).name("Sant Gervasi - la Bonanova").build(),
                    Neighborhood.builder().id(26L).name("Sant Gervasi - Galvany").build(),
                    Neighborhood.builder().id(27L).name("El Putxet i el Farró").build(),
                    Neighborhood.builder().id(28L).name("Vallcarca i els Penitents").build(),
                    Neighborhood.builder().id(29L).name("El Coll").build(),
                    Neighborhood.builder().id(30L).name("La Salut").build(),
                    Neighborhood.builder().id(31L).name("La Vila de Gràcia").build(),
                    Neighborhood.builder().id(32L).name("El Camp d'en Grassot i Gràcia Nova").build(),
                    Neighborhood.builder().id(33L).name("El Baix Guinardó").build(),
                    Neighborhood.builder().id(34L).name("Can Baró").build(),
                    Neighborhood.builder().id(35L).name("El Guinardó").build(),
                    Neighborhood.builder().id(36L).name("La Font d'en Fargues").build(),
                    Neighborhood.builder().id(37L).name("El Carmel").build(),
                    Neighborhood.builder().id(38L).name("La Teixonera").build(),
                    Neighborhood.builder().id(39L).name("Sant Genís dels Agudells").build(),
                    Neighborhood.builder().id(40L).name("Montbau").build(),
                    Neighborhood.builder().id(41L).name("La Vall d'Hebron").build(),
                    Neighborhood.builder().id(42L).name("La Clota").build(),
                    Neighborhood.builder().id(43L).name("Horta").build(),
                    Neighborhood.builder().id(44L).name("Vilapicina i la Torre Llobeta").build(),
                    Neighborhood.builder().id(45L).name("Porta").build(),
                    Neighborhood.builder().id(46L).name("El Turó de la Peira").build(),
                    Neighborhood.builder().id(47L).name("Can Peguera").build(),
                    Neighborhood.builder().id(48L).name("La Guineueta").build(),
                    Neighborhood.builder().id(49L).name("Canyelles").build(),
                    Neighborhood.builder().id(50L).name("Les Roquetes").build(),
                    Neighborhood.builder().id(51L).name("Verdun").build(),
                    Neighborhood.builder().id(52L).name("La Prosperitat").build(),
                    Neighborhood.builder().id(53L).name("La Trinitat Nova").build(),
                    Neighborhood.builder().id(54L).name("Torre Baró").build(),
                    Neighborhood.builder().id(55L).name("Ciutat Meridiana").build(),
                    Neighborhood.builder().id(56L).name("Vallbona").build(),
                    Neighborhood.builder().id(57L).name("La Trinitat Vella").build(),
                    Neighborhood.builder().id(58L).name("Baró de Viver").build(),
                    Neighborhood.builder().id(59L).name("El Bon Pastor").build(),
                    Neighborhood.builder().id(60L).name("Sant Andreu").build(),
                    Neighborhood.builder().id(61L).name("La Sagrera").build(),
                    Neighborhood.builder().id(62L).name("El Congrés i els Indians").build(),
                    Neighborhood.builder().id(63L).name("Navas").build(),
                    Neighborhood.builder().id(64L).name("El Camp de l'Arpa del Clot").build(),
                    Neighborhood.builder().id(65L).name("El Clot").build(),
                    Neighborhood.builder().id(66L).name("El Parc i la Llacuna del Poblenou").build(),
                    Neighborhood.builder().id(67L).name("La Vila Olímpica del Poblenou").build(),
                    Neighborhood.builder().id(68L).name("El Poblenou").build(),
                    Neighborhood.builder().id(69L).name("Diagonal Mar i el Front Marítim del Poblenou").build(),
                    Neighborhood.builder().id(70L).name("El Besòs i el Maresme").build(),
                    Neighborhood.builder().id(71L).name("Provençals del Poblenou").build(),
                    Neighborhood.builder().id(72L).name("Sant Martí de Provençals").build(),
                    Neighborhood.builder().id(73L).name("La Verneda i la Pau").build()
            );
            neighborhoodRepository.saveAll(barrios);
            log.info("¡Se han creado {} barrios!", barrios.size());
        } else {
            log.info("La tabla 'barrios' ya está poblada.");
        }

        if (complaintRepository.count() > 0) {
            log.info("La base de datos de feedbacks ya está poblada. No se crearán nuevos datos.");
            return;
        }

        log.info("Poblando la base de datos con feedbacks de prueba...");

        List<Neighborhood> neighborhoods = neighborhoodRepository.findAll();
        if (neighborhoods.isEmpty()) {
            log.warn("No se encontraron barrios en la base de datos. Asegúrate de que data.sql se ha ejecutado.");
            return;
        }

        List<String> quejas = Arrays.asList(
                "Demasiado ruido de turistas por la noche.", "Las calles están muy sucias.",
                "Imposible caminar por la acera, siempre bloqueada.", "Los precios en las tiendas son abusivos.",
                "Patinetes eléctricos de alquiler por todas partes, son un peligro.", "Falta de respeto en las zonas comunes.",
                "Fiestas en pisos turísticos hasta altas horas.", "El comercio local desaparece."
        );


        List<Long> hotspotIds = Arrays.asList(1L, 2L, 3L, 4L, 6L, 31L);

        int numeroDeFeedbacksACrear = 300;
        Random random = new Random();

        for (int i = 0; i < numeroDeFeedbacksACrear; i++) {
            Neighborhood barrioSeleccionado;


            if (random.nextInt(10) < 7) {
                Long hotspotId = hotspotIds.get(random.nextInt(hotspotIds.size()));
                barrioSeleccionado = neighborhoods.stream().filter(n -> n.getId() == hotspotId).findFirst().orElse(neighborhoods.get(0));
            } else {
                barrioSeleccionado = neighborhoods.get(random.nextInt(neighborhoods.size()));
            }

            String quejaRandom = quejas.get(random.nextInt(quejas.size()));

            Complaint complaint = Complaint.builder()
                    .userId(UUID.randomUUID())
                    .hoodId(barrioSeleccionado.getId())
                    .hoodName(barrioSeleccionado.getName())
                    .content(quejaRandom)
                    .build();

            complaintRepository.save(complaint);
        }

        log.info("¡Se han creado {} feedbacks de prueba!", numeroDeFeedbacksACrear);
    }
}