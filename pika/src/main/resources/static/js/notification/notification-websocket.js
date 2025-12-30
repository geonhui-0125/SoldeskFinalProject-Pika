import { handleNotificationClick } from './noti.js';

document.addEventListener('DOMContentLoaded', function() {
    // 현재 페이지의 프로토콜, 호스트, 포트를 사용하여 WebSocket URL을 구성
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const hostname = window.location.hostname;
    const port = window.location.port ? `:${window.location.port}` : '';
    const wsUrl = `${protocol}//${hostname}${port}/ws/notification`;

    const notificationSocket = new WebSocket(wsUrl);

    notificationSocket.onopen = function(event) {
        console.log("Notification WebSocket connected.", event);
    };

    notificationSocket.onmessage = function(event) {
        console.log("Notification received:", event.data);
        const notification = JSON.parse(event.data);

        // 1. 헤더의 알림 카운트 업데이트
        const notificationCountElement = document.getElementById('notification-count');
        if (notificationCountElement) {
            let currentCount = parseInt(notificationCountElement.textContent.replace('(', '').replace(')', '') || '0', 10);
            currentCount++;
            notificationCountElement.textContent = `(${currentCount})`;
            notificationCountElement.classList.add('new-notification'); // 새로운 알림 시각적 효과 (CSS 필요)
            // 잠시 후 클래스 제거하여 효과 초기화
            setTimeout(() => notificationCountElement.classList.remove('new-notification'), 3000);
        }

        // 2. header.js에서 알림 목록을 새로고침하도록 커스텀 이벤트 디스패치
        //    header.js의 알림 리스트 업데이트 함수가 이 이벤트를 감지하여 동작합니다.
        const notificationsUpdatedEvent = new CustomEvent('notifications-updated');
        document.dispatchEvent(notificationsUpdatedEvent);

        // 3. (선택 사항) 토스트 알림 표시
        showNotificationToast(notification.title, notification.content, notification.notificationId, notification.actionUrl);
    };

    notificationSocket.onclose = function(event) {
        console.log("Notification WebSocket disconnected.", event);
        // 연결이 끊겼을 때 재연결 로직 추가 가능
    };

    notificationSocket.onerror = function(error) {
        console.error("Notification WebSocket error:", error);
    };

    /**
     * 간단한 토스트 알림을 표시하는 헬퍼 함수
     * 이 함수를 위한 기본 CSS 스타일링이 필요할 수 있습니다.
     */
    function showNotificationToast(title, content, noti_id, url) {
        let toastContainer = document.getElementById('notification-toast-container');
        if (!toastContainer) {
            // 토스트 컨테이너가 없으면 생성
            toastContainer = document.createElement('div');
            toastContainer.id = 'notification-toast-container';
            Object.assign(toastContainer.style, {
                position: 'fixed',
                top: '20px',
                right: '20px',
                zIndex: '10000',
                display: 'flex',
                flexDirection: 'column',
                gap: '10px'
            });
            document.body.appendChild(toastContainer);
        }

        const toast = document.createElement('div');
        Object.assign(toast.style, {
            backgroundColor: '#4CAF50', // 녹색
            color: 'white',
            padding: '15px',
            borderRadius: '8px',
            boxShadow: '0 2px 10px rgba(0,0,0,0.2)',
            opacity: '0',
            transition: 'opacity 0.5s ease-in-out, transform 0.5s ease-in-out',
            transform: 'translateY(-20px)',
            minWidth: '250px',
            fontFamily: "'Malgun Gothic', sans-serif",
            cursor: 'pointer'
        });
        toast.innerHTML = `<strong>${title}</strong><p>${content}</p>`;

        if (url) {
            toast.addEventListener('click', () => {

                handleNotificationClick(noti_id, url);
                window.location.href = url;
                toast.remove(); // 클릭 후 토스트 제거
            });
        }

        toastContainer.appendChild(toast);

        // 토스트 애니메이션 (나타나기)
        setTimeout(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateY(0)';
        }, 100);

        // 5초 후 토스트 애니메이션 (사라지기)
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(-20px)';
            setTimeout(() => toast.remove(), 600); // 애니메이션 후 DOM에서 제거
        }, 5000);
    }
});
