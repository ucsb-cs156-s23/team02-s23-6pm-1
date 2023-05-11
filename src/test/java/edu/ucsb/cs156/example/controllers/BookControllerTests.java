package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Book;
import edu.ucsb.cs156.example.repositories.BookRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = BookController.class)
@Import(TestConfig.class)
public class BookControllerTests extends ControllerTestCase {

        @MockBean
        BookRepository bookRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/book/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/book/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/book/all"))
                                .andExpect(status().is(200)); // logged
        }

        
        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/book?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/book/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/book/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/book/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions
        
        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Book greenEggs = Book.builder()
                                .name("GreenEggsAndHam")
                                .genre("Poetry")
                                .author("DrSeuss")
                                .build();

                when(bookRepository.findById(1L)).thenReturn(Optional.of(greenEggs));

                // act
                MvcResult response = mockMvc.perform(get("/api/book?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(bookRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(greenEggs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(bookRepository.findById(-1L)).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/book?id=-1"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(bookRepository, times(1)).findById(eq(-1L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Book with id -1 not found", json.get("message"));
        }
        
        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_book() throws Exception {

                // arrange

                Book greenEggs = Book.builder()
                                .name("GreenEggsAndHam")
                                .genre("Poetry")
                                .author("DrSeuss")
                                .build();

                Book hrrPtr = Book.builder()
                                .name("HarryPotter")
                                .genre("Fantasy")
                                .author("JKRowling")
                                .build();

                ArrayList<Book> expectedBooks = new ArrayList<>();
                expectedBooks.addAll(Arrays.asList(greenEggs, hrrPtr));

                when(bookRepository.findAll()).thenReturn(expectedBooks);

                // act
                MvcResult response = mockMvc.perform(get("/api/book/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(bookRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedBooks);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_book() throws Exception {
                // arrange

                Book greenEggs = Book.builder()
                                .name("GreenEggsAndHam")
                                .genre("Poetry")
                                .author("DrSeuss")
                                .build();

                when(bookRepository.save(eq(greenEggs))).thenReturn(greenEggs);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/book/post?name=GreenEggsAndHam&genre=Poetry&author=DrSeuss")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(bookRepository, times(1)).save(greenEggs);
                String expectedJson = mapper.writeValueAsString(greenEggs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
 
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_book() throws Exception {
                // arrange

                Book greenEggs = Book.builder()
                                .name("GreenEggsAndHam")
                                .genre("Poetry")
                                .author("DrSeuss")
                                .build();

                when(bookRepository.findById(eq(1L))).thenReturn(Optional.of(greenEggs));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/book?id=1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(1L);
                verify(bookRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 1 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_book_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(bookRepository.findById(eq(100L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/book?id=100")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(100L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 100 not found", json.get("message"));
        }
        //wip

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_book() throws Exception {
                // arrange

                Book greenEggsOrig = Book.builder()
                                .name("GreenEggsAndHam")
                                .genre("Poetry")
                                .author("DrSeuss")
                                .build();

                Book greenEggsEdited = Book.builder()
                                .name("GreenEggsAndHamEdited")
                                .genre("PoetryEdited")
                                .author("DrSeussEdited")
                                .build();

                String requestBody = mapper.writeValueAsString(greenEggsEdited);

                when(bookRepository.findById(eq(25L))).thenReturn(Optional.of(greenEggsOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/book?id=25")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(25L);
                verify(bookRepository, times(1)).save(greenEggsEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_book_that_does_not_exist() throws Exception {
                // arrange

                Book greenEggsEdited = Book.builder()
                                .name("GreenEggsAndHamEdited")
                                .genre("PoetryEdited")
                                .author("DrSeussEdited")
                                .build();

                String requestBody = mapper.writeValueAsString(greenEggsEdited);

                when(bookRepository.findById(eq(25L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/book?id=25")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(25L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 25 not found", json.get("message"));

        }

}
