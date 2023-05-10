package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;
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

@WebMvcTest(controllers = RestaurantsController.class)
@Import(TestConfig.class)
public class RestaurantsControllerTests extends ControllerTestCase {

    @MockBean
    RestaurantRepository restaurantRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/restaurants/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/restaurants/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/restaurants/all"))
                .andExpect(status().is(200)); // logged
    }

    // Authorization tests for /api/restaurants/post

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/restaurants/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/restaurants/post"))
                .andExpect(status().is(403)); // only admins can post
    }

    // // Tests with mocks for database actions

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_user_can_get_all_restaurants() throws Exception {

        // arrange

        var restaurant1 = Restaurant.builder()
                .name("a restaurant")
                .address("1234 State St")
                .description("a description")
                .build();

        var restaurant2 = Restaurant.builder()
                .name("another restaurant")
                .address("5678 State St")
                .description("another description")
                .build();

        var expectedDates = new ArrayList<>(Arrays.asList(restaurant1, restaurant2));

        when(restaurantRepository.findAll()).thenReturn(expectedDates);

        // act
        MvcResult response = mockMvc.perform(get("/api/restaurants/all"))
                .andExpect(status().isOk())
                .andReturn();

        // assert

        verify(restaurantRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedDates);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = {"ADMIN", "USER"})
    @Test
    public void an_admin_user_can_post_a_new_restaurant() throws Exception {
        // arrange

        var restaurant1 = Restaurant.builder()
                .name("a restaurant")
                .address("1234 State St")
                .description("a description")
                .build();

        when(restaurantRepository.save(eq(restaurant1))).thenReturn(restaurant1);

        // act
        MvcResult response = mockMvc.perform(

                        post("/api/restaurants/post?name=%s&address=%s&description=%s".formatted(restaurant1.getName(), restaurant1.getAddress(), restaurant1.getDescription()))
                                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(restaurantRepository, times(1)).save(restaurant1);
        String expectedJson = mapper.writeValueAsString(restaurant1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = {"ADMIN", "USER"})
    @Test
    public void admin_can_delete_a_date() throws Exception {
        // arrange

        var restaurant1 = Restaurant.builder()
                .name("a restaurant")
                .address("1234 State St")
                .description("a description")
                .build();


        when(restaurantRepository.findById(eq(15L))).thenReturn(Optional.of(restaurant1));

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/restaurants?id=15")
                                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(restaurantRepository, times(1)).findById(15L);
        verify(restaurantRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("Restaurant with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = {"ADMIN", "USER"})
    @Test
    public void admin_tries_to_delete_non_existant_restaurant_and_gets_right_error_message()
            throws Exception {
        // arrange

        when(restaurantRepository.findById(eq(15L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/restaurants?id=15")
                                .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(restaurantRepository, times(1)).findById(15L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Restaurant with id 15 not found", json.get("message"));
    }

}