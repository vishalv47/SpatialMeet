package com.spatialmeet.controller;

import com.spatialmeet.dto.response.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RestController
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GuestController {
    
    private static final Logger logger = LoggerFactory.getLogger(GuestController.class);

    @PostMapping("/enter")
    public ResponseEntity<?> enterAsGuest(@RequestParam(required = false) String displayName) {
        logger.info("Guest login attempt with displayName: {}", displayName);
        try {
            // Generate a temporary guest session
            String guestId = "guest_" + UUID.randomUUID().toString().substring(0, 8);
            String guestDisplayName = displayName != null && !displayName.trim().isEmpty() 
                ? displayName.trim() 
                : "Guest " + guestId.substring(6);
            
            // Create guest session data
            GuestSessionResponse guestSession = new GuestSessionResponse();
            guestSession.setGuestId(guestId);
            guestSession.setDisplayName(guestDisplayName);
            guestSession.setSessionToken("guest_token_" + UUID.randomUUID().toString());
            guestSession.setMessage("Welcome as guest user!");
            
            logger.info("Guest session created successfully: {}", guestId);
            return ResponseEntity.ok(guestSession);
        } catch (Exception e) {
            logger.error("Error creating guest session", e);
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error creating guest session: " + e.getMessage()));
        }
    }

    // Inner class for guest session response
    public static class GuestSessionResponse {
        private String guestId;
        private String displayName;
        private String sessionToken;
        private String message;
        private boolean isGuest = true;

        // Getters and setters
        public String getGuestId() {
            return guestId;
        }

        public void setGuestId(String guestId) {
            this.guestId = guestId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isGuest() {
            return isGuest;
        }

        public void setGuest(boolean guest) {
            isGuest = guest;
        }
    }
}
