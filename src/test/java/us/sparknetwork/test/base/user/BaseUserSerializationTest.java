package us.sparknetwork.test.base.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import us.sparknetwork.base.user.BaseUser;
import us.sparknetwork.base.user.User;

import java.util.UUID;

public class BaseUserSerializationTest {

    private static UUID userId = UUID.fromString("6add6e1d-1766-4370-b25d-9ee8d8fb45b9");

    private String oldSerializedUser = "{\"nick\":null,\"nameHistory\":[],\"lastJoin\":0,\"lastServerId\":null,\"online\":false,\"addressHistory\":[],\"lastSpeakTime\":0,\"globalChatVisible\":true,\"staffChatVisible\":false,\"inStaffChat\":false,\"lastPrivateMessageReplier\":null,\"socialSpyVisible\":false,\"ignoredPlayers\":[],\"privateMessagesVisible\":true,\"godModeEnabled\":false,\"vanished\":false,\"freezed\":false,\"friendsLimit\" : 0, \"friends\": [], \"_id\":\"6add6e1d-1766-4370-b25d-9ee8d8fb45b9\"}";
    private String serializedUser = "{\"nick\":null,\"nameHistory\":[],\"lastJoin\":0,\"lastServerId\":null,\"online\":false,\"addressHistory\":[],\"lastSpeakTime\":0,\"globalChatVisible\":true,\"staffChatVisible\":false,\"inStaffChat\":false,\"lastPrivateMessageReplier\":null,\"socialSpyVisible\":false,\"ignoredPlayers\":[],\"privateMessagesVisibility\": \"ALL\",\"godModeEnabled\":false,\"vanished\":false,\"freezed\":false,\"friendsLimit\" : 0, \"friends\": [], \"_id\":\"6add6e1d-1766-4370-b25d-9ee8d8fb45b9\"}";

    @Test
    public void serializeBaseUser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC));

        User user = new BaseUser(userId);

        mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, user);
    }

    @Test
    public void deserializeOldVersionBaseUser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC));

        User user = mapper.readValue(oldSerializedUser, User.class);
    }

    @Test
    public void deserializeBaseUser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC));

        User user = mapper.readValue(serializedUser, User.class);
    }
}
