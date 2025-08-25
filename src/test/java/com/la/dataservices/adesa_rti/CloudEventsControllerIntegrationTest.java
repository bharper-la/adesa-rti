package com.la.dataservices.adesa_rti;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(resolver = CloudEventsProfileResolver.class)
@org.springframework.context.annotation.Import(TestAsyncConfig.class)
class CloudEventsControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    VehicleRepositoryJson vehicleRepositoryJson;
    @Autowired Environment env;

    private String load(String path) throws Exception {
        var res = new ClassPathResource(path);
        try (var in = res.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private boolean isLegacyJson() {
        return Arrays.asList(env.getActiveProfiles()).contains("legacy-json");
    }

    @BeforeEach
    void clean() {
        vehicleRepositoryJson.deleteAll();
    }

    @Test
    @DisplayName("VehicleAdded v2 -> persists Vehicle")
    void vehicleAdded_v2_persists() throws Exception {
        String body = load("samples/vehicleAdded_v2.json");

        mockMvc.perform(post("/events")
                        .contentType("application/cloudevents+json")
                        .content(body))
                .andExpect(status().isOk());

        var opt = vehicleRepositoryJson.findByVin("1C6SRFJT1LN201690");
        assertThat(opt).isPresent();

        // Optional, profile-aware checks:
        var v = opt.get();
        assertThat(v.getMakeName()).isEqualTo("Ram");
        // if you want to assert legacy-json fields:
        // if (isLegacyJson()) assertThat(v.getAdditionalVehicleImageUrlsJson()).isNotBlank();
    }

    @Test
    @DisplayName("VehicleUpdated v2 -> upserts Vehicle")
    void vehicleUpdated_v2_upserts() throws Exception {
        String add = load("samples/vehicleAdded_v2.json");
        mockMvc.perform(post("/events").contentType("application/cloudevents+json").content(add))
                .andExpect(status().isOk());

        String upd = load("samples/vehicleUpdated_v2.json");
        mockMvc.perform(post("/events").contentType("application/cloudevents+json").content(upd))
                .andExpect(status().isOk());

        var veh = vehicleRepositoryJson.findByVin("4T3ZE11A19U005674").orElseThrow();
        assertThat(veh.getMakeName()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("VehicleRemoved v2 -> accepts (no persistence)")
    void vehicleRemoved_v2_accepts() throws Exception {
        String body = load("samples/vehicleRemoved_v2.json");
        mockMvc.perform(post("/events")
                        .contentType("application/cloudevents+json")
                        .content(body))
                .andExpect(status().isOk());
    }
}
