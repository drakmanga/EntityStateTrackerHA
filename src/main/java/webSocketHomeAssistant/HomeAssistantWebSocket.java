package webSocketHomeAssistant;

import com.google.gson.JsonElement;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;

public class HomeAssistantWebSocket extends WebSocketClient {
    private static final String HOME_ASSISTANT_WS_URL = "ws://192.168.0.138:8123/api/websocket";
    private static final String TOKEN = "insert_your_token"; // Sostituisci con il tuo token

    public HomeAssistantWebSocket(URI serverUri) {
        super(serverUri);
    }

    public static void main(String[] args) {
        try {
            //Connect to webSocket
            HomeAssistantWebSocket client = new HomeAssistantWebSocket(new URI(HOME_ASSISTANT_WS_URL));
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to Home Assistant WebSocket!");

        //Auth with token
        JsonObject authMessage = new JsonObject();
        authMessage.addProperty("type", "auth");
        authMessage.addProperty("access_token", TOKEN);
        send(authMessage.toString());
    }

    @Override
    public void onMessage(String message) {
//        System.out.println("Received: " + message);
        JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();

        //Check if authentication is successful
        if (jsonMessage.has("type") && "auth_ok".equals(jsonMessage.get("type").getAsString())) {
            System.out.println("Authentication successful!");

            // Subscribe to state changes
            JsonObject subscribeMessage = new JsonObject();
            subscribeMessage.addProperty("id", 1);
            subscribeMessage.addProperty("type", "subscribe_events");
            subscribeMessage.addProperty("event_type", "state_changed");
            send(subscribeMessage.toString());
        }
        //Handle "state_changed" events
        if (jsonMessage.has("event")) {
            JsonObject event = jsonMessage.getAsJsonObject("event");

            if (event.has("data")) {
                JsonObject eventData = event.getAsJsonObject("data");
//                System.out.println(eventData);

                // Check if "entity_id" exists and is a primitive
                if (eventData.has("entity_id") && eventData.get("entity_id").isJsonPrimitive()) {
                    String entityId = eventData.get("entity_id").getAsString();

                    // Ensure "new_state" exists and is an object
                    if (eventData.has("new_state") && eventData.get("new_state").isJsonObject()) {
                        JsonObject newStateObject = eventData.getAsJsonObject("new_state");


                        // Ensure "state" exists and is a primitive
                        if (newStateObject.has("state") && newStateObject.get("state").isJsonPrimitive()) {
                            String newState = newStateObject.get("state").getAsString();

                            JsonObject attributesObject = newStateObject.getAsJsonObject("attributes");
                            String elementFriendlyName = attributesObject.get("friendly_name").getAsString();

                            JsonObject contextObject = newStateObject.getAsJsonObject("context");
                            JsonElement elementUserId = contextObject.get("user_id");

                            String userIdAction = (elementUserId != null && !elementUserId.isJsonNull()) ? elementUserId.getAsString() : "Sconosciuto";

                            //Convert data into json

                            EntityService entityService = new EntityService();

                            if (newState.equals("on") || newState.equals("off")) {
                                entityService.startSshConnection();
                                entityService.startConnection();
                                entityService.insertEntityActivity(entityId, newState, elementFriendlyName, userIdAction);
                                entityService.closeConnection();
                                entityService.disconnectSshConnection();
                            }
                        }
                    }
                }
            }
        }
    }

    //Handle disconnect reason
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from Home Assistant WebSocket. Reason: " + reason);
    }

    //Handle webSocket error
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket Error: " + ex.getMessage());
    }
}
