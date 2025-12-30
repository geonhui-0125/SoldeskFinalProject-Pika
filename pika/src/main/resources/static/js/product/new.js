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


    // 파일 리스트를 관리하는 전역 변수
    let imageFiles = [];

    // 파일 변경 시 미리보기를 업데이트하는 함수
    function updateImagePreviews() {
        const $mainImageBox = $("#main-image-box");
        const $mainPreview = $mainImageBox.find(".main-preview-image");
        const $placeholder = $mainImageBox.find(".placeholder-text");
        const $additionalList = $("#additional-image-list");

        // 모든 미리보기 초기화
        $additionalList.empty();
        // 대표 이미지 삭제 버튼도 함께 제거
        $mainImageBox.find(".remove-image-btn").remove(); 
        $mainPreview.hide().attr("src", "");
        $placeholder.show();
        $mainImageBox.removeClass("invalid");

        if (imageFiles.length > 0) {
            // 대표 이미지 설정
            $placeholder.hide();
            const mainImageReader = new FileReader();
            mainImageReader.onload = function(e) {
                $mainPreview.attr("src", e.target.result).show();
            };
            mainImageReader.readAsDataURL(imageFiles[0]);

            // 대표 이미지에 삭제 버튼 추가
            const $mainRemoveBtn = $(`<button type="button" class="remove-image-btn" data-index="0">&times;</button>`);
            $mainImageBox.append($mainRemoveBtn);

            // 추가 이미지 목록 설정
            imageFiles.slice(1).forEach((file, index) => {
                const additionalImageReader = new FileReader();
                additionalImageReader.onload = function(e) {
                    const $additionalItem = $(`
                        <div class="additional-image-item">
                            <img src="${e.target.result}" alt="추가 이미지">
                            <button type="button" class="remove-image-btn" data-index="${index + 1}">&times;</button>
                        </div>
                    `);
                    $additionalList.append($additionalItem);
                };
                additionalImageReader.readAsDataURL(file);
            });
        }

        // 파일 입력(input) 엘리먼트의 files 속성 업데이트
        const dataTransfer = new DataTransfer();
        imageFiles.forEach(file => dataTransfer.items.add(file));
        $(".image-input")[0].files = dataTransfer.files;
    }

    // 파일 입력(input) 변경 이벤트
    $(".image-input").on("change", function(e) {
        imageFiles = Array.from(e.target.files); // 직접 imageFiles 업데이트
        updateImagePreviews();
    });

    // 이미지 삭제 버튼 클릭 이벤트
    $(document).on("click", ".remove-image-btn", function() {
        const indexToRemove = $(this).data("index");
        
        // 버튼이 속한 컨테이너에서 버튼 제거
        $(this).remove(); 
        
        imageFiles.splice(indexToRemove, 1);
        updateImagePreviews();
    });

    // 폼 제출 시 유효성 검사 로직 (imageFiles 배열을 확인하도록 변경)
    $("#productForm").on("submit", function (e) {
        // 1. 이전 에러 표시 초기화
        $(".invalid").removeClass("invalid");

        // 2. 이미지 체크
        if (imageFiles.length === 0) {
            alert("이미지를 1개 이상 등록해주세요.");
            $("#main-image-box").addClass("invalid"); // 클래스명 변경
            e.preventDefault();
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

        // 6. 가격 체크 (priceWrap.addClass("invalid") 부분 수정)
        const priceWrap = $(".price-content");
        const price = $("input[name='price']");

        if (price.val().trim() === "") {
            alert("가격을 입력해주세요.");
            price.addClass("invalid"); // price 입력창 자체에 invalid 클래스 적용
            priceWrap.addClass("invalid"); // price-content 래퍼에도 invalid 클래스 적용
            e.preventDefault();
            return false;
        }

        return true;
    });

    $(".price-input").on("input", function () {

        let originalValue = $(this).val();
        let numericValue = originalValue.replace(/[^0-9]/g, '');

        // 원본값과 숫자만 추출한 값이 다르면 = 숫자가 아닌 문자가 섞여있음
        if (originalValue !== numericValue) {
            alert("숫자만 입력 가능합니다.");
            $(this).val(numericValue); // 문자는 지워버림
        }
    });

    $("input, textarea, #categorySelect, .image-input").on("click", function() {
        $(this).removeClass("invalid");
        $(this).closest(".price-content").removeClass("invalid");
    });
});

