$(document).ready(function () {

    /** * 메인 카테고리 드롭다운:
     * #category-wrapper 내부의 #hamburger-icon 클릭 시 드롭다운 표시/숨김 (Toggle)
     */
    $("#hamburger-icon").click(function(event) {
        // 클릭 이벤트가 버블링되어 body나 document에 영향을 주는 것을 방지
        event.stopPropagation();

        $("#category-dropdown")
            .stop(true, true)
            // slideToggle을 사용하여 열고 닫는 기능을 하나로 처리
            .slideToggle(120);
    });

    // 드롭다운이 열려있는 상태에서 드롭다운 외부를 클릭하면 닫히도록 설정
    $(document).click(function(event) {
        // 클릭된 요소가 #category-wrapper 내부에 포함되어 있지 않다면
        if (!$(event.target).closest('#category-wrapper').length) {
            // 드롭다운 숨김
            if ($("#category-dropdown").is(":visible")) {
                $("#category-dropdown").slideUp(120);
            }
        }
    });


    /** * 서브 카테고리 (Submenu):
     * .category-item 클릭 시 서브메뉴 표시/숨김 (Toggle)
     */
    $(".category-item").click(function(event) {
        // 부모 요소의 클릭 이벤트(메인 드롭다운 닫기)가 실행되지 않도록 방지
        event.stopPropagation();

        // 현재 클릭된 아이템의 서브메뉴를 토글
        const $submenu = $(this).find(".submenu");

        // 다른 서브메뉴는 모두 닫음
        $(".submenu").not($submenu).slideUp(0);

        // 현재 서브메뉴 토글
        $submenu.stop(true, true).slideToggle(120);
    });

    $("#favorite-link").click(function() {
        location.href = "/user/mypage";
    })

    $("#top-link").click(function() {
        // HTML과 BODY에 0 (상단)
        $('html, body').scrollTop(0)
    });

    /*******************************
     알림(Notification)
     ********************************/

    let currentPage = 0;
    let totalPages = 0;

    function fetchNotifications(page) {
        $.ajax({
            url: "/notifications",
            type: "GET",
            data: { page: page, size: 2 },
            success: function (response) {
                const notificationItems = $("#notification-items");
                notificationItems.empty();
                totalPages = response.totalPages;
                currentPage = response.number;

                if (response.content.length === 0) {
                    notificationItems.append('<li>새 알림이 없습니다.</li>');
                } else {
                    response.content.forEach(function (notification) {
                        const notificationItem =
                            '<li>' +
                            '<div class="notification-item-link" data-url="' + notification.actionUrl + '" data-notification-id="' + notification.notificationId + '">' +
                            '<strong>' + notification.title + '</strong>' +
                            '<p>' + notification.content + '</p>' +
                            '<span>' + notification.timeAgo + '</span>' +
                            '</div>' +
                            '</li>';
                        notificationItems.append(notificationItem);
                    });
                }

                $("#prev-notification-page").prop('disabled', currentPage === 0);
                $("#next-notification-page").prop('disabled', currentPage >= totalPages - 1);
            },
            error: function () {
                const notificationItems = $("#notification-items");
                notificationItems.empty();
                notificationItems.append('<li>알림을 불러오는데 실패했습니다.</li>');
            }
        });
    }

    function updateNotificationCount() {
        $.ajax({
            url: "/notifications/count",
            type: "GET",
            success: function (count) {
                const countElement = $("#notification-count");
                if (count > 0) {
                    countElement.text('(' + count + ')');
                } else {
                    countElement.text('');
                }
            }
        });
    }
    
    // 페이지 로드 시 안읽은 알림 개수 업데이트
    updateNotificationCount();

    $("#notification-btn").click(function (e) {
        e.stopPropagation();
        var notificationList = $("#notification-list");
        notificationList.toggle();

        if (notificationList.is(":visible")) {
            fetchNotifications(0);
        }
    });
    
    $("#prev-notification-page").click(function() {
        if(currentPage > 0) {
            fetchNotifications(currentPage - 1);
        }
    });

    $("#next-notification-page").click(function() {
        if(currentPage < totalPages - 1) {
            fetchNotifications(currentPage + 1);
        }
    });

    document.getElementById('notification-items').addEventListener('click', function(e) {
        let target = e.target;
        // 클릭된 요소가 .notification-item-link 클래스를 가질 때까지 부모로 이동
        while (target && !target.classList.contains('notification-item-link')) {
            target = target.parentElement;
        }

        if (target && target.classList.contains('notification-item-link')) {
            const notificationId = target.getAttribute('data-notification-id');
            const href = target.getAttribute('data-url');

            console.log("Marking notification as read. ID: " + notificationId + ", URL: " + href);

            fetch('/notifications/read/' + notificationId, {
                method: 'PUT'
            })
            .then(resp => {
                if (!resp.ok) {
                    console.error('Failed to mark notification as read. Status: ' + resp.status);
                }
                // 성공하든 실패하든 링크로 이동
                window.location.href = href;
            })
            .catch(error => {
                console.error('Error in fetch call:', error);
                // 에러 발생 시에도 링크로 이동
                window.location.href = href;
            });
        }
    });

    $(document).click(function () {
        if ($("#notification-list").is(":visible")) {
            $("#notification-list").hide();
        }
    });

    $("#notification-list").click(function (e) {
        e.stopPropagation();
    });
});