package com.ragchat.rag_chat_storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@EnableR2dbcAuditing
@SpringBootApplication
public class RagChatStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagChatStorageApplication.class, args);
	}

}
