// This script establishes a global STOMP over SockJS connection for the application.

// Make stompClient a global variable, so it can be accessed by other scripts (e.g., dm.html's script).
let stompClient = null;

function connectStompWebSocket() {
    // Avoid creating a new connection if one already exists and is open.
    if (stompClient && stompClient.connected) {
        console.log("STOMP WebSocket is already connected.");
        return;
    }

    // Use SockJS to provide WebSocket emulation for older browsers.
    // The endpoint '/ws/chat' is configured in WebSockConfig.java
    const socket = new SockJS("/ws/chat");
    stompClient = Stomp.over(socket);

    // Optional: STOMP client debug logs
    stompClient.debug = function(str) {
        console.log("STOMP Debug: " + str);
    };

    stompClient.connect({}, function(frame) {
        console.log("STOMP WebSocket: Global connection established: " + frame);

        // Subscribe to user-specific queue for private messages
        // The destination /user/queue/messages is configured implicitly by Spring Security
        // and our MessageService.java for convertAndSendToUser.
        stompClient.subscribe('/user/queue/messages', function(message) {
            console.log("!!!STOMP message received from server (raw):", message.body); // 추가된 로그
            try {
                const receivedMsg = JSON.parse(message.body);
                console.log("STOMP WebSocket: Message received, dispatching 'chat:message' event.", receivedMsg);

                // Dispatch a custom event with the message data.
                // Any part of the application can now listen for this event.
                document.dispatchEvent(new CustomEvent('chat:message', { detail: receivedMsg }));

            } catch (e) {
                console.error("Error parsing or dispatching STOMP message:", e);
            }
        });

    }, function(error) {
        console.error("STOMP WebSocket Error: " + error);
        // Optional: Implement a reconnection logic with a delay.
        // For now, just log the error.
    });
}

function disconnectStompWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect();
        console.log("STOMP WebSocket: Disconnected.");
    }
}

// Attempt to connect as soon as the script is loaded.
// This assumes the user is authenticated (handled by sec:authorize).
connectStompWebSocket();