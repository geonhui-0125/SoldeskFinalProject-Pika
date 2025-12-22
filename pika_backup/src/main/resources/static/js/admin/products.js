document.addEventListener('DOMContentLoaded', function() {
    const productTableBody = document.getElementById('productTableBody');
    const violationReasonModal = document.getElementById('violationReasonModal');
    const closeButton = violationReasonModal.querySelector('.close-button');
    const confirmMarkViolationButton = document.getElementById('confirmMarkViolationButton');
    const confirmDeleteProductButton = document.getElementById('confirmDeleteProductButton');
    const modalProductIdSpan = document.getElementById('modalProductId');
    const violationReasonInput = document.getElementById('violationReason');

    let currentProductIdToManage = null;
    let currentActionType = null; // 'mark' or 'delete'

    // 제품 목록 불러오기
    function fetchProducts() {
        fetch('/admin/api/products')
            .then(response => {
                if (!response.ok) {
                    throw new Error('제품 목록을 불러오는 데 실패했습니다.');
                }
                return response.json();
            })
            .then(products => {
                productTableBody.innerHTML = ''; // 기존 내용 지우기
                products.forEach(product => {
                    const row = productTableBody.insertRow();
                    row.innerHTML = `
                        <td>${product.id}</td>
                        <td>${product.productName}</td>
                        <td>${product.sellerId}</td>
                        <td>${product.policyViolationStatus}</td>
                        <td>${product.policyViolationReason ? product.policyViolationReason : '-'}</td>
                        <td class="action-buttons">
                            ${product.policyViolationStatus === 'NORMAL' ?
                                `<button class="mark-violation-btn" data-product-id="${product.id}">위반 표시</button>` :
                                `<button class="lift-violation-btn" data-product-id="${product.id}">위반 해제</button>`
                            }
                            ${product.policyViolationStatus !== 'DELETED_BY_ADMIN' ?
                                `<button class="delete-product-btn" data-product-id="${product.id}">제품 삭제</button>` :
                                `<button disabled>삭제됨</button>`
                            }
                        </td>
                    `;
                });
                attachEventListeners();
            })
            .catch(error => {
                console.error('Error fetching products:', error);
                alert('제품 목록을 불러오는 중 오류가 발생했습니다: ' + error.message);
            });
    }

    // 버튼에 이벤트 리스너 연결
    function attachEventListeners() {
        document.querySelectorAll('.mark-violation-btn').forEach(button => {
            button.onclick = (event) => openViolationReasonModal(event.target.dataset.productId, 'mark');
        });

        document.querySelectorAll('.delete-product-btn').forEach(button => {
            button.onclick = (event) => openViolationReasonModal(event.target.dataset.productId, 'delete');
        });

        document.querySelectorAll('.lift-violation-btn').forEach(button => {
            button.onclick = (event) => liftProductViolation(event.target.dataset.productId);
        });
    }

    // 정책 위반 사유 모달 열기
    function openViolationReasonModal(productId, actionType) {
        currentProductIdToManage = productId;
        currentActionType = actionType;
        modalProductIdSpan.textContent = productId;
        violationReasonInput.value = '';
        violationReasonModal.style.display = 'block';

        if (actionType === 'mark') {
            confirmMarkViolationButton.style.display = 'inline-block';
            confirmDeleteProductButton.style.display = 'none';
        } else if (actionType === 'delete') {
            confirmMarkViolationButton.style.display = 'none';
            confirmDeleteProductButton.style.display = 'inline-block';
        }
    }

    // 위반 표시 적용
    confirmMarkViolationButton.onclick = function() {
        const productId = currentProductIdToManage;
        const reason = violationReasonInput.value;

        if (!reason.trim()) {
            alert('위반 사유를 입력해주세요.');
            return;
        }

        fetch(`/admin/api/products/${productId}/markViolation`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ reason: reason })
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.text();
        })
        .then(message => {
            alert(message);
            violationReasonModal.style.display = 'none';
            fetchProducts(); // 목록 새로고침
        })
        .catch(error => {
            console.error('제품 위반 표시 중 오류 발생:', error);
            alert('제품 위반 표시 중 오류가 발생했습니다: ' + error.message);
        });
    };

    // 제품 삭제 적용
    confirmDeleteProductButton.onclick = function() {
        const productId = currentProductIdToManage;
        const reason = violationReasonInput.value;

        if (!reason.trim()) {
            alert('삭제 사유를 입력해주세요.');
            return;
        }

        if (!confirm(`${productId} 제품을 정말 삭제하시겠습니까? (정책 위반 사유: ${reason})`)) {
            return;
        }

        fetch(`/admin/api/products/${productId}/delete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ reason: reason })
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.text();
        })
        .then(message => {
            alert(message);
            violationReasonModal.style.display = 'none';
            fetchProducts(); // 목록 새로고침
        })
        .catch(error => {
            console.error('제품 삭제 중 오류 발생:', error);
            alert('제품 삭제 중 오류가 발생했습니다: ' + error.message);
        });
    };

    // 위반 해제
    function liftProductViolation(productId) {
        if (!confirm(`${productId} 제품의 정책 위반 상태를 해제하시겠습니까?`)) {
            return;
        }

        fetch(`/admin/api/products/${productId}/liftViolation`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.text();
        })
        .then(message => {
            alert(message);
            fetchProducts(); // 목록 새로고침
        })
        .catch(error => {
            console.error('제품 위반 해제 중 오류 발생:', error);
            alert('제품 위반 해제 중 오류가 발생했습니다: ' + error.message);
        });
    }

    // 모달 닫기
    closeButton.onclick = function() {
        violationReasonModal.style.display = 'none';
    };

    window.onclick = function(event) {
        if (event.target == violationReasonModal) {
            violationReasonModal.style.display = 'none';
        }
    };

    // 페이지 로드 시 제품 목록 불러오기
    fetchProducts();
});
