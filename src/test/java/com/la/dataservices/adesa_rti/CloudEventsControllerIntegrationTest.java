package com.la.dataservices.adesa_rti;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
// If you put properties into application-test.yml, also:
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestAsyncConfig.class)
class CloudEventsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    private String load(String path) throws Exception {
        var res = new ClassPathResource(path);
        try (var in = res.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    void clean() {
        vehicleRepository.deleteAll();
    }

    @Test
    @DisplayName("VehicleAdded v1 -> persists Vehicle")
    void vehicleAdded_v1_persists() throws Exception {
        String body = load("samples/vehicleAdded_v2.json");
        mockMvc.perform(post("/events")
                .contentType("application/cloudevents+json")
                .content(body))
                .andExpect(status().isOk());
        assertThat(vehicleRepository.findByVin("1C6SRFJT1LN201690")).isPresent();
    }

    @Test
    @DisplayName("VehicleUpdated v1 -> upserts Vehicle")
    void vehicleUpdated_v1_upserts() throws Exception {
        String add = load("samples/vehicleAdded_v2.json");
        mockMvc.perform(post("/events").contentType("application/cloudevents+json").content(add))
                .andExpect(status().isOk());

        String upd = load("samples/vehicleUpdated_v2.json");
        mockMvc.perform(post("/events").contentType("application/cloudevents+json").content(upd))
                .andExpect(status().isOk());

        var veh = vehicleRepository.findByVin("1C6SRFJT1LN201690").orElseThrow();
        assertThat(veh.getMakeName()).isEqualTo("Ram");
    }

    @Test
    @DisplayName("VehicleRemoved v1 -> still accepts (no persistence)")
    void vehicleRemoved_v1_accepts() throws Exception {
        String body = load("samples/vehicleRemoved_v2.json");
        mockMvc.perform(post("/events")
                .contentType("application/cloudevents+json")
                .content(body))
                .andExpect(status().isOk());
    }
}
