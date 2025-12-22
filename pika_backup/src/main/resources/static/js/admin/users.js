document.addEventListener('DOMContentLoaded', function() {
    const userTableBody = document.getElementById('userTableBody');
    const restrictionModal = document.getElementById('restrictionModal');
    const closeButton = restrictionModal.querySelector('.close-button');
    const confirmRestrictButton = document.getElementById('confirmRestrictButton');
    const modalUserIdSpan = document.getElementById('modalUserId');
    const restrictionReasonInput = document.getElementById('restrictionReason');
    const restrictionEndDateInput = document.getElementById('restrictionEndDate');

    let currentUserIdToRestrict = null;

    // 사용자 목록 불러오기
    function fetchUsers() {
        fetch('/admin/api/users')
            .then(response => {
                if (!response.ok) {
                    throw new Error('사용자 목록을 불러오는 데 실패했습니다.');
                }
                return response.json();
            })
            .then(users => {
                userTableBody.innerHTML = ''; // 기존 내용 지우기
                users.forEach(user => {
                    const row = userTableBody.insertRow();
                    row.innerHTML = `
                        <td>${user.id}</td>
                        <td>${user.nickname}</td>
                        <td>${user.email}</td>
                        <td>${user.warningCount}</td>
                        <td>${user.isRestricted ? '제한됨' : '정상'}</td>
                        <td>${user.restrictionReason ? user.restrictionReason : '-'}</td>
                        <td>${user.restrictionEndDate ? new Date(user.restrictionEndDate).toLocaleString() : '-'}</td>
                        <td>${user.role}</td>
                        <td class="action-buttons">
                            <button class="warn-user-btn" data-user-id="${user.id}">경고 부여</button>
                            ${user.isRestricted ?
                                `<button class="unrestrict-user-btn" data-user-id="${user.id}">제한 해제</button>` :
                                `<button class="restrict-user-btn" data-user-id="${user.id}">이용 제한</button>`
                            }
                        </td>
                    `;
                });
                attachEventListeners();
            })
            .catch(error => {
                console.error('Error fetching users:', error);
                alert('사용자 목록을 불러오는 중 오류가 발생했습니다: ' + error.message);
            });
    }

    // 버튼에 이벤트 리스너 연결
    function attachEventListeners() {
        document.querySelectorAll('.warn-user-btn').forEach(button => {
            button.onclick = (event) => warnUser(event.target.dataset.userId);
        });

        document.querySelectorAll('.restrict-user-btn').forEach(button => {
            button.onclick = (event) => openRestrictionModal(event.target.dataset.userId);
        });

        document.querySelectorAll('.unrestrict-user-btn').forEach(button => {
            button.onclick = (event) => unrestrictUser(event.target.dataset.userId);
        });
    }

    // 사용자 경고 부여
    function warnUser(userId) {
        if (!confirm(`${userId} 사용자에게 경고를 부여하시겠습니까?`)) {
            return;
        }

        fetch(`/admin/api/users/${userId}/warn`, {
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
            fetchUsers(); // 목록 새로고침
        })
        .catch(error => {
            console.error('경고 부여 중 오류 발생:', error);
            alert('경고 부여 중 오류가 발생했습니다: ' + error.message);
        });
    }

    // 이용 제한 모달 열기
    function openRestrictionModal(userId) {
        currentUserIdToRestrict = userId;
        modalUserIdSpan.textContent = userId;
        restrictionReasonInput.value = '';
        restrictionEndDateInput.value = '';
        restrictionModal.style.display = 'block';
    }

    // 이용 제한 적용
    confirmRestrictButton.onclick = function() {
        const userId = currentUserIdToRestrict;
        const reason = restrictionReasonInput.value;
        const endDate = restrictionEndDateInput.value; // YYYY-MM-DDTHH:MM

        if (!reason.trim()) {
            alert('제한 사유를 입력해주세요.');
            return;
        }

        const payload = {
            reason: reason
        };
        if (endDate) {
            payload.endDate = endDate + ':00'; // 초까지 포함하도록 형식 맞춤
        }

        fetch(`/admin/api/users/${userId}/restrict`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.text();
        })
        .then(message => {
            alert(message);
            restrictionModal.style.display = 'none';
            fetchUsers(); // 목록 새로고침
        })
        .catch(error => {
            console.error('이용 제한 중 오류 발생:', error);
            alert('이용 제한 중 오류가 발생했습니다: ' + error.message);
        });
    };

    // 제한 해제
    function unrestrictUser(userId) {
        if (!confirm(`${userId} 사용자의 제한을 해제하시겠습니까?`)) {
            return;
        }

        fetch(`/admin/api/users/${userId}/unrestrict`, {
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
            fetchUsers(); // 목록 새로고침
        })
        .catch(error => {
            console.error('제한 해제 중 오류 발생:', error);
            alert('제한 해제 중 오류가 발생했습니다: ' + error.message);
        });
    }

    // 모달 닫기
    closeButton.onclick = function() {
        restrictionModal.style.display = 'none';
    };

    window.onclick = function(event) {
        if (event.target == restrictionModal) {
            restrictionModal.style.display = 'none';
        }
    };

    // 페이지 로드 시 사용자 목록 불러오기
    fetchUsers();
});
