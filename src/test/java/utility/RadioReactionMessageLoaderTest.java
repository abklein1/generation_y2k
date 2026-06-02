package utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RadioReactionMessageLoader")
class RadioReactionMessageLoaderTest {

    @BeforeEach
    void reset() {
        RadioReactionMessageLoader.resetForTests();
    }

    @Test
    @DisplayName("Loads multiple like and dislike message templates")
    void testLoadsReactionMessages() {
        List<String> likes = RadioReactionMessageLoader.getLikeMessages();
        List<String> dislikes = RadioReactionMessageLoader.getDislikeMessages();

        assertTrue(likes.size() > 1, "Expected multiple like messages");
        assertTrue(dislikes.size() > 1, "Expected multiple dislike messages");
        assertTrue(likes.stream().allMatch(message -> message.contains("%s")));
        assertTrue(dislikes.stream().allMatch(message -> message.contains("%s")));
    }

    @Test
    @DisplayName("Random picks come from the configured message pools")
    void testPickConfiguredMessage() {
        String like = RadioReactionMessageLoader.pickLikeMessage(new Random(1L));
        String dislike =
                RadioReactionMessageLoader.pickDislikeMessage(new Random(2L));

        assertTrue(RadioReactionMessageLoader.getLikeMessages().contains(like));
        assertTrue(RadioReactionMessageLoader.getDislikeMessages()
                .contains(dislike));
    }

    @Test
    @DisplayName("Returned message lists are immutable")
    void testImmutableResults() {
        assertThrows(UnsupportedOperationException.class,
                () -> RadioReactionMessageLoader.getLikeMessages().add("Nope"));
        assertThrows(UnsupportedOperationException.class,
                () -> RadioReactionMessageLoader.getDislikeMessages()
                        .add("Nope"));
    }
}
