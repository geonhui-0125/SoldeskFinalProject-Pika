export function handleNotificationClick(notificationId, href) {
    console.log("Marking notification as read. ID: " + notificationId + ", URL: " + href);

    return fetch('/notifications/read/' + notificationId, {
        method: 'PUT'
    })
        .then(resp => {
            if (!resp.ok) {
                console.error('Failed to mark notification as read. Status: ' + resp.status);
            }
            window.location.href = href;
        })
        .catch(error => {
            console.error('Error in fetch call:', error);
            window.location.href = href;
        });
}