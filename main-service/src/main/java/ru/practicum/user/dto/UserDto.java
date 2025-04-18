package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;

    @Email
    @Size(min = 6, max = 254, message = "олжен быть от 6 до 254")
    @NotBlank
    private String email;

    @Size(min = 2, max = 250, message = "должно быть от 2 до 250")
    @NotBlank(message = "имя не может быть пустым")
    private String name;
}
