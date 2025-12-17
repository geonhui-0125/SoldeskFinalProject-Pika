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

});