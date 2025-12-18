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
        if($(event.target).is('a')){
            return;
        }
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
     카테고리 검색기능
     *******************************/

    const $searchCategoryDisplay = $('#search-category-display');
    const $searchCategoryDisplaySpan = $('#search-category-display span');
    const $searchCategoryInput = $('#search-category-input');
    const $categoryDropdown = $('#category-dropdown');

    //메인 카테고리 및 서브 카테고리 링크 클릭 시
    $('#category-dropdown').on('click', '.category-item a, .submenu a', function (event){
        event.preventDefault(); //기본 링크 이동 방지
        event.stopPropagation(); //상위 요소로의 이벤트 버블링 방지

        const fullCategoryName = $(this).text(); //메인 or 메인>서브 카테고리
        const categoryValue = $(this).data('category-value');

        $searchCategoryDisplaySpan.text(fullCategoryName); //선택한 카테고리 표시
        $searchCategoryInput.val(categoryValue) //검색 폼 hidden input 값 업데이트(data-category-value사용)
        $categoryDropdown.slideUp(120); //카테고리 드롭다운
        $searchForm.submit(); //키워드 없이 카테고리만으로 검색 실행
    });

    //"전체" 카테고리 표시 클릭시 필터 초기화
    $searchCategoryDisplay.on('click', function (event){
        if($searchCategoryDisplaySpan.text() !== '전체'){
            event.stopPropagation();
            $searchCategoryDisplaySpan.text('전체');
            $searchCategoryInput.val(''); //hidden input 값 초기화
            $searchForm.submit();
        }
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

    const notificationItemsEl = document.getElementById('notification-items');
    if (notificationItemsEl) {
        notificationItemsEl.addEventListener('click', function(e) {
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
    }

    $(document).click(function () {
        if ($("#notification-list").is(":visible")) {
            $("#notification-list").hide();
        }
    });

    $("#notification-list").click(function (e) {
        e.stopPropagation();
    });

    /*******************************
         인기 검색어 소트 기능
     *******************************/

    const $searchBarInput = $('#search-bar input[name="keyword"]');
    const $popularSearchesContainer = $('#popular-searches-container');
    const $popularSearchesList = $('#popular-searches-list');
    const $searchForm = $('#search-bar form');

    let popularKeywordsData = [];

    function fetchAndDisplayPopularKeywords() {
        if (popularKeywordsData.length === 0) { // 캐시 사용 활성화
            $.ajax({
                url: '/search/popular',
                method: 'GET',
                success: function (data) {
                    popularKeywordsData = data;
                    $popularSearchesList.empty();
                    if (data && data.length > 0) {
                        data.forEach(function (searchItem) {
                            const listItem = $('<li><a href="#"></a></li>');
                            listItem.find('a').text(searchItem.keyword).attr('data-keyword', searchItem.keyword);
                            $popularSearchesList.append(listItem);
                        });
                    } else {
                        $popularSearchesList.append('<li><span class="no-results">인기 검색어가 없습니다.</span></li>');
                    }
                    $popularSearchesContainer.slideDown(120);
                },
                error: function (xhr, status, error) {
                    $popularSearchesList.empty().append('<li><span class="error-msg">인기 검색어를 불러올 수 없습니다.</span></li>');
                    $popularSearchesContainer.slideDown(120);
                }
            });
        } else { // 캐시된 데이터 사용
            $popularSearchesList.empty();
            popularKeywordsData.forEach(function (searchItem) {
                const listItem = $('<li><a href="#"></a></li>');
                listItem.find('a').text(searchItem.keyword).attr('data-keyword', searchItem.keyword);
                $popularSearchesList.append(listItem);
            });
            $popularSearchesContainer.slideDown(120);
        }
    }

    $searchBarInput.on('focus', function (event) {
        event.stopPropagation();
        fetchAndDisplayPopularKeywords();
    });

    $popularSearchesList.on('click', 'a', function (event) {
        event.preventDefault();
        event.stopPropagation();
        const keyword = $(this).attr('data-keyword');
        $searchBarInput.val(keyword);
        $searchForm.submit();
        $popularSearchesContainer.slideUp(120);
    });

    // 외부 클릭 시 인기 검색어 숨기기
    $(document).on('click', function (event) {
        if (!$(event.target).closest('#search-bar').length && !$popularSearchesContainer.is(event.target) && $popularSearchesContainer.has(event.target).length === 0) {
            if ($popularSearchesContainer.is(":visible")) {
                $popularSearchesContainer.slideUp(120);
            }
        }
    });
});