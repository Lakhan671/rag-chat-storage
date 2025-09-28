package com.ragchat.rag_chat_storage.dto;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private Boolean isFavorite;
}
