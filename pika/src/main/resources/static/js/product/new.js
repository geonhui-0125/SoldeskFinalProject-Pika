$(function () {

    $(function () {

        let selectedMain = "";

        // 메인 카테고리 클릭
        $(".category-main-item").on("click", function () {
            selectedMain = $(this).text().trim();

            // 다른 서브 리스트 닫기
            $(".category-sub-list").hide();

            // 현재 메인 카테고리의 서브 리스트 열기
            $(this).next(".category-sub-list").show();
        });

        // 서브 카테고리 클릭
        $(".category-sub-item").on("click", function () {
            const sub = $(this).text().trim();

            if (!selectedMain) return;

            const categoryPath = selectedMain + " > " + sub;

            // 화면 표시 텍스트 변경
            $(".category-selected-text").text(categoryPath);

            // hidden input에 값 저장
            $("#categoryPath").val(categoryPath);

            // 드롭다운 닫기
            $("#categoryDropdown").hide();
        });

        // 셀렉트 박스 클릭 → 드롭다운 토글
        $("#categorySelect").on("click", function () {
            $("#categoryDropdown").toggle();
        });

    });


    $(".image-input").on("change", function (e) {

        const file = e.target.files[0];
        if (!file) return;

        const reader = new FileReader();

        reader.onload = function (event) {
            $(".preview-image")
                .attr("src", event.target.result)
                .show();

            $(".image-upload-box")
                .css("border", "1px solid #ccc")


            $(".image-placeholder").hide();
        };

        reader.readAsDataURL(file);
    });

});

