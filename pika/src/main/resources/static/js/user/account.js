$(document).ready(function() {
    let currentMaxLen = 16;
    const sellerName = $('#currentUserName').val();

    // 모달 열기/닫기
    window.openBankModal = function() {
        showView(); // 열 때 항상 조회 화면부터 표시
        $('#bankModal').removeClass('hidden');
    };
    window.closeBankModal = function() {
        $('#bankModal').addClass('hidden');
    };

    // 화면 전환 함수
    window.showEditForm = function() {
        // 1. 먼저 화면을 전환 (display: block 상태로 만듦)
        $('#accountView').addClass('hidden');
        $('#accountEditForm').removeClass('hidden');

        // 2. 브라우저 자동완성 및 렌더링 지연을 고려하여 0.01초 뒤에 초기화 실행
        setTimeout(function() {

            $('#accountNumber').val('');
            $('#accountHolderDisplay').val('').attr('placeholder', '조회 버튼을 눌러주세요');

            $('#bankCode').val('');

            $('#selected-bank-name').text('은행을 선택해 주세요')

            console.log("은행 코드 및 계좌 정보 초기화 완료");
        }, 10);
    };
    window.showView = function() {
        $('#accountEditForm').addClass('hidden');
        $('#accountView').removeClass('hidden');
    };

    // 은행 선택 (004 보존 로직 추가)
    window.selectBank = function(element) {
        // 1. jQuery를 이용해 data 속성 읽기 (문자열 보존)
        const code = $(element).data('code'); // "004"
        const name = $(element).data('name');
        const len = $(element).data('len');

        // 데이터가 정상적으로 읽혔는지 확인
        if (!code) {
            console.error("은행 코드를 읽지 못했습니다. element:", element);
            return;
        }

        // 2. 문자열 강제 변환 및 3자리 패딩 (안전장치)
        const formattedCode = String(code).padStart(3, '0');

        // 3. Hidden input에 세팅
        $('#bankCode').val(formattedCode);

        // 4. 자릿수 제한 및 UI 업데이트
        currentMaxLen = parseInt(len) || 16;
        $('#accountNumber').attr('maxlength', currentMaxLen);

        $('#selected-bank-name').text(name)
            .css({'color': '#ff4500', 'font-weight': 'bold'});

        console.log("선택된 은행 코드:", formattedCode);
    };

    // 계좌 조회 시뮬레이션
    window.verifyAccount = function() {
        const accNum = $('#accountNumber').val();

        if (!$('#bankCode').val()) { alert("은행을 선택해 주세요."); return; }

        if(!accNum) { alert("계좌번호를 입력해 주세요."); return; }

        if (accNum.length < (currentMaxLen - 3)) {
            alert("계좌번호가 너무 짧습니다."); return; }

        const $holderDisplay = $('#accountHolderDisplay');
        $holderDisplay.val("조회 중...");

        setTimeout(() => {
            $holderDisplay.val(sellerName || "익명");
        }, 500);
    };

    // 숫자 입력 제한
    $('#accountNumber').on('input', function() {
        let val = $(this).val().replace(/[^0-9]/g, '');
        if (val.length > currentMaxLen) val = val.substring(0, currentMaxLen);
        $(this).val(val);
    });

    // 폼 전송 검증
    $('#accountEditForm').on('submit', function(e) {
        const holderVal = $('#accountHolderDisplay').val();
        if (!holderVal || holderVal.includes("조회")) {
            alert("계좌 조회를 완료해 주세요."); e.preventDefault(); return;
        }
    });
});