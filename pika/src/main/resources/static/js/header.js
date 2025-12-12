$(document).ready(function () {

    /** ▼ 메인 카테고리 hover 시 드롭다운 표시 */
    $("#category-wrapper").hover(
        function () {
            $("#category-dropdown")
                .stop(true, true)
                .slideDown(120);
        },
        function () {
            $("#category-dropdown")
                .stop(true, true)
                .slideUp(120);
        }
    );

    $(".category-item").hover(
        function () {
            $(this).find(".submenu").stop(true, true).show();
        },
        function () {
            $(this).find(".submenu").stop(true, true).hide();
        }
    );

});