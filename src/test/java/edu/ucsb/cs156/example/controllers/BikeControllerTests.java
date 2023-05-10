package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Bike;
import edu.ucsb.cs156.example.repositories.BikeRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BikeController.class)
@Import(TestConfig.class)
public class BikeControllerTests extends ControllerTestCase {

        @MockBean
        BikeRepository bikeRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/bikes/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/bikes/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/bikes/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/bikes?id=7"))
                        .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/bikes/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/bikes/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/bikes/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                Bike bike = Bike.builder()
                        .model("Among Us")
                        .manufacturer("Innersloth")
                        .numGears(69)
                        .id(0L)
                        .build();

                when(bikeRepository.findById(eq(0L))).thenReturn(Optional.of(bike));

                // act
                MvcResult response = mockMvc.perform(get("/api/bikes?id=0"))
                        .andExpect(status().isOk()).andReturn();

                // assert

                verify(bikeRepository, times(1)).findById(eq(0L));
                String expectedJson = mapper.writeValueAsString(bike);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(bikeRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/bikes?id=7"))
                        .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(bikeRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Bike with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_bikes() throws Exception {

                // arrange

                Bike bike1 = Bike.builder()
                        .model("Among Us")
                        .manufacturer("Innersloth")
                        .numGears(69)
                        .id(0L)
                        .build();

                Bike bike2 = Bike.builder()
                        .model("Deez nuts")
                        .manufacturer("Vine Boom")
                        .numGears(420)
                        .id(1L)
                        .build();

                ArrayList<Bike> expectedBikes = new ArrayList<>(Arrays.asList(bike1, bike2));

                when(bikeRepository.findAll()).thenReturn(expectedBikes);

                // act
                MvcResult response = mockMvc.perform(get("/api/bikes/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(bikeRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedBikes);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_commons() throws Exception {
                // arrange

                Bike bike = Bike.builder()
                        .model("Among Us")
                        .manufacturer("Innersloth")
                        .numGears(69)
                        .id(0L)
                        .build();

                when(bikeRepository.save(eq(bike))).thenReturn(bike);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/bikes/post?model=Among Us&manufacturer=Innersloth&numGears=69&id=0")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(bikeRepository, times(1)).save(bike);
                String expectedJson = mapper.writeValueAsString(bike);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
}
