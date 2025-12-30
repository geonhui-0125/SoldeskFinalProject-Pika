$(function () {
    // 카테고리 드롭다운 관련 로직 (이전 버전으로 복원)
    let selectedMain = "";
    $(".category-main-item").on("click", function () {
        selectedMain = $(this).text().trim();
        $(".category-sub-list").hide();
        $(this).next(".category-sub-list").show();
    });

    $(".category-sub-item").on("click", function () {
        const sub = $(this).text().trim();
        if (!selectedMain) return;
        const categoryPath = selectedMain + " > " + sub;
        $(".category-selected-text").text(categoryPath);
        $("#categoryPath").val(categoryPath);
        $("#categoryDropdown").hide();
    });

    $("#categorySelect").on("click", function () {
        $("#categoryDropdown").toggle();
    });

    // --- 이미지 업로드 로직 (수정된 버전 유지) ---
    const fileInput = document.getElementById('file-input');
    const previewContainer = document.querySelector('.image-preview-container');
    const imageCounter = document.querySelector('.image-counter');
    const MAX_FILES = 10;
    let fileList = []; // 파일 목록을 관리하는 배열

    const updatePreviewsAndFileInput = () => {
        previewContainer.innerHTML = '';

        fileList.forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const item = document.createElement('div');
                item.classList.add('image-preview-item');

                item.innerHTML = `
                    <img src="${e.target.result}" class="preview-image">
                    <button type="button" class="remove-image-btn" data-index="${index}">&times;</button>
                `;
                previewContainer.appendChild(item);
            };
            reader.readAsDataURL(file);
        });

        if (fileList.length < MAX_FILES) {
            const uploadBox = document.createElement('label');
            uploadBox.classList.add('image-upload-box');
            uploadBox.setAttribute('for', 'file-input');
            uploadBox.innerHTML = `
                <div class="plus-icon">+</div>
                <div class="placeholder-text">이미지 추가</div>
            `;
            previewContainer.appendChild(uploadBox);
        }

        imageCounter.textContent = `(${fileList.length}/${MAX_FILES})`;

        const dataTransfer = new DataTransfer();
        fileList.forEach(file => dataTransfer.items.add(file));
        fileInput.files = dataTransfer.files;

        if(fileList.length > 0) {
            // .invalid 클래스를 id가 아닌 class로 관리하도록 수정
            $('.image-upload-box').removeClass('invalid');
        }
    };

    fileInput.addEventListener('change', (event) => {
        const newFiles = Array.from(event.target.files);
        const remainingSlots = MAX_FILES - fileList.length;

        if (newFiles.length > remainingSlots) {
            alert(`이미지는 최대 ${MAX_FILES}개까지만 추가할 수 있습니다.`);
            fileList = fileList.concat(newFiles.slice(0, remainingSlots));
        } else {
            fileList = fileList.concat(newFiles);
        }
        
        event.target.value = ''; // 동일한 파일 재선택을 위해 초기화
        updatePreviewsAndFileInput();
    });

    previewContainer.addEventListener('click', (event) => {
        if (event.target.classList.contains('remove-image-btn')) {
            const indexToRemove = parseInt(event.target.getAttribute('data-index'), 10);
            fileList.splice(indexToRemove, 1);
            updatePreviewsAndFileInput();
        }
    });

    // --- 폼 제출 유효성 검사 (이전 버전 기반으로 복원 및 수정) ---
    $("#productForm").on("submit", function (e) {
        $(".invalid").removeClass("invalid");

        if (fileList.length === 0) {
            alert("이미지를 1개 이상 등록해주세요.");
            $(".image-upload-box").addClass("invalid");
            e.preventDefault();
            return false;
        }

        const title = $("input[name='title']");
        if (title.val().trim() === "") {
            alert("상품명을 입력해주세요.");
            title.addClass("invalid").focus();
            e.preventDefault();
            return false;
        }

        if ($("#categoryPath").val() === "") {
            alert("카테고리를 선택해주세요.");
            $("#categorySelect").addClass("invalid");
            e.preventDefault();
            return false;
        }

        const desc = $("textarea[name='description']");
        if (desc.val().trim() === "") {
            alert("설명을 입력해주세요.");
            desc.addClass("invalid").focus();
            e.preventDefault();
            return false;
        }

        const priceWrap = $(".price-content");
        const price = $("input[name='price']");
        if (price.val().trim() === "") {
            alert("가격을 입력해주세요.");
            priceWrap.addClass("invalid");
            e.preventDefault();
            return false;
        }
        return true;
    });

    // --- 가격 입력 및 유효성 클래스 제거 (이전 버전으로 복원) ---
    $(".price-input").on("input", function () {
        let originalValue = $(this).val();
        let numericValue = originalValue.replace(/[^0-9]/g, '');
        if (originalValue !== numericValue) {
            alert("숫자만 입력 가능합니다.");
            $(this).val(numericValue);
        }
    });

    $("input, textarea, #categorySelect, .image-input").on("click", function () {
        $(this).removeClass("invalid");
        $(this).closest(".price-content").removeClass("invalid");
        $(".image-upload-box").removeClass("invalid");
    });
});

