package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Controller.AddressController;
import org.example.ecommerce.DTO.AddAddressDto;
import org.example.ecommerce.DTO.ShowAddressDto;
import org.example.ecommerce.Exception.AddressAlreadyExistsException;
import org.example.ecommerce.Exception.AddressDoesNotMatchUserException;
import org.example.ecommerce.Exception.AddressNotFoundException;
import org.example.ecommerce.Exception.GlobalExceptionHandler;
import org.example.ecommerce.Mappers.AddressMapper;
import org.example.ecommerce.Model.Address;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.AddressRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Service.AddressService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @Test
    void addAddressToCurrentUser_shouldAddAddressToUserSuccessfully() throws Exception {
        String jsonContent = """
                {
                    "streetAddress": "Druzhba 40",
                    "city": "Plovdiv",
                    "state": "Plovdiv",
                    "postalCode": "4002",
                    "country": "Bulgaria"
                }
                """;
        AddAddressDto addAddressDto = new AddAddressDto("Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        ShowAddressDto showAddressDto = new ShowAddressDto(
                1L,
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria",
                "SHIPPING_AND_BILLING",
                false
        );

        when(addressService.addAddressToCurrentUser(addAddressDto)).thenReturn(showAddressDto);

        mockMvc.perform(post("/api/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)).andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.streetAddress").value("Druzhba 40"))
                .andExpect(jsonPath("$.city").value("Plovdiv"))
                .andExpect(jsonPath("$.addressType").value("SHIPPING_AND_BILLING"))
                .andExpect(jsonPath("$.isDefault").value(false));
    }

    @Test
    void addAddressToCurrentUser_whenInvalidAddress_returns400() throws Exception {
        String jsonContent = """
                {
                    "streetAddress": "",
                    "city": "Plovdiv",
                    "state": "Plovdiv",
                    "postalCode": "4002",
                    "country": "Bulgaria"
                }
                """;
        AddAddressDto addAddressDto = new AddAddressDto("", null, null, null, null);

        mockMvc.perform(post("/api/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        ).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].defaultMessage").value("Street address cannot be empty"));
    }

    @Test
    void addAddressToCurrentUser_whenDuplicateAddress_throwsException() throws Exception {
        String jsonContent = """
                {
                    "streetAddress": "Druzhba 40",
                    "city": "Plovdiv",
                    "state": "Plovdiv",
                    "postalCode": "4002",
                    "country": "Bulgaria"
                }
                """;

        AddAddressDto addAddressDto = new AddAddressDto("Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        when(addressService.addAddressToCurrentUser(addAddressDto))
                .thenThrow(new AddressAlreadyExistsException("Address already exists"));

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
                ).andExpect(status().isBadRequest())
                .andExpect(content().string("Address already exists"));
    }

    @Test
    void deleteAddressOfCurrentUser_shouldDeleteAddressSuccessfully() throws Exception {
        Long id = 1L;
        doNothing().when(addressService).deleteAddressOfCurrentUser(id);

        mockMvc.perform(delete("/api/addresses/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(addressService, times(1)).deleteAddressOfCurrentUser(id);
        verifyNoMoreInteractions(addressService);
    }

    @Test
    void deleteAddressOfCurrentUser_shouldThrowException_ifAddressIsDuplicated() throws Exception {
        Long id = 1L;
        doThrow(new AddressAlreadyExistsException("Address already exists"))
                .when(addressService).deleteAddressOfCurrentUser(id);

        mockMvc.perform(delete("/api/addresses/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Address already exists"));

        verify(addressService, times(1)).deleteAddressOfCurrentUser(id);
        verifyNoMoreInteractions(addressService);
    }

    @Test
    void getAddressOfCurrentUserByAddressId_whenFound_returns200AndBody() throws Exception {
        Long id = 1L;
        ShowAddressDto showAddressDto = new ShowAddressDto(
                1L,
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria",
                "SHIPPING_AND_BILLING",
                false
        );

        String jsonContent = """
                {
                    "streetAddress": "Druzhba 40",
                    "city": "Plovdiv",
                    "state": "Plovdiv",
                    "postalCode": "4002",
                    "country": "Bulgaria"
                }
                """;

        when(addressService.getAddressOfCurrentUserByAddressId(id)).thenReturn(showAddressDto);

        mockMvc.perform(get("/api/addresses/1", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.streetAddress").value("Druzhba 40"))
                .andExpect(jsonPath("$.city").value("Plovdiv"))
                .andExpect(jsonPath("$.addressType").value("SHIPPING_AND_BILLING"))
                .andExpect(jsonPath("$.isDefault").value(false));

        verify(addressService, times(1)).getAddressOfCurrentUserByAddressId(id);
        verifyNoMoreInteractions(addressService);
    }

    @Test
    void getAddressOfCurrentUserByAddressId_whenNotFound_returns404() throws Exception {
        Long id = 1L;
        when(addressService.getAddressOfCurrentUserByAddressId(id))
                .thenThrow(new AddressNotFoundException("Address not found"));

        mockMvc.perform(get("/api/addresses/1", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Address not found"));

        verify(addressService, times(1)).getAddressOfCurrentUserByAddressId(id);
        verifyNoMoreInteractions(addressService);
    }

    @Test
    void getAddressOfCurrentUserByAddressId_whenDifferentUser_returns403() throws Exception {
        Long id = 1L;
        when(addressService.getAddressOfCurrentUserByAddressId(id))
                .thenThrow(new AddressDoesNotMatchUserException("Address does not match the address of the user!"));

        mockMvc.perform(get("/api/addresses/1", id))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Address does not match the address of the user!"));

        verify(addressService, times(1)).getAddressOfCurrentUserByAddressId(id);
        verifyNoMoreInteractions(addressService);
    }

    @Test
    void getAllAddressesOfCurrentUser_whenEmpty_returnsEmptyList() throws Exception {
        when(addressService.getAllAddressesOfCurrentUser()).thenReturn(List.of());

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(addressService, times(1)).getAllAddressesOfCurrentUser();
        assertThat(addressService.getAllAddressesOfCurrentUser()).isEmpty();
    }

    @Test
    void getAllAddressesOfCurrentUser_whenNotEmpty_returnsListOfAddresses() throws Exception {
        List<ShowAddressDto> addresses = List.of(new ShowAddressDto(1L,
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria",
                "SHIPPING_AND_BILLING",
                false));

        when(addressService.getAllAddressesOfCurrentUser()).thenReturn(addresses);

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));

        verify(addressService, times(1)).getAllAddressesOfCurrentUser();
        assertThat(addressService.getAllAddressesOfCurrentUser()).isEqualTo(addresses);
    }


}
