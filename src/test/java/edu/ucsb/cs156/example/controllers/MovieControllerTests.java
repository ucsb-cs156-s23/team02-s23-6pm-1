package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Movie;
import edu.ucsb.cs156.example.repositories.MovieRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

@WebMvcTest(controllers = MovieController.class)
@Import(TestConfig.class)
public class MovieControllerTests extends ControllerTestCase {

        @MockBean
        MovieRepository movieRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/movies/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/movies?id=7"))
                        .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/movies/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/movies/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/movies/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                Movoe movie = Movie.builder()
                        .name("good movie")
                        .synopsis("good plot")
                        .castMembers("good cast")
                        .id(0L)
                        .build();

                when(movieRepository.findById(eq(0L))).thenReturn(Optional.of(movie));

                // act
                MvcResult response = mockMvc.perform(get("/api/movies?id=0"))
                        .andExpect(status().isOk()).andReturn();

                // assert

                verify(movieRepository, times(1)).findById(eq(0L));
                String expectedJson = mapper.writeValueAsString(movie);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(movieRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/movies?id=7"))
                        .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(movieRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Movie with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_movies() throws Exception {

                // arrange

                Movie movie1 = Movie.builder()
                        .name("good movie")
                        .synopsis("good plot")
                        .castMembers("good cast")
                        .id(0L)
                        .build();

                Movie movie2 = Movie.builder()
                        .name("ok movie")
                        .synopsis("ok plot")
                        .castMembers("ok cast")
                        .id(0L)
                        .build();

                ArrayList<Movie> expectedMovies = new ArrayList<>(Arrays.asList(movie1, movie2));

                when(movieRepository.findAll()).thenReturn(expectedMovies);

                // act
                MvcResult response = mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(movieRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedMovies);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_commons() throws Exception {
                // arrange

                Movie movie = Movie.builder()
                        .name("good movie")
                        .synopsis("good plot")
                        .castMembers("good cast")
                        .id(0L)
                        .build();

                when(movieRepository.save(eq(movie))).thenReturn(movie);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/movies/post?name=good movie&synopsis=good plot&castMembers=good cast")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(movieRepository, times(1)).save(movie);
                String expectedJson = mapper.writeValueAsString(movie);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_movie() throws Exception {
                // arrange

                Movie movie = Movie.builder()
                        .name("good movie")
                        .synopsis("good plot")
                        .castMembers("good cast")
                        .id(0L)
                        .build();

                when(movieRepository.findById(eq(0L))).thenReturn(Optional.of(movie));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/movies?id=0")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

                // assert
                verify(movieRepository, times(1)).findById(0L);
                verify(movieRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Movie with id 0 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_movie_and_gets_right_error_message()
                throws Exception {
                // arrange

                when(movieRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/movies?id=15")
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(Repository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Movie with id 15 not found", json.get("message"));
        }
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_movie() throws Exception {
                // arrange
                Movie movie = Movie.builder()
                        .name("good movie")
                        .synopsis("good plot")
                        .castMembers("good cast")
                        .id(0L)
                        .build();

                Movie movieEdited = Movie.builder()
                        .name("gooder movie")
                        .synopsis("gooder plot")
                        .castMembers("gooder cast")
                        .id(0L)
                        .build();

                String requestBody = mapper.writeValueAsString(movieEdited);

                when(movieRepository.findById(eq(0L))).thenReturn(Optional.of(movie));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/movies?id=0")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

                // assert
                verify(movieRepository, times(1)).findById(0L);
                verify(movieRepository, times(1)).save(movieEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_movie_that_does_not_exist() throws Exception {
                // arrange
                Movie movieEdited = Movie.builder()
                        .name("gooder movie")
                        .synopsis("gooder plot")
                        .castMembers("gooder cast")
                        .id(0L)
                        .build();

                String requestBody = mapper.writeValueAsString(movieEdited);

                when(movieRepository.findById(eq(0L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/movies?id=0")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(movieRepository, times(1)).findById(0L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Movie with id 0 not found", json.get("message"));

        }

}
