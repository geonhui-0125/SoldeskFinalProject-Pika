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

    $("#productForm").on("submit", function (e) {
        // 1. 이전 에러 표시 초기화
        $(".invalid").removeClass("invalid");

        // 2. 이미지 체크
        if ($(".image-input")[0].files.length === 0) {
            alert("이미지를 등록해주세요.");
            $(".image-upload-box").addClass("invalid");
            e.preventDefault(); // 전송 중단
            return false;
        }

        // 3. 상품명 체크
        const title = $("input[name='title']");
        if (title.val().trim() === "") {
            alert("상품명을 입력해주세요.");
            title.addClass("invalid").focus();
            e.preventDefault();
            return false;
        }

        // 4. 카테고리 체크
        if ($("#categoryPath").val() === "") {
            alert("카테고리를 선택해주세요.");
            $("#categorySelect").addClass("invalid");
            e.preventDefault();
            return false;
        }

        // 5. 설명 체크
        const desc = $("textarea[name='description']");
        if (desc.val().trim() === "") {
            alert("설명을 입력해주세요.");
            desc.addClass("invalid").focus();
            e.preventDefault();
            return false;
        }

        // 6. 가격 체크
        const price = $("input[name='price']");
        if (price.val().trim() === "") {
            alert("가격을 입력해주세요.");
            price.addClass("invalid").focus();
            e.preventDefault();
            return false;
        }

        return true;
    });

    /*$(".price-input").on("input", function () {
        // 현재 입력값에서 숫자만 남기기
        let value = $(this).val().replace(/[^0-9]/g, '');

        // 숫자가 있을 경우에만 천단위 콤마 찍기
        if (value !== "") {
            value = Number(value).toLocaleString();
        }

        // 변환된 값을 다시 input에 넣기
        $(this).val(value);
    });*/
});

