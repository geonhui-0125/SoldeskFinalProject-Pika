/**
 * 탭 전환 기능
 * @param {string} type - 'purchase' 또는 'sales'
 */
function showTab(type) {
    $('.history-container').removeClass('active');
    $('.tab-btn').removeClass('active');

    if(type === 'purchase') {
        $('#purchase-list').addClass('active');
        $('.tab-btn').filter(function() { return $(this).text().trim() === '구매 내역'; }).addClass('active');
    } else {
        $('#sales-list').addClass('active');
        $('.tab-btn').filter(function() { return $(this).text().trim() === '판매 내역'; }).addClass('active');
    }
}

/**
 * 상세 모달 열기 및 데이터 바인딩
 */
function openDetail(type, title, amount, status, opponent, uid, date, productId) {
    $('#m-type').text(type + " 상세 내역");
    $('#m-title').text(title);
    $('#m-amount').text(Number(amount).toLocaleString() + "원");
    $('#m-status').text(status);
    $('#m-opp-label').text(type === '구매' ? '판매자' : '구매자');
    $('#m-opponent').text(opponent);
    $('#m-date').text(date);
    $('#m-uid').text(uid);

    // 상세보기 버튼: 어떤 상태에서든 상세 페이지로 이동
    $('#m-action-btn').attr('onclick', `location.href='/products/info/${productId}'`);

    $('#detailModal').css('display', 'flex').hide().fadeIn(200);
}

/**
 * 모달 닫기
 */
function closeModal() {
    $('#detailModal').fadeOut(200);
}

// 외부 영역 클릭 시 닫기
$(document).ready(function() {
    $(window).on('click', function(event) {
        if ($(event.target).is('#detailModal')) {
            closeModal();
        }
    });
});

// 외부 영역 클릭 시 닫기 처리
$(document).ready(function() {
    $(window).on('click', function(event) {
        if ($(event.target).is('#detailModal')) {
            closeModal();
        }
    });
});