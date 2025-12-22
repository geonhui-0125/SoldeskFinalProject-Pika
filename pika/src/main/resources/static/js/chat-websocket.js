// This script establishes a global WebSocket connection for the application.

// Make socket a global variable, so it can be accessed by other scripts (e.g., dm.html's script).
let socket;

function connectWebSocket() {
    // Avoid creating a new connection if one already exists and is open.
    if (socket && socket.readyState === WebSocket.OPEN) {
        console.log("WebSocket is already connected.");
        return;
    }

    // Establish the connection. The endpoint should be handled by your WebSockConfig.
    socket = new WebSocket("ws://" + window.location.host + "/ws/chat");

    socket.onopen = () => {
        console.log("WebSocket: Global connection established.");
        // You could send a user authentication token here if needed.
    };

    socket.onclose = () => {
        console.log("WebSocket: Global connection closed.");
        // Optional: Implement a reconnection logic with a delay.
    };

    socket.onerror = (error) => {
        console.error("WebSocket Error:", error);
    };

    // This is the core of the new architecture.
    // Instead of directly manipulating the DOM, it fires a global event.
    socket.onmessage = (event) => {
        try {
            const receivedMsg = JSON.parse(event.data);
            console.log("WebSocket: Message received, dispatching 'chat:message' event.", receivedMsg);

            // Dispatch a custom event with the message data.
            // Any part of the application can now listen for this event.
            document.dispatchEvent(new CustomEvent('chat:message', { detail: receivedMsg }));

        } catch (e) {
            console.error("Error parsing or dispatching WebSocket message:", e);
        }
    };
}

function disconnectWebSocket() {
    if (socket !== null) {
        socket.close();
        console.log("WebSocket: Disconnected.");
    }
}

// Attempt to connect as soon as the script is loaded.
// This assumes the user is authenticated. You might want to wrap this call
// in a check to see if the user is logged in before connecting.
connectWebSocket();